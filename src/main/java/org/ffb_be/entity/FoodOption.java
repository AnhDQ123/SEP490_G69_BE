package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.ffb_be.utils.enums.Status;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "food_options")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FoodOption extends BaseEntity {
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

    private String image;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product food;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private Types type;

    @OneToMany(mappedBy = "FoodOption")
    private List<CartItemOption> cartItemOptions;

    @OneToMany(mappedBy = "FoodOption")
    private List<OrderItemOption> orderItemOptions;
}
