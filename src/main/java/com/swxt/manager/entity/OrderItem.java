package com.swxt.manager.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单项实体，对应数据库表 order_item
 *
 * <p>记录订单中每个商品的购买明细：商品名、单价与数量。</p>
 */
@Data
public class OrderItem {

    /** 订单项 ID（自增主键） */
    private Long id;

    /** 订单 ID（关联 orders.id） */
    private Long orderId;

    /** 商品 ID（关联 product.id） */
    private Long productId;

    /** 商品名称（下单时快照） */
    private String productName;

    /** 购买单价（下单时快照） */
    private BigDecimal price;

    /** 购买数量 */
    private Integer quantity;
}
