package com.swxt.manager.dto.stock;

import lombok.Data;

/**
 * 出入库操作请求：指定商品 ID、数量与备注
 */
@Data
public class StockOperateRequest {

    /** 商品 ID */
    private Long productId;

    /** 操作数量（正整数） */
    private Integer quantity;

    /** 备注 */
    private String remark;
}
