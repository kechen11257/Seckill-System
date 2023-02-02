package com.kechen.seckill.component;

import com.kechen.seckill.db.dao.SeckillActivityDao;
import com.kechen.seckill.db.po.SeckillActivity;
import com.kechen.seckill.services.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RedisPreheatRunner  implements ApplicationRunner {
    @Autowired
    RedisService redisService;

    // get data from database
    @Autowired
    SeckillActivityDao seckillActivityDao;

    /**
     * 启动项目时 向 Redis 存入 商品库存
     * @param args
     * @throws Exception
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // list all seckill activity
        List<SeckillActivity> seckillActivities = seckillActivityDao.querySeckillActivitysByStatus(1);
        // loop all seckill activity and write into Redis
        for (SeckillActivity seckillActivity : seckillActivities) {
            redisService.setValue("stock:" + seckillActivity.getId(),
                    (long) seckillActivity.getAvailableStock());
        }
    }
}
