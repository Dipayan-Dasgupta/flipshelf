package com.flipshelf.repository;
import com.flipshelf.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PurchaseRepository extends JpaRepository<Purchase,Long> {
    List<Purchase> findByUserEmail(String userEmail);
}
