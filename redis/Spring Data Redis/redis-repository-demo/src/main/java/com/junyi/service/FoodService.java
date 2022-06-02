package com.junyi.service;

import com.alibaba.fastjson.JSON;
import com.junyi.entity.Creator;
import com.junyi.entity.Food;
import com.junyi.repository.FoodRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.PartialUpdate;
import org.springframework.data.redis.core.RedisKeyValueTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @time: 2021/1/26 9:46
 * @version: 1.0
 * @author: junyi Xu
 * @description:
 */
@Service
@Slf4j
public class FoodService {

    @Autowired
    FoodRepository foodRepository;
    @Autowired
    RedisTemplate<String, String> redisTemplate;
    @Autowired
    RedisKeyValueTemplate redisKeyValueTemplate;


    public void insert(Food food) {
        foodRepository.save(food);

        Optional<Food> redisFood = foodRepository.findById(String.valueOf(food.getId()));
        redisFood.ifPresent(value -> log.info("Find food: {}", JSON.toJSON(value)));
        log.info("count: {}", foodRepository.count());
    }

    public void delete(Food food) {
        foodRepository.deleteById(String.valueOf(food.getId()));
    }


    public List<Food> listFoods() {
        ArrayList<Food> res = new ArrayList<>();
        for (Food food : foodRepository.findAll()) {
            res.add(food);
        }
        return res;
    }

    /** 支持修改部分属性，而不是全部属性 */
    public void testPartialUpdate(Food food) {
        PartialUpdate<Food> record = new PartialUpdate<Food>(food.getId(), Food.class)
                .refreshTtl(true)
                .set("expiration", 30L)
                .set("name", "milk")
                .set("creator", new Creator(10, "abc"));
        redisKeyValueTemplate.update(record);
    }
}
