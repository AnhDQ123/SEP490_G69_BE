package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.ffb_be.utils.enums.ReportEnum;
import org.ffb_be.utils.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "food_option")
@AllArgsConstructor
@NoArgsConstructor
public class FoodOption {
    @Id
    @Column(name = "food_option_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name")
    private String name;

    @Column(name="price")
    private BigDecimal price;

    @Column(name="quantity")
    private int quantity;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name="created_at")
    private LocalDate created_at;

    @Column(name="updated_at")
    private LocalDate updated_at;
}
