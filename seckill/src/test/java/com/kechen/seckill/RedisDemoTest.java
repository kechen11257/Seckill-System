package com.kechen.seckill;

import com.kechen.seckill.services.RedisService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import javax.annotation.Resource;

@SpringBootTest
public class RedisDemoTest {
        @Resource
        private RedisService redisService;

        @Test
        public void  stockTest(){
            redisService.setValue("stock:19",10L);
        }

        @Test
        public void getStockTest(){
            String stock =  redisService.getValue("stock:19");
            System.out.println(stock);
        }
}
