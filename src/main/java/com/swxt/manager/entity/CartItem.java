package com.swxt.manager.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 购物车实体，对应数据库表 cart
 *
 * <p>记录用户加入购物车的商品与数量；user_id + product_id 唯一（重复加入累加数量）。</p>
 */
@Data
public class CartItem {

    /** 购物车行 ID（自增主键） */
    private Long id;

    /** 所属用户 ID（关联 user.id） */
    private Long userId;

    /** 商品 ID（关联 product.id） */
    private Long productId;

    /** 数量 */
    private Integer quantity;

    /** 加入时间 */
    private LocalDateTime createdAt;

    /** 最近更新时间 */
    private LocalDateTime updatedAt;
}
