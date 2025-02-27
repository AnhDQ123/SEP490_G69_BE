package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.ffb_be.utils.enums.Status;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "products")
@AllArgsConstructor
@NoArgsConstructor
@Builder
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

    @ManyToMany
    @JoinTable(
            name = "food_category",
            joinColumns = @JoinColumn(name = "food_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories;

    @ManyToOne
    @JoinColumn(name = "discount_id")
    private Discount discount;

    @OneToMany(mappedBy = "product")
    private List<Feedback> feedbacks;

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
