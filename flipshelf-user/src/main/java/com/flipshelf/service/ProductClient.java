package com.flipshelf.service;

import com.flipshelf.model.Product;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "product-service", url = "${product.service.url}/api/products")
public interface ProductClient {
    @GetMapping("/view")
    ResponseEntity<?> getAllProducts(@RequestHeader("Authorization") String token);

    @GetMapping("/update-stock")
    Product updateStock(@RequestHeader("Authorization") String token, @RequestParam("productId") Long productId, @RequestParam("quantity") int quantity);
}
