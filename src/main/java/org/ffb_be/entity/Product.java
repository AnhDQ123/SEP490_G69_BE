package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.ffb_be.utils.enums.Status;

import java.time.LocalDate;

@Entity
@Table(name = "product")
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="product_name")
    private String name;

    @Column(name="manufacturer")
    private String manufacturer;

    @Column(name="supplier")
    private String supplier;

    @Column(name="expired_date")
    private LocalDate expired_date;

    @Column(name="description")
    private LocalDate description;

    @Column(name="rate")
    private Float rate;

    @Enumerated(EnumType.STRING)
    private Status is_active;

    @Column(name="created_at")
    private LocalDate created_at;

    @Column(name="updated_at")
    private LocalDate updated_at;
}
