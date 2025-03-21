package org.ffb_be.repository;

import org.ffb_be.entity.FoodOption;
import org.ffb_be.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FoodOptionRepository extends JpaRepository<FoodOption, Long> {

    List<FoodOption> findFoodOptionsByFood(Product food);

}
