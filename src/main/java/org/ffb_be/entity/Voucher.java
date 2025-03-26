package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ffb_be.utils.enums.DiscountType;
import org.ffb_be.utils.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "vouchers")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Voucher  extends BaseEntity {
    @Id
    @Column(name = "voucher_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code" , unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    private DiscountType discountType;

    @Column(name = "discount_value")
    private BigDecimal discountValue;

    @Column(name = "min_order_value")
    private BigDecimal minOrderValue;

    @Column(name = "total_vouchers")
    private Integer totalVouchers;

    @Column(name = "used_vouchers")
    private Integer usedVouchers = 0;

    @Column(name="start_date")
    private LocalDate startDate;

    @Column(name="end_date")
    private LocalDate endDate ;

    @Column(name = "max_usage_per_customer")
    private Integer maxUsagePerCustomer; // Số lần tối đa mỗi khách hàng được dùng

    @Column(name = "is_stackable")
    private Boolean isStackable;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(mappedBy = "voucher")
    private List<Order> orders;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    private Shop shop;
}
