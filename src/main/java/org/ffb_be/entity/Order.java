package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.ffb_be.utils.enums.CartEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "order")
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @Id
    @Column(name = "order_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "voucher_code")
    private String voucher_code;
    @Column(name = "shipping_address")
    private String shipping_address;
    @Column(name = "total")
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    private CartEnum status;

    @Column(name="created_at")
    private LocalDate created_at;

    @Column(name="updated_at")
    private LocalDate updated_at;

}
