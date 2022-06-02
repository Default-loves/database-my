package com.junyi.converter;

import com.junyi.entity.Creator;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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
 * "creator_id"："1"
 * "creator_name": "a"
 */

/**
 * @time: 2021/1/27 18:23
 * @version: 1.0
 * @author: junyi Xu
 * @description:
 */
@WritingConverter
public class CreatorToMapConverter implements Converter<Creator, Map<String, byte[]>> {
    @Override
    public Map<String, byte[]> convert(Creator creator) {
        HashMap<String, byte[]> map = new HashMap<>();
        map.put("creator_id", creator.getId().toString().getBytes(StandardCharsets.UTF_8));
        map.put("creator_name", creator.getName().getBytes(StandardCharsets.UTF_8));
        return map;
    }
}
