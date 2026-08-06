package com.swxt.manager.dto.cart;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 购物车展示项：购物车行 + 商品实时信息（名称/价格/图片/库存），供前端列表渲染
 */
@Data
public class CartItemVO {

    /** 购物车行 ID */
    private Long id;

    /** 商品 ID */
    private Long productId;

    /** 商品名称 */
    private String name;

    /** 商品价格（实时取自 product 表） */
    private BigDecimal price;

    /** 商品图片 URL */
    private String image;

    /** 购买数量 */
    private Integer quantity;

    /** 商品实时库存（前端可提示不足） */
    private Integer stock;
}
