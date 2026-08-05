package com.swxt.manager.entity;

import lombok.Data;

/**
 * 商品实体，对应数据库表 product
 *
 * <p>包含商品基本属性：名称、分类、价格、图片、描述、库存与上下架状态。</p>
 */
@Data
public class Product {

    /** 商品 ID（自增主键） */
    private Long id;

    /** 商品名称 */
    private String name;

    /** 所属分类 ID（关联 category.id） */
    private Long categoryId;

    /** 商品价格（精确到分的小数） */
    private java.math.BigDecimal price;

    /** 商品图片 URL */
    private String image;

    /** 商品描述 */
    private String description;

    /** 库存数量 */
    private Integer stock;

    /** 状态：0=禁用/下架，1=启用/在售 */
    private Integer status;
}
