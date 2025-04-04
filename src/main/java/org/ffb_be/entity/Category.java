package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Table(name = "categorys")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Category extends BaseEntity{
    @Id
    @Column(name = "category_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="category_name")
    private String name;

    @Column(name="description", columnDefinition = "TEXT")
    private String description;

    @Column
    private String image;

    @OneToMany(mappedBy = "category")
    private Set<Product> foods;
}
