package com.kechen.seckill.services;

import com.alibaba.fastjson.JSON;
import com.kechen.seckill.db.dao.OrderDao;
import com.kechen.seckill.db.dao.SeckillActivityDao;
import com.kechen.seckill.db.dao.SeckillCommodityDao;
import com.kechen.seckill.db.po.Order;
import com.kechen.seckill.db.po.SeckillActivity;
import com.kechen.seckill.db.po.SeckillCommodity;
import com.kechen.seckill.mq.RocketMQService;
import com.kechen.seckill.util.SnowFlake;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

@Slf4j
@Service
public class SeckillActivityService {

    @Resource
    private RedisService redisService;

    @Resource
    private SeckillActivityDao seckillActivityDao;

    @Resource
    private RocketMQService rocketMQService;
    @Resource
    private OrderDao orderDao;

    @Resource
    SeckillCommodityDao seckillCommodityDao;

    /**
     * datacenterId;  数据中心
     * machineId;     机器标识
     * 在分布式环境中可以从机器配置上读取
     * 单机开发环境中先写死
     */
    private SnowFlake snowFlake = new SnowFlake(1, 1);

    /**
     * 判断秒杀库存
     *
     * @param activityId
     * @return
     */
    public boolean seckillStockValidator(long activityId) {
        String key = "stock:" + activityId;
        return redisService.stockDeductValidator(key);
    }

    /**
     * 创建订单
     *
     * @param seckillActivityId
     * @param userId
     * @return
     * @throws Exception
     */
    public Order createOrder(long seckillActivityId, long userId) throws Exception {
        /*
         * 1.Create Order 创建订单
         */
        SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
        Order order = new Order();
        //采用雪花算法生成订单ID
        order.setOrderNo(String.valueOf(snowFlake.nextId()));
        order.setSeckillActivityId(seckillActivity.getId());
        order.setUserId(userId);
        order.setOrderAmount(seckillActivity.getSeckillPrice().longValue());
        /*
         *2.Send create order message 发送创建订单消息
         */
        rocketMQService.sendMessage("seckill_order", JSON.toJSONString(order));

        /*
         * 3.发送订单付款状态校验消息
         * 开源RocketMQ支持延迟消息，但是不支持秒级精度。默认支持18个level的延迟消息，这是通过broker端的messageDelayLevel配置项确定的，如下：
         * messageDelayLevel=1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h
         * delayTimeLevel:3 对应的是10秒
         */
        rocketMQService.sendDelayMessage("pay_check", JSON.toJSONString(order), 3);

        return order;
    }
    /**
     * 订单支付完成处理
     * @param orderNo

    public void payOrderProcess(String orderNo) {
        log.info("完成支付订单 订单号：" + orderNo);
        Order order = orderDao.queryOrder(orderNo);
        boolean deductStockResult = seckillActivityDao.deductStock(order.getSeckillActivityId());
        if (deductStockResult) {
            order.setPayTime(new Date());
            // 订单状态 0、没有可用库存，无效订单  1、已创建等待支付  2、完成支付
            order.setOrderStatus(2);
            orderDao.updateOrder(order);
        }
    }
     */

    /**
     * 订单支付完成处理
     *
     * @param orderNo
     */
    public void payOrderProcess(String orderNo) throws Exception {
        log.info("Complete the payment order, order number：" + orderNo);
        Order order = orderDao.queryOrder(orderNo);

        /*
         * 1.Determine whether the order exists 判断订单是否存在
         * 2.Determine whether the order status is unpaid 判断订单状态是否为未支付状态
         */
        if (order == null) {
            log.error("The order corresponding to the order number does not exist：" + orderNo);
            return;
        } else if(order.getOrderStatus() != 1 ) {
            log.error("Invalid order status:" + orderNo);
            return;
        }

        /*
         * 2.Order payment completed 订单支付完成
         */
        order.setPayTime(new Date());
        // Order status 0: No stock available, invalid order 1: Created and waiting for payment, 2: Payment completed
        // 订单状态 0:没有可用库存，无效订单 1:已创建等待付款 ,2:支付完成
        order.setOrderStatus(2);
        orderDao.updateOrder(order);
        /*
         *3.Send order payment success message 发送订单付款成功消息
         */
        rocketMQService.sendMessage("pay_done", JSON.toJSONString(order));
    }

    /**
     * 将秒杀详情相关信息倒入redis
     *
     * @param seckillActivityId
     */
    public void pushSeckillInfoToRedis(long seckillActivityId) {
        SeckillActivity seckillActivity = seckillActivityDao.querySeckillActivityById(seckillActivityId);
        redisService.setValue("seckillActivity:" + seckillActivityId, Long.valueOf(JSON.toJSONString(seckillActivity)));

        SeckillCommodity seckillCommodity = seckillCommodityDao.querySeckillCommodityById(seckillActivity.getCommodityId());
        redisService.setValue("seckillCommodity:" + seckillActivity.getCommodityId(), Long.valueOf(JSON.toJSONString(seckillCommodity)));
    }
}


