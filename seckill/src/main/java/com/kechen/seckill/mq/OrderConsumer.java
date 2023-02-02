package com.kechen.seckill.mq;

import com.alibaba.fastjson.JSON;
import com.kechen.seckill.db.dao.OrderDao;
import com.kechen.seckill.db.dao.SeckillActivityDao;
import com.kechen.seckill.db.po.Order;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
@RocketMQMessageListener(topic = "seckill_order", consumerGroup = "seckill_order_group")
public class OrderConsumer implements RocketMQListener<MessageExt> {
    @Resource
    private OrderDao orderDao;

    @Resource
    private SeckillActivityDao seckillActivityDao;

    @Override
    @Transactional
    public void onMessage(MessageExt messageExt) {
        //1.Parse create order request message 解析创建订单请求消息
        String message = new String(messageExt.getBody(), StandardCharsets.UTF_8);
        log.info("A request to create an order has been received：" + message);
        // reverse-serialize message
        Order order = JSON.parseObject(message, Order.class);
        order.setCreateTime(new Date());
        //2.deduction of inventory 扣减库存
        boolean lockStockResult = seckillActivityDao.lockStock(order.getSeckillActivityId());
        if (lockStockResult) {
            //Order status 0: No stock available, invalid order 1: Created pending payment
            // 订单状态 0:没有可用库存，无效订单 1:已创建等待付款
            order.setOrderStatus(1);
        } else {
            order.setOrderStatus(0);
        }
        //3.insert order 插入订单
        orderDao.insertOrder(order);
    }
}
