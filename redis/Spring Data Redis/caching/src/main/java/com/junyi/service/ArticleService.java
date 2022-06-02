package com.junyi.service;

import com.junyi.mapper.ArticleMapper;
import com.junyi.domain.Article;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.core.AutoConfigureCache;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @time: 2022/5/27 17:36
 * @version: 1.0
 * @author: junyi Xu
 * @description:
 */
@Service
public class ArticleService {

    @Autowired
    ArticleMapper articleMapper;

    @CachePut(value = "article", key = "#p0.id")
    public Article insert(Article article) {
        articleMapper.insert(article);
        return article;
    }

    @CachePut(value = "article", key = "#p0.id")
    public Article update(Article article) {
        articleMapper.update(article);
        return article;
    }

    @CacheEvict(value = "article", key = "#id")
    public void delete(Integer id) {
        articleMapper.delete(id);
    }

    @Cacheable(value = "article", key = "#id")
    public Article findByName(Integer id) {
        return articleMapper.selectByPrimaryKey(id);
    }

    public List<Article> getList() {
        return articleMapper.listAll();
    }
}
