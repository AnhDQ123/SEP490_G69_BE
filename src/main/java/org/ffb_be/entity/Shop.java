package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.ffb_be.utils.enums.Status;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "shops")
@AllArgsConstructor
@NoArgsConstructor
@Builder
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

    @OneToMany(mappedBy = "shop")
    private List<Product> products;

    @OneToMany(mappedBy = "shop")
    private List<Feedback> feedbacks;

    @OneToMany(mappedBy = "shop")
    private List<Voucher> vouchers;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User owner;
}
