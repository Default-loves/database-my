package com.junyi;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.junyi.MyApplicationRunner.REDIS_QUEUE;

/**
 * @time: 2022/3/9 18:06
 * @version: 1.0
 * @author: junyi Xu
 * @description:
 */
@RestController
@RequestMapping("test")
@Slf4j
public class MyController {

    @Autowired
    RedissonClient redissonClient;

    @PostMapping("put")
    public void put(@RequestParam("data") String data) {
        String[] splitArray = StrUtil.split(data, ",");
        for (String item : splitArray) {
            sendMessage(item);
        }
    }


    public void sendMessage(String message) {
        RBlockingDeque<String> blockingDeque = redissonClient.getBlockingDeque(REDIS_QUEUE);
        try {
            blockingDeque.putFirst(message);
            log.info("将消息: {} 插入到队列。", message);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
