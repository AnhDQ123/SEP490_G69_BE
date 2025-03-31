package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ffb_be.utils.enums.SellType;
import org.ffb_be.utils.enums.Status;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "products")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Product extends BaseEntity {
    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="product_name")
    private String name;

    @Column(name="manufacturer")
    private String manufacturer;

    @Column
    private int quantity;

    @Column
    private String image;

    @Column(name="supplier")
    private String supplier;

    @Column(name="expired_date")
    private LocalDate expired_date;

    @Column(name="description")
    private String description;

    @Column(name="rate")
    private Float rate;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product")
    private List<Discount> discounts;

    @OneToMany(mappedBy = "product")
    private List<Feedback> feedbacks;

    @Enumerated(EnumType.STRING)
    private SellType type;

    @OneToMany(mappedBy = "product")
    private List<CartItem> cartItems;

    @OneToMany(mappedBy = "product")
    private List<OrderItem> orderItems;

    @OneToMany(mappedBy = "food")
    private List<FoodOption> foodOptions;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;

}
