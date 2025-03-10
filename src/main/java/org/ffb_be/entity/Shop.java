package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ffb_be.utils.enums.SellType;
import org.ffb_be.utils.enums.Status;
import java.util.List;

@Entity
@Table(name = "shops")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Shop extends BaseEntity{
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

    @Column(name="background_image")
    private String backgroundImage;

    @Column(name="phone")
    private String phone;

    @Column(name="address")
    private String address;

    @Column(name="registration_certificate")
    private String registrationCertificate;

    @Column(name="food_safety_certificate")
    private String foodSafetyCertificate;

    @Column(name="menu")
    private String menu;

    @Column(name="rate")
    private Double rate;

    @Column(name="view_count")
    private Integer viewCount;

    @Enumerated(EnumType.STRING)
    private Status isActive;

    @Column(name="is_shipping")
    private Boolean isShipping;

    @Column(name="is_opening")
    private Boolean isOpening;

    @Column(name="open_time")
    private String openTime;

    @Column(name="close_time")
    private String closeTime;

    @OneToMany(mappedBy = "shop")
    private List<Product> products;

    @Enumerated(EnumType.STRING)
    private SellType sellType;

    @OneToMany(mappedBy = "shop")
    private List<Feedback> feedbacks;

    @OneToMany(mappedBy = "shop")
    private List<Voucher> vouchers;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User owner;
}
