package com.junyi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.junyi.entity.Creator;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

/**
 * 使用这个 Converter之前是这样的：
 * "_class"："com.junyi.entity.Food"
 * "id"："-1860065803"
 * "name"："apple"
 * "count"："10"
 * "creator.id"："1"
 * "creator.name"： "a"
 *
 * 使用完后是这样的：
 * "_class"："com.junyi.entity.Food"
 * "id"："-1860065803"
 * "name"："apple"
 * "count"："10"
 * "creator"：{
 *   "id": 1,
 *   "name": "d"
 * }
 */

/**
 *
 * @time: 2021/1/27 17:58
 * @version: 1.0
 * @author: junyi Xu
 * @description:
 */
@WritingConverter
public class CreatorToByteConverter implements Converter<Creator, byte[]> {

    private Jackson2JsonRedisSerializer<Creator> serializer;

    public CreatorToByteConverter() {
        serializer = new Jackson2JsonRedisSerializer<Creator>(Creator.class);
        serializer.setObjectMapper(new ObjectMapper());
    }

    @Override
    public byte[] convert(Creator creator) {
        return serializer.serialize(creator);
    }
}
