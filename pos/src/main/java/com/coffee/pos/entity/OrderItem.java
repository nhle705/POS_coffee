package com.coffee.pos.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "order_items")
@Getter
@Setter
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productName;
    private Integer quantity;
    private Double price;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonBackReference
    private Order order;
}