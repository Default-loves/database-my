package com.junyi.converter;

import com.junyi.entity.Creator;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.util.Map;

/**
 * @time: 2021/1/27 18:26
 * @version: 1.0
 * @author: junyi Xu
 * @description:
 */
@ReadingConverter
public class MapToCreatorConverter implements Converter<Map<String, byte[]>, Creator> {
    @Override
    public Creator convert(Map<String, byte[]> stringMap) {
        Creator object = new Creator();
        object.setId(Integer.valueOf(new String(stringMap.get("creator_id"))));
        object.setName(new String(stringMap.get("creator_name")));
        return object;
    }
}
