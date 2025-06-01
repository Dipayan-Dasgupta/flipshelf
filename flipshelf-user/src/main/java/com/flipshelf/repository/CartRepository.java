package com.flipshelf.repository;

import com.flipshelf.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart,Long> {
    List<Cart> findByUserEmail(String userEmail);
    Optional<Cart> findByUserEmailAndProductId(String userEmail, Long productId);
}
