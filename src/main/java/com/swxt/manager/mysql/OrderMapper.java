package com.swxt.manager.mysql;

import com.swxt.manager.entity.OrderInfo;
import com.swxt.manager.entity.OrderItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 订单 Mapper：创建订单、查询订单列表与订单项明细
 */
public interface OrderMapper {

    /**
     * 插入订单主表
     */
    @Insert("INSERT INTO orders(user_id, username, total_amount, status, remark) " +
            "VALUES(#{userId}, #{username}, #{totalAmount}, #{status}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertOrder(OrderInfo order);

    /**
     * 插入订单项
     */
    @Insert("INSERT INTO order_item(order_id, product_id, product_name, price, quantity) " +
            "VALUES(#{orderId}, #{productId}, #{productName}, #{price}, #{quantity})")
    int insertOrderItem(OrderItem item);

    /**
     * 按 ID 查询订单
     */
    @Select("SELECT * FROM orders WHERE id = #{id}")
    OrderInfo findById(@Param("id") Long id);

    /**
     * 按用户 ID 查询订单列表
     */
    @Select("SELECT * FROM orders WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<OrderInfo> listByUserId(@Param("userId") Long userId);

    /**
     * 查询全部订单（按时间倒序）
     */
    @Select("SELECT * FROM orders ORDER BY created_at DESC")
    List<OrderInfo> listAll();

    /**
     * 按订单 ID 查询订单项列表
     */
    @Select("SELECT * FROM order_item WHERE order_id = #{orderId}")
    List<OrderItem> listItemsByOrderId(@Param("orderId") Long orderId);

    /**
     * 更新订单状态
     */
    @Update("UPDATE orders SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
