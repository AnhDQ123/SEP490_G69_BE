package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.ffb_be.utils.enums.Status;

import java.util.List;


@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User extends BaseEntity{
    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;

    @Column(name="phone",unique = true)
    private String phone;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToMany(mappedBy = "writer")
    private List<Blog> blogs;

    @OneToMany(mappedBy = "owner")
    private List<Cart> carts;

    @OneToMany(mappedBy = "writer")
    private List<Comment> comments;

    @OneToMany(mappedBy = "writer")
    private List<Feedback> feedbacks;

    @OneToMany(mappedBy = "owner")
    private List<Order> orders;

    @OneToMany(mappedBy = "shipper")
    private List<Order> shipperOrders;

    @OneToOne(mappedBy = "user")
    private Profile profile;

    @OneToOne(mappedBy = "owner")
    private Shop shop;

    @OneToMany(mappedBy = "reporter")
    private List<Report> reports;
}
