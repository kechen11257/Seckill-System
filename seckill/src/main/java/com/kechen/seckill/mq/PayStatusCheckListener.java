package com.kechen.seckill.mq;

import com.alibaba.fastjson.JSON;
import com.kechen.seckill.db.dao.OrderDao;
import com.kechen.seckill.db.dao.SeckillActivityDao;
import com.kechen.seckill.db.po.Order;
import com.kechen.seckill.services.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RocketMQMessageListener(topic = "pay_check", consumerGroup = "pay_check_group")
public class PayStatusCheckListener implements RocketMQListener<MessageExt> {
    @Resource
    private OrderDao orderDao;

    @Resource
    private SeckillActivityDao seckillActivityDao;

    @Resource
    private RedisService redisService;

    @Override
    @Transactional
    public void onMessage(MessageExt messageExt) {
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.info("Receive order payment status verification message:" + message);
        Order order = JSON.parseObject(message, Order.class);

        //1.checking order
        Order orderInfo = orderDao.queryOrder(order.getOrderNo());

        //2.Determine whether the order has been paid 判读订单是否完成支付
        if (orderInfo.getOrderStatus() != 2) {
            //3. 未完成支付关闭订单
            //3. Close the order without completing the payment
            log.info("Unfinished payment close order, order number：" + orderInfo.getOrderNo());
            orderInfo.setOrderStatus(99);
            orderDao.updateOrder(orderInfo);

            //4.restore database inventory 恢复数据库库存
            seckillActivityDao.revertStock(order.getSeckillActivityId());
            // restore redis inventory 恢复 redis 库存
            redisService.revertStock("stock:" + order.getSeckillActivityId());
            //5.Remove user from purchased list 将用户从已购名单中移除
            redisService.removeLimitMember(order.getSeckillActivityId(), order.getUserId());
        }
    }
}
