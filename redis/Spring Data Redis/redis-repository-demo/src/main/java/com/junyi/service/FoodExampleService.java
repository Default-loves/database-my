package com.junyi.service;

import com.alibaba.fastjson.JSON;
import com.junyi.entity.Food;
import com.junyi.repository.FoodExampleRepository;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @time: 2021/1/28 14:13
 * @version: 1.0
 * @author: junyi Xu
 * @description:
 */
@Slf4j
@Service
public class FoodExampleService {

//    @Resource
//    FoodExampleRepository foodExampleRepository;
////
//    public void testExample() {
//        Food food = Food.builder().name("apple").build();
//        Iterable<Food> all = foodExampleRepository.findAll(Example.of(food));
//        for (Food item : all) {
//            log.info("Find food: {}", JSON.toJSON(item));
//        }
//    }
}
