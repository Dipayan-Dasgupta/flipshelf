package com.flipshelf.model;
import lombok.Data;

@Data
public class Product {

    private Long id;

    private String name;

    private String description;

    private double price;

    private int stock;

    private String sellerEmail;
}
