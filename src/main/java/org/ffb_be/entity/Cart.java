package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.ffb_be.utils.enums.CartEnum;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "cart")
@AllArgsConstructor
@NoArgsConstructor
public class Cart {
    @Id
    @Column(name = "cart_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(name = "total")
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    private CartEnum status;

    @Column(name="created_at")
    private LocalDate created_at;

    @Column(name="updated_at")
    private LocalDate updated_at;

}
