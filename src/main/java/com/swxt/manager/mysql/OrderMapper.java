package com.swxt.manager.mysql;

import com.swxt.manager.entity.OrderInfo;
import com.swxt.manager.entity.OrderItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

public interface OrderMapper {

    /**
     * 创建订单主表
     */
    @Insert("INSERT INTO orders(user_id, username, total_amount, status, remark, receiver_name, receiver_phone, receiver_address) " +
            "VALUES(#{userId}, #{username}, #{totalAmount}, #{status}, #{remark}, #{receiverName}, #{receiverPhone}, #{receiverAddress})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int createOrder(OrderInfo order);

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
     * 按用户 ID 分页查询订单
     */
    @Select("<script>SELECT * FROM orders WHERE user_id = #{userId}" +
            "<if test='status != null and status != \"\"'> AND status = #{status}</if>" +
            "<if test='keyword != null and keyword != \"\"'> AND CAST(id AS CHAR) LIKE CONCAT('%', #{keyword}, '%')</if>" +
            " ORDER BY created_at DESC LIMIT #{offset}, #{size}</script>")
    List<OrderInfo> listByUserIdPage(@Param("userId") Long userId, @Param("offset") int offset,
                                     @Param("size") int size, @Param("status") String status,
                                     @Param("keyword") String keyword);

    /**
     * 统计用户订单总数
     */
    @Select("<script>SELECT COUNT(*) FROM orders WHERE user_id = #{userId}" +
            "<if test='status != null and status != \"\"'> AND status = #{status}</if>" +
            "<if test='keyword != null and keyword != \"\"'> AND CAST(id AS CHAR) LIKE CONCAT('%', #{keyword}, '%')</if>" +
            "</script>")
    long countByUserId(@Param("userId") Long userId, @Param("status") String status,
                       @Param("keyword") String keyword);

    /**
     * 查询全部订单
     */
    @Select("SELECT * FROM orders ORDER BY created_at DESC")
    List<OrderInfo> listAll();

    /**
     * 分页查询全部订单
     */
    @Select("<script>SELECT * FROM orders WHERE 1=1" +
            "<if test='status != null and status != \"\"'> AND status = #{status}</if>" +
            "<if test='keyword != null and keyword != \"\"'> AND (username LIKE CONCAT('%', #{keyword}, '%') OR CAST(id AS CHAR) LIKE CONCAT('%', #{keyword}, '%'))</if>" +
            " ORDER BY created_at DESC LIMIT #{offset}, #{size}</script>")
    List<OrderInfo> listAllPage(@Param("offset") int offset, @Param("size") int size,
                                @Param("status") String status, @Param("keyword") String keyword);

    /**
     * 统计订单总数
     */
    @Select("<script>SELECT COUNT(*) FROM orders WHERE 1=1" +
            "<if test='status != null and status != \"\"'> AND status = #{status}</if>" +
            "<if test='keyword != null and keyword != \"\"'> AND (username LIKE CONCAT('%', #{keyword}, '%') OR CAST(id AS CHAR) LIKE CONCAT('%', #{keyword}, '%'))</if>" +
            "</script>")
    long countAll(@Param("status") String status, @Param("keyword") String keyword);

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
