package com.junyi.controller;

import com.junyi.domain.Article;
import com.junyi.service.ArticleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @time: 2022/5/27 17:35
 * @version: 1.0
 * @author: junyi Xu
 * @description:
 */
@RestController
@RequestMapping("article")
@Slf4j
public class ArticleController {

    @Autowired
    ArticleService articleService;

    @PostMapping("/")
    public void insert(@RequestBody Article article) {
        articleService.insert(article);
    }

    @PutMapping("/")
    public void update(@RequestBody Article article) {
        articleService.update(article);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Integer id) {
        articleService.delete(id);
    }

    @GetMapping("/")
    public Article find(@RequestParam("id") Integer id) {
        return articleService.findByName(id);
    }

    @GetMapping("/list")
    public List<Article> list() {
        return articleService.getList();
    }
}
