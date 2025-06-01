package com.flipshelf.serviceimpl;

import com.flipshelf.cache.CacheManager;
import com.flipshelf.model.Product;
import com.flipshelf.repository.ProductRepository;
import com.flipshelf.service.ProductService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Service
public class ProductServiceImpl implements ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CacheManager cacheManager;

    @Override
    @Transactional
    public Product addProduct(Product product) {
        String sellerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        product.setSellerEmail(sellerEmail);
        cacheManager.invalidateCache(sellerEmail);
        return productRepository.save(product);
    }

    @Override
    @Transactional
    public Product editProduct(Long id, Product updatedProduct) {
        String sellerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Product existingProduct = productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (!existingProduct.getSellerEmail().equals(sellerEmail)) {
            throw new SecurityException("Not authorized to edit this product");
        }

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setStock(updatedProduct.getStock());
        cacheManager.invalidateCache(sellerEmail);
        return productRepository.save(existingProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        String sellerEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Product product = productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (!product.getSellerEmail().equals(sellerEmail)) {
            throw new SecurityException("Not authorized to delete this product");
        }
        cacheManager.invalidateCache(sellerEmail);
        productRepository.delete(product);
    }

    @Async("productTaskExecutor")
    @CircuitBreaker(name = "productServiceCB", fallbackMethod = "fallbackForProductService")
    @Retry(name = "productServiceRetry")
    @RateLimiter(name = "productServiceRateLimiter")
    @Override
    public CompletableFuture<Page<Product>> viewProducts(int page, int size) {
        Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
        logger.info("Async thread started: {}", Thread.currentThread().getName());

        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            logger.error("Authentication is null in async thread! Access denied.");
            throw new RuntimeException("Unauthorized access");
        }

        String sellerEmail = authentication.getName();
        String role = authentication.getAuthorities().stream().map(grantedAuthority -> grantedAuthority.getAuthority()).findFirst().orElse("ROLE_UNKNOWN");

        logger.info("User: {}, Role: {}", sellerEmail, role);

        Pageable pageable = PageRequest.of(page, size, Sort.by("price").ascending());
        String cacheKey = cacheManager.generateCacheKey(role, sellerEmail, page, size);
        Page<Product> cachedProduct = cacheManager.getCachedProducts(cacheKey, pageable);
        if (cachedProduct != null) {
            return CompletableFuture.completedFuture(cachedProduct);
        }
        Page<Product> productsPage;
        if (role.equals("ROLE_ADMIN") || role.equals("ROLE_USER")) {
            productsPage = productRepository.findAll(pageable);
        } else if (role.equals("ROLE_SELLER")) {
            productsPage = productRepository.findBySellerEmail(sellerEmail, pageable);
        } else {
            productsPage = Page.empty(pageable); // no products
        }
        logger.info("Caching result with key: {}", cacheKey);
        cacheManager.cacheProducts(cacheKey, productsPage);
        logger.info("Total products found: {}", productsPage.getTotalElements());
        logger.info("Async thread finished: {}", Thread.currentThread().getName());
        logger.info("Async thread finished: {}", Thread.currentThread().getName());
        return CompletableFuture.completedFuture(productsPage);
    }

    @Override
    public Product updateStock(Long productId, int quantity) throws Exception {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        Product product = productRepository.findById(productId).orElseThrow(() -> new Exception("Product not found with ID: " + productId));
        if (product.getStock() < quantity) {
            throw new IllegalStateException("Insufficient stock for product: " + product.getName());
        }
        product.setStock(product.getStock() - quantity);
        String sellerEmail = productRepository.findById(productId).get().getSellerEmail();
        cacheManager.invalidateCache(sellerEmail);
        return productRepository.save(product);
    }

    public CompletableFuture<Page<Product>> fallbackForProductService(int page, int size, Throwable throwable) {
        Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
        if (throwable instanceof RequestNotPermitted) {
            logger.warn("Request was rate-limited.");
        } else {
            logger.warn("Breaker: {}", throwable.getMessage());
        }
        return CompletableFuture.completedFuture(null); // Indicates fallback occurred
    }
}
