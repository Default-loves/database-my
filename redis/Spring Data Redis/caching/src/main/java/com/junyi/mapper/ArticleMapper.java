package com.junyi.mapper;


import com.junyi.domain.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleMapper {
    int delete(Integer id);

    int insert(Article record);

    int update(Article record);

    Article selectByPrimaryKey(Integer id);

    Article selectByName(@Param("name") String name);

    List<Article> listAll();

}