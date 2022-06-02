package com.junyi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * Redis 使用事务需要进行以下配置
 *
 * @time: 2021/1/27 14:45
 * @version: 1.0
 * @author: junyi Xu
 * @description:
 */
@Configuration
@EnableTransactionManagement
public class RedisTxContextConfiguration {

//    @Bean
//    public StringRedisTemplate redisTemplate() {
//        StringRedisTemplate template = new StringRedisTemplate(redisConnectionFactory());
//        // explicitly enable transaction support
//        template.setEnableTransactionSupport(true);
//        return template;
//    }
//
//    @Bean
//    public RedisConnectionFactory redisConnectionFactory() {
//        RedisStandaloneConfiguration serverConfig = new RedisStandaloneConfiguration();
//
//        return new LettuceConnectionFactory(serverConfig);
//    }
//
//    @Bean
//    public PlatformTransactionManager transactionManager() throws SQLException {
//        return new DataSourceTransactionManager(dataSource());
//    }
//
//    @Bean
//    public DataSource dataSource() throws SQLException {
//        // ...
//        return null;
//    }
}
