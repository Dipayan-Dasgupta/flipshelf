package com.flipshelf.controller;

import com.flipshelf.model.Product;
import com.flipshelf.service.ProductService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
    @Autowired
    private ProductService productService;

    @PostMapping("/add")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Product> addProduct(@RequestHeader("Authorization") String token, @Valid @RequestBody Product product) {
        Product savedProduct = productService.addProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @PutMapping("/edit/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Product> editProduct(@RequestHeader("Authorization") String token, @PathVariable Long id, @Valid @RequestBody Product product) {
        Product updatedProduct = productService.editProduct(id, product);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Void> deleteProduct(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/view")
    public ResponseEntity<?> viewProducts(@RequestHeader("Authorization") String token, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Product> productPage = productService.viewProducts(page,size).get(); // Waits for async
            if (productPage == null) {
                return ResponseEntity.ok(Map.of(
                        "description", "Service temporarily unavailable. Please try again later."
                ));
            }
            return ResponseEntity.ok(Map.of("products", productPage.getContent(), "currentPage", productPage.getNumber(), "totalPages", productPage.getTotalPages(), "totalItems", productPage.getTotalElements()));
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            logger.error("Execution exception in viewProducts(): {}", cause.getMessage(), cause);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", cause.getMessage()));
        } catch (Exception ex) {
            logger.error("Unexpected error in viewProducts(): {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/update-stock")
    @PreAuthorize("hasRole('SELLER') or hasRole('USER')")
    public ResponseEntity<Product> updateStock(@RequestHeader("Authorization") String token, @RequestParam Long productId, @RequestParam int quantity) throws Exception {
        logger.info("Updating stock for product ID: {}, quantity: {}", productId, quantity);
        Product updatedProduct = productService.updateStock(productId, quantity);
        return ResponseEntity.ok(updatedProduct);
    }
}
