package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "categorys")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Category extends BaseEntity{
    @Id
    @Column(name = "category_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="category_name")
    private String name;

    @Column(name="description")
    private String description;


    @ManyToMany(mappedBy = "categories")
    private Set<Product> foods;
}
