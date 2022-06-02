package com.junyi;

import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 使用 Jedis 客户端简单操作 Redis
 * @time: 2021/4/15 10:20
 * @version: 1.0
 * @author: junyi Xu
 * @description:
 */
@Slf4j
public class Application {

    @Test
    public void test() {
        log.info("simple use");
        JedisPool jedisPool = new JedisPool("127.0.0.1", 6379);
        Jedis jedis = jedisPool.getResource();
        jedis.set("food:001", "apple");
    }
}
