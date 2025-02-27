package org.ffb_be.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.ffb_be.utils.enums.TypesCategory;

import java.util.List;

@Entity
@Table(name = "types")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Types extends BaseEntity {
    @Id
    @Column(name = "type_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TypesCategory category;

    @Column(name = "description")
    private String description;

    @OneToMany(mappedBy = "type")
    private List<FoodOption> foodOptions;

    @OneToMany(mappedBy = "type")
    private List<Image> images;

    @OneToMany(mappedBy = "type")
    private List<Report> reports;

}
