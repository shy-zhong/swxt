package com.swxt.manager.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 出入库记录实体，对应数据库表 stock_record
 *
 * <p>记录每次商品入库(IN)或出库(OUT)操作，含操作人、数量与备注。</p>
 */
@Data
public class StockRecord {

    /** 记录 ID（自增主键） */
    private Long id;

    /** 商品 ID（关联 product.id） */
    private Long productId;

    /** 操作类型：IN=入库，OUT=出库 */
    private String type;

    /** 操作数量 */
    private Integer quantity;

    /** 操作人用户名 */
    private String operator;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    private LocalDateTime createdAt;
}
