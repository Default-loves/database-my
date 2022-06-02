package com.junyi.config;

import org.springframework.data.redis.core.index.IndexConfiguration;
import org.springframework.data.redis.core.index.IndexDefinition;
import org.springframework.data.redis.core.index.SimpleIndexDefinition;

import java.util.Collections;

/**
 * 这个类的效果和直接在 Food 类的 name 字段上直接添加 @Indexed 是一样的
 * 作用是在Redis中额外创建了索引
 * 例如 Key 为 food:name:apple  Value 为 ：1681846526。其中Value为 ID 字段
 * @time: 2021/1/28 10:26
 * @version: 1.0
 * @author: junyi Xu
 * @description:
 */
public class MyIndexConfiguration extends IndexConfiguration {

    @Override
    protected Iterable<IndexDefinition> initialConfiguration() {
        return Collections.singleton(new SimpleIndexDefinition("food", "name"));
    }
}
