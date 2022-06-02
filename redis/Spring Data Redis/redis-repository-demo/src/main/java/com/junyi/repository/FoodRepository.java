package com.junyi.repository;

import com.junyi.entity.Food;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * @time: 2021/1/27 15:19
 * @version: 1.0
 * @author: junyi Xu
 * @description:
 */
public interface FoodRepository extends CrudRepository<Food, String> {
}
