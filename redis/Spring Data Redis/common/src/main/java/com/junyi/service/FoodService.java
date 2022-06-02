package com.junyi.service;

import com.alibaba.fastjson.JSON;
import com.junyi.entity.Food;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

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
    private RedisTemplate<String, String> template;     // 直接注入
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Resource(name="redisTemplate")
    private ListOperations<String, Object> listOps;     // 注入 List 操作类, 当然了也可以注入其他的, 查看 template.opsXXX 可以看到支持的Redis类型

    public Food getOne(Integer id) {
        return Food.getOne();
    }

    public List<Food> listAll() {
        return Collections.singletonList(Food.getOne());
    }

    @Transactional
    public void test(Food food) {
        listOps.leftPush(String.valueOf(food.getId()), JSON.toJSONString(food));
    }

    public void testTemplate() {
        Food apple = Food.builder()
                .id(1)
                .name("apple")
                .build();
        template.opsForSet().add("food", JSON.toJSONString(apple));
    }

    /**
     * 可以使用这种方法来直接与 Redis 交互
     */
    public void useCallback() {
        stringRedisTemplate.execute(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                Long size = connection.dbSize();
                // Can cast to StringRedisConnection if using a StringRedisTemplate
                ((StringRedisConnection)connection).set("key", "value");
                return "OK";
            }
        });
    }
}
