package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "order_item")
@AllArgsConstructor
@NoArgsConstructor
public class OrderItem  {
    @Id
    @Column(name = "order_item_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "unitPrice")
    private BigDecimal unitPrice;

    @Column(name = "quantity")
    private BigDecimal quantity;

    @Column(name = "totalPrice")
    private BigDecimal totalPrice;


}
