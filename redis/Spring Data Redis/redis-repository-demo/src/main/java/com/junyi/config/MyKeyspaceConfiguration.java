package com.junyi.config;

import com.junyi.entity.Food;
import org.springframework.data.redis.core.convert.KeyspaceConfiguration;

import java.util.Collections;

/**
 * 这个类主要是用于设置 Redis Hash类型的Key
 * 这个效果和直接在 Food 类上添加 @RedisHash("food")效果是一样的
 * 不过这个可以统一配置，相比独立配置在实体类上，管理起来更方便
 * @time: 2021/1/28 9:51
 * @version: 1.0
 * @author: junyi Xu
 * @description:
 */
public class MyKeyspaceConfiguration extends KeyspaceConfiguration {

    @Override
    protected Iterable<KeyspaceSettings> initialConfiguration() {
        return Collections.singleton(new KeyspaceSettings(Food.class, "food"));
    }
}
