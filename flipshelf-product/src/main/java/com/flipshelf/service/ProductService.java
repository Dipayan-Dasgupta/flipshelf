package com.flipshelf.service;

import com.flipshelf.model.Product;
import org.springframework.data.domain.Page;
import java.util.concurrent.CompletableFuture;

public interface ProductService {
    Product addProduct(Product product);

    Product editProduct(Long id, Product product);

    void deleteProduct(Long id);

    CompletableFuture<Page<Product>> viewProducts(int page, int size);

    Product updateStock(Long productId, int quantity) throws Exception;
}
