package com.junyi.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

/**
 * @time: 2021/1/26 9:45
 * @version: 1.0
 * @author: junyi Xu
 * @description:
 */
@Data
@Builder
@RedisHash
public class Food {
    @Id private Integer id;
//    @Indexed
    private String name;
    private Integer count;
    private Creator creator;
    @TimeToLive private long expiration;    // 添加了这个注解，会将字段自动应用到 Redis 的 TTL中，单位为秒
}
