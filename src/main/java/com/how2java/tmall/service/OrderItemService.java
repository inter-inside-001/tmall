package com.how2java.tmall.service;
 
import java.util.List;

import com.how2java.tmall.pojo.Order;
import com.how2java.tmall.pojo.OrderItem;

public interface OrderItemService {
     

    void add(OrderItem c);

    void delete(int id);
    void update(OrderItem c);
    OrderItem get(int id);
    List list();

    void fill(List<Order> os);

    void fill(Order o);

    // 根据产品获取销售量
    int getSaleCount(int pid);

    // 获得某个用户的订单项
    List<OrderItem> listByUser(int userId);
}