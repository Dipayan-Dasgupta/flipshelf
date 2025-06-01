package com.flipshelf.controller;

import com.flipshelf.model.Cart;
import com.flipshelf.model.Product;
import com.flipshelf.model.Purchase;
import com.flipshelf.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private UserService userService;

    @GetMapping("/products")
    public ResponseEntity<?> viewAllProducts(@RequestHeader("Authorization") String token) {
        ResponseEntity<?> products = userService.viewAllProducts(token);
        return ResponseEntity.ok(products).getBody();
    }

    @PostMapping("/orders")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Purchase> createOrder(@RequestHeader("Authorization") String token, @Valid @RequestBody Purchase purchase) {
        logger.info("Creating order for product ID: {}", purchase.getProductId());
        Purchase purchase1 = userService.purchaseItems(token, purchase);
        return ResponseEntity.status(HttpStatus.CREATED).body(purchase1);
    }

    @GetMapping("/orders")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Purchase>> getOrderHistory(@RequestHeader("Authorization") String token) {
        logger.info("Fetching order history for authenticated user");
        List<Purchase> orders = userService.getPurchaseHistory();
        return ResponseEntity.ok(orders);
    }

    @PostMapping("/cart")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Cart> addToCart(@RequestHeader("Authorization") String token, @Valid @RequestBody Cart cart) {
        logger.info("Adding product ID {} to cart", cart.getProductId());
        Cart cart1 = userService.addToCart(cart);
        return ResponseEntity.status(HttpStatus.CREATED).body(cart);
    }

    @GetMapping("/cart")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Cart>> getCart(@RequestHeader("Authorization") String token) {
        logger.info("Fetching cart for authenticated user");
        List<Cart> cartItems = userService.getSavedCart();
        return ResponseEntity.ok(cartItems);
    }

    @DeleteMapping("/cart/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> removeFromCart(@RequestHeader("Authorization") String token, @PathVariable Long id){
        logger.info("Removing cart item ID: {}", id);
        userService.removeFromCart(id);
        return ResponseEntity.noContent().build();
    }
}
