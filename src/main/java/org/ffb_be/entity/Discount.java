package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.ffb_be.utils.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "discounts")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Discount {
    @Id
    @Column(name = "discount_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="discount_percentage")
    private BigDecimal discount_percentage;

    @Column(name="created_at")
    private LocalDate created_at;

    @Column(name="updated_at")
    private LocalDate updated_at;

    @Column(name="start_date")
    private LocalDate start_date;

    @Column(name="end_date ")
    private LocalDate end_date ;
    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy = "discount")
    private List<Product> products;
}
