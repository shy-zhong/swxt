package com.swxt.manager.dto.statistics;

import lombok.Data;

import java.util.List;

/**
 * 统计面板返回对象
 *
 * 聚合汇总指标、分类统计、价格区间统计与库存流水，供前端统计页面展示。
 */
@Data
public class StatisticsVO {

    /** 总体汇总指标 */
    private Summary summary;

    /** 按商品分类统计 */
    private List<CategoryStat> categoryStats;

    /** 按价格区间统计 */
    private List<PriceRangeStat> priceRangeStats;

    /** 汇总指标：商品、用户、库存、价格等维度 */
    @Data
    public static class Summary {
        private long totalProducts;      // 商品总数
        private long activeProducts;     // 在售商品数
        private long inactiveProducts;   // 下架商品数
        private long totalUsers;         // 用户总数
        private long adminUsers;         // 管理员数
        private long normalUsers;        // 普通用户数
        private long totalStock;         // 总库存
        private double stockValue;       // 库存货值（数量×价格）
        private double avgPrice;         // 平均价格
        private double maxPrice;         // 最高价格
        private double minPrice;         // 最低价格
    }

    /** 分类维度统计 */
    @Data
    public static class CategoryStat {
        private Long categoryId;         // 分类 ID
        private String categoryName;     // 分类名称
        private long count;              // 该分类商品数
        private long stock;              // 该分类总库存
        private double avgPrice;         // 该分类平均价
        private double percent;          // 数量占比（%）
    }

    /** 价格区间统计 */
    @Data
    public static class PriceRangeStat {
        private String label;            // 区间展示名，如 "0-100 元"
        private String rangeKey;         // 区间键，如 "0-100"
        private long count;              // 该区间商品数
        private double percent;          // 占比（%）
    }

    /** 库存流水记录项 */
    @Data
    public static class StockRecordItem {
        private Long id;                 // 流水 ID
        private Long productId;          // 商品 ID
        private String productName;      // 商品名称
        private String type;             // 类型：IN=入库，OUT=出库
        private int quantity;            // 变动数量
        private String operator;         // 操作人
        private String remark;           // 备注
        private String createdAt;        // 创建时间
    }
}
