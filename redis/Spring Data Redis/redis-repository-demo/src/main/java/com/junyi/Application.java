package com.junyi;

import com.junyi.config.MyIndexConfiguration;
import com.junyi.config.MyKeyspaceConfiguration;
import com.junyi.service.FoodExampleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

/**
 * Redis  Repository Demo
 * 能够快捷得将对象保存到 Redis，保存的时候不设置 id，Redis会自动生成
 * @time: 2021/1/27 15:13
 * @version: 1.0
 * @author: junyi Xu
 * @description:
 */
@SpringBootApplication
@Slf4j
@EnableRedisRepositories(keyspaceConfiguration = MyKeyspaceConfiguration.class, indexConfiguration = MyIndexConfiguration.class)
public class Application  {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
