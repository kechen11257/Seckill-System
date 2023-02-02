package com.kechen.seckill.services;

import com.kechen.seckill.db.dao.SeckillActivityDao;
import com.kechen.seckill.db.po.SeckillActivity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeckillOverSellService {

    @Autowired
    private SeckillActivityDao seckillActivityDao;

    public String  processSeckill(long activityId) {
        // get current seckill activity
        SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(activityId);
        // get available stock of current seckill
        long availableStock = seckillActivity.getAvailableStock();
        String result;
        if (availableStock > 0) {
            result = "Congratulations, the snap up is successful";
            System.out.println(result);
            availableStock = availableStock - 1;
            // update current availableStock of current seckill item
            seckillActivity.setAvailableStock(Integer.valueOf("" + availableStock));
            // update into database
            seckillActivityDao.updateSeckillActivity(seckillActivity);
        } else {
            result = "Sorry, the snap-up failed, the product is sold out";
            System.out.println(result);
        }
        return result;
    }
}
