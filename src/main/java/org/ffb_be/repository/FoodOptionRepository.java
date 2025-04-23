package org.ffb_be.repository;

import org.ffb_be.entity.FoodOption;
import org.ffb_be.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodOptionRepository extends JpaRepository<FoodOption, Long> {

    List<FoodOption> findFoodOptionsByFood(Product food);

    @Query("SELECT f FROM FoodOption f WHERE f.id IN :ids")
    List<FoodOption> findByIds(List<Long> ids);

}
