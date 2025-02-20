package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cart_item")
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {
    @Id
    @Column(name = "cart_item_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "unitPrice")
    private BigDecimal unitPrice;

    @Column(name = "quantity")
    private BigDecimal quantity;

    @Column(name = "totalPrice")
    private BigDecimal totalPrice;

    @Column(name="created_at")
    private LocalDate created_at;

    @Column(name="updated_at")
    private LocalDate updated_at;
}
