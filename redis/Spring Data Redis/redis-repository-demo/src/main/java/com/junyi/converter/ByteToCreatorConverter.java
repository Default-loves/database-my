package com.junyi.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.junyi.entity.Creator;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

/**
 * @time: 2021/1/27 18:02
 * @version: 1.0
 * @author: junyi Xu
 * @description:
 */
@ReadingConverter
public class ByteToCreatorConverter implements Converter<byte[], Creator> {

    private Jackson2JsonRedisSerializer<Creator> serializer;

    public ByteToCreatorConverter() {
        serializer = new Jackson2JsonRedisSerializer<>(Creator.class);
        serializer.setObjectMapper(new ObjectMapper());
    }

    @Override
    public Creator convert(byte[] bytes) {
        return serializer.deserialize(bytes);
    }
}
