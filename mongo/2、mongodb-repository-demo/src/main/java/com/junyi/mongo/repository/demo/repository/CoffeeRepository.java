package com.junyi.mongo.repository.demo.repository;

import com.junyi.mongo.repository.demo.model.Coffee;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CoffeeRepository extends MongoRepository<Coffee, Long> {
    List<Coffee> findByName(String name);
}
