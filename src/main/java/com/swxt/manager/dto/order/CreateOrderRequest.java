package com.swxt.manager.dto.order;

import lombok.Data;

import java.util.List;

/**
 * 创建订单请求：包含订单项列表与备注
 */
@Data
public class CreateOrderRequest {

    /** 订单项列表 */
    private List<OrderItemRequest> items;

    /** 备注 */
    private String remark;

    /**
     * 订单项请求：商品 ID 与购买数量
     */
    @Data
    public static class OrderItemRequest {
        /** 商品 ID */
        private Long productId;
        /** 购买数量 */
        private Integer quantity;
    }
}
