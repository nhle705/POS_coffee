package com.coffee.pos.entity;

import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(name = "total_amount")
    private Double totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<OrderItem> items = new ArrayList<>();

    public Order() {
        this.orderDate = LocalDateTime.now();
    }
}