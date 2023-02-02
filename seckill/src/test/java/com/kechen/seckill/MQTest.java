package com.kechen.seckill;

import com.kechen.seckill.mq.RocketMQService;
import com.kechen.seckill.services.SeckillActivityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest
public class MQTest {

    @Autowired
    RocketMQService rocketMQService;

    @Autowired
    SeckillActivityService seckillActivityService;

    @Test
    public void sendMQTest() throws Exception {
        rocketMQService.sendMessage("test-kechen", "Hello World!" + new Date().toString());
    }
}
