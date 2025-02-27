package org.ffb_be.repository;

import org.ffb_be.entity.Types;
import org.ffb_be.utils.enums.TypesCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TypesRepository extends JpaRepository<Types, Long> {
    Optional<Types> findByCategory(TypesCategory category);
}
