package com.junyi;

import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @time: 2022/5/26 9:16
 * @version: 1.0
 * @author: junyi Xu
 * @description:
 */
@Component
@Slf4j
public class A implements ApplicationRunner {

    @Autowired
    RedisTemplate<String, Object> redisTemplate;
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // test1();
        test2();
    }

    private void test2() {
        HashOperations<String, Object, Object> hashOpt = redisTemplate.opsForHash();
        Boolean exist = hashOpt.hasKey("gzTraffic:X11000000026", "accessToken");
        log.info("{}", exist);

    }

    private void test1() {
        ValueOperations<String, Object> valueOps = redisTemplate.opsForValue();
        String key = "tf::1651";

        Object value = valueOps.get(key);
        if (value != null) {
            log.info(value.toString());
        }

        String str = stringRedisTemplate.opsForValue().get(key);

        log.info(str);
    }
}
