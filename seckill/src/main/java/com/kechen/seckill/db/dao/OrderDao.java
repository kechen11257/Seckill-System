package com.kechen.seckill.db.dao;

import com.kechen.seckill.db.po.Order;

public interface OrderDao {

    void insertOrder(Order order);
    Order queryOrder(String orderNo);
    void updateOrder(Order order);
}
