package com.flipshelf.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Entity
@Table(name = "flipshelf_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  private String description;

  @Column(nullable = false)
  @Min(value = 0, message = "Price must be non-negative")
  private double price;

  @Min(value = 0, message = "Stock must be non-negative")
  @Column(nullable = false)
  private int stock;

  @Column(nullable = false)
  private String sellerEmail;
}
