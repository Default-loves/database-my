package com.junyi.repository;

import com.junyi.entity.Food;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import org.springframework.stereotype.Repository;

/**
 * TODO
 * 有问题，无法在 service 中注入这个 Bean
 * @time: 2021/1/28 14:52
 * @version: 1.0
 * @author: junyi Xu
 * @description:
 */
@Repository
public interface FoodExampleRepository extends QueryByExampleExecutor<Food> {
}
