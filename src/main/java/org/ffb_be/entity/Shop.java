package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.ffb_be.utils.enums.Status;

import java.time.LocalDate;

@Entity
@Table(name = "shop")
@AllArgsConstructor
@NoArgsConstructor
public class Shop {
    @Id
    @Column(name = "shop_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="shop_name")
    private String name;

    @Column(name="description")
    private String description;

    @Column(name="logo")
    private String logo;

    @Column(name="backgroundImage")
    private String backgroundImage;

    @Column(name="phone")
    private String phone;

    @Column(name="address")
    private String address;

    @Column(name="registration_certificate")
    private String Registration_certificate;

    @Column(name="food_safety_certificate")
    private String Food_safety_certificate;

    @Column(name="rate")
    private int rate;

    @Column(name="view_count")
    private int view_count;

    @Enumerated(EnumType.STRING)
    private Status is_active;

    @Column(name="created_at")
    private LocalDate created_at;

    @Column(name="updated_at")
    private LocalDate updated_at;


}
