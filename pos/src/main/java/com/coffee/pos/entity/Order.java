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

    @Column(name = "discount")
    private Double discount = 0.0; // Lưu % giảm giá (VD: 10.0)

    @Column(name = "tax")
    private Double tax = 10.0;     // Lưu % thuế VAT (VD: 10.0)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status = OrderStatus.PENDING; 

    @Column(name = "cancel_reason", length = 500)
    private String cancelReason;

    @Column(name = "payment_method")
    private String paymentMethod; 

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JsonManagedReference
    private List<OrderItem> items = new ArrayList<>();

   
    @PrePersist
    protected void onCreate() {
        if (this.orderDate == null) {
            this.orderDate = LocalDateTime.now();
        }
    }

    public Order() {
        this.status = OrderStatus.PENDING;
        this.discount = 0.0;
        this.tax = 10.0;
    }

    public enum OrderStatus {
        PENDING,   
        PAID,      
        CANCELLED  
    }
}