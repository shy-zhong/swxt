package com.swxt.manager.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单实体，对应数据库表 orders
 *
 * <p>记录用户下单信息，包含订单状态与关联的订单项列表。</p>
 */
@Data
public class OrderInfo {

    /** 订单 ID（自增主键） */
    private Long id;

    /** 下单用户 ID */
    private Long userId;

    /** 下单用户名 */
    private String username;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 订单状态：PENDING=待处理，PROCESSING=处理中，COMPLETED=已完成，CANCELLED=已取消 */
    private String status;

    /** 备注 */
    private String remark;

    /** 收货人 */
    private String receiverName;

    /** 收货电话 */
    private String receiverPhone;

    /** 收货地址 */
    private String receiverAddress;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 关联的订单项列表（联查时填充） */
    private List<OrderItem> items;
}
