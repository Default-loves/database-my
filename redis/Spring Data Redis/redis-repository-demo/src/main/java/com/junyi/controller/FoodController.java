package com.junyi.controller;

import com.junyi.entity.Food;
import com.junyi.service.FoodExampleService;
import com.junyi.service.FoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @time: 2021/1/26 9:49
 * @version: 1.0
 * @author: junyi Xu
 * @description:
 */
@RestController
@RequestMapping("/food")
public class FoodController {

    @Autowired
    FoodService foodService;

    @GetMapping("/getOne")
    public Food getOne(@RequestParam Integer id) {
        return null;
    }

    @GetMapping("/getAll")
    public List<Food> getAll() {
        return foodService.listFoods();
    }

    @PostMapping("/delete")
    public String delete(@RequestBody Food food) {
        foodService.delete(food);
        return "delete success";
    }

    @PostMapping("/add")
    public String add(@RequestBody Food food) {
        foodService.insert(food);
        return "OK";
    }

    @PostMapping("/test")
    public String test(@RequestBody Food food) {
        foodService.testPartialUpdate(food);
        return "OK";
    }
}
