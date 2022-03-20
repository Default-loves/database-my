package com.junyi;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @time: 2022/3/9 18:01
 * @version: 1.0
 * @author: junyi Xu
 * @description:
 */
@Component
@Slf4j
public class MyApplicationRunner implements ApplicationRunner {

    @Autowired
    RedissonClient redissonClient;

    public static final String REDIS_QUEUE = "my-queue";


    @Override
    public void run(ApplicationArguments args) throws Exception {
        new Thread(this::onMessage).start();
    }




    /**
     * 从队列尾部阻塞读取消息，若没有消息，线程就会阻塞等待新消息插入，防止 CPU 空转
     */
    public void onMessage() {
        RBlockingDeque<String> blockingDeque = redissonClient.getBlockingDeque(REDIS_QUEUE);
        log.info("开始监听");
        while (true) {
            try {
                String message = blockingDeque.takeLast();
                log.info("从队列 {} 中读取到消息：{}.", REDIS_QUEUE, message);
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
