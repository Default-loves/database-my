package com.junyi.entity;

import lombok.Builder;
import lombok.Data;

/**
 * @time: 2021/1/26 9:45
 * @version: 1.0
 * @author: junyi Xu
 * @description:
 */
@Data
@Builder
public class Food {
    private Integer id;
    private String name;
    private Integer count;


    public static Food getOne() {
        return Food.builder()
                .id(1)
                .name("apple")
                .count(99)
                .build();
    }
}
