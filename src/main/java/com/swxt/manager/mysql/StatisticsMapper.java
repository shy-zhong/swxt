package com.swxt.manager.mysql;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;


public interface StatisticsMapper {

    /**
     * 商品总数
     */
    @Select("SELECT COUNT(*) FROM product")
    long countProducts();

    /**
     * 启用商品数
     */
    @Select("SELECT COUNT(*) FROM product WHERE status = 1")
    long countActiveProducts();

    /**
     * 用户总数
     */
    @Select("SELECT COUNT(*) FROM user WHERE deleted = 0")
    long countUsers();

    @Select("SELECT COUNT(*) FROM user WHERE role = 'ADMIN' AND deleted = 0")
    long countAdminUsers();

    /**
     * 库存总量
     */
    @Select("SELECT IFNULL(SUM(stock), 0) FROM product")
    long sumStock();

    /**
     * 库存价值
     */
    @Select("SELECT IFNULL(SUM(stock * price), 0) FROM product")
    double sumStockValue();

    /**
     * 商品平均价格
     */
    @Select("SELECT IFNULL(AVG(price), 0) FROM product")
    double avgPrice();

    /**
     * 商品最高价格
     */
    @Select("SELECT IFNULL(MAX(price), 0) FROM product")
    double maxPrice();

    /**
     * 商品最低价格
     */
    @Select("SELECT IFNULL(MIN(price), 0) FROM product")
    double minPrice();

    /**
     * 按分类聚合
     */
    @Select("SELECT c.id AS category_id, c.name AS category_name, " +
        "COUNT(p.id) AS count, " +
        "IFNULL(SUM(p.stock), 0) AS stock, " +
        "IFNULL(AVG(p.price), 0) AS avg_price " +
        "FROM category c LEFT JOIN product p ON p.category_id = c.id " +
        "GROUP BY c.id, c.name, c.sort_order " +
        "ORDER BY c.sort_order ASC, c.id ASC")
    List<Map<String, Object>> categoryAgg();

    /**
     * 价格区间聚合
     */
    @Select("SELECT " +
            "SUM(CASE WHEN price >= 0 AND price < 100 THEN 1 ELSE 0 END) AS r1, " +
            "SUM(CASE WHEN price >= 100 AND price < 500 THEN 1 ELSE 0 END) AS r2, " +
            "SUM(CASE WHEN price >= 500 AND price < 1000 THEN 1 ELSE 0 END) AS r3, " +
            "SUM(CASE WHEN price >= 1000 AND price < 3000 THEN 1 ELSE 0 END) AS r4, " +
            "SUM(CASE WHEN price >= 3000 THEN 1 ELSE 0 END) AS r5 " +
            "FROM product")
    Map<String, Object> priceRangeAgg();

    /**
     * 库存流水分页查询
     */
    @Select("SELECT sr.id, sr.product_id AS product_id, p.name AS product_name, " +
            "sr.type, sr.quantity, sr.operator, sr.remark, " +
            "DATE_FORMAT(sr.created_at, '%Y-%m-%d %H:%i:%s') AS created_at " +
            "FROM stock_record sr LEFT JOIN product p ON sr.product_id = p.id " +
            "ORDER BY sr.created_at DESC, sr.id DESC LIMIT #{offset}, #{size}")
    List<Map<String, Object>> stockRecordPage(@Param("offset") int offset, @Param("size") int size);

    /**
     * 库存流水条件查询
     */
    @Select("SELECT sr.id, sr.product_id AS product_id, p.name AS product_name, " +
            "sr.type, sr.quantity, sr.operator, sr.remark, " +
            "DATE_FORMAT(sr.created_at, '%Y-%m-%d %H:%i:%s') AS created_at " +
            "FROM stock_record sr LEFT JOIN product p ON sr.product_id = p.id " +
            "WHERE p.name LIKE CONCAT('%', #{value}, '%') " +
            "OR sr.operator LIKE CONCAT('%', #{value}, '%') " +
            "ORDER BY sr.created_at DESC, sr.id DESC LIMIT #{offset}, #{size}")
    List<Map<String, Object>> searchStockRecordsPage(@Param("value") String value,
                                                     @Param("offset") int offset,
                                                     @Param("size") int size);

    /**
     * 库存流水条件查询总数
     */
    @Select("SELECT COUNT(*) FROM stock_record sr LEFT JOIN product p ON sr.product_id = p.id " +
            "WHERE p.name LIKE CONCAT('%', #{value}, '%') " +
            "OR sr.operator LIKE CONCAT('%', #{value}, '%')")
    long countSearchStockRecords(@Param("value") String value);

    /**
     * 库存流水总数
     */
    @Select("SELECT COUNT(*) FROM stock_record")
    long countStockRecords();
}
