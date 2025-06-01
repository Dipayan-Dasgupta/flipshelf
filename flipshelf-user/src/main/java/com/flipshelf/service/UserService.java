package com.flipshelf.service;

import com.flipshelf.model.Cart;
import com.flipshelf.model.Product;
import com.flipshelf.model.Purchase;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserService {
    ResponseEntity<?> viewAllProducts(String token);

    Cart addToCart(Cart cart);

    Purchase purchaseItems(String token,Purchase purchase);

    List<Purchase> getPurchaseHistory();

    List<Cart> getSavedCart();

    void removeFromCart(Long id) ;
}
