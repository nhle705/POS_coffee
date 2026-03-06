package com.coffee.pos.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "products")
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "nvarchar(255)")
    private String name;

    private Double price;

    @Column(columnDefinition = "nvarchar(255)")
    private String category;
}