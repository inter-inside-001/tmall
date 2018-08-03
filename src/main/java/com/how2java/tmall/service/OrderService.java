package com.how2java.tmall.service;
 
import java.util.List;

import com.how2java.tmall.pojo.Order;
import com.how2java.tmall.pojo.OrderItem;

public interface OrderService {

    String waitPay = "waitPay";
    String waitDelivery = "waitDelivery";
    String waitConfirm = "waitConfirm";
    String waitReview = "waitReview";
    String finish = "finish";
    String delete = "delete";

    void add(Order c);
    // 生成订单
    float add(Order c, List<OrderItem> ois);
    // 列出某个状态的订单
    List list(int uid, String excludedStatus);
    void delete(int id);
    void update(Order c);
    Order get(int id);
    List list();
}