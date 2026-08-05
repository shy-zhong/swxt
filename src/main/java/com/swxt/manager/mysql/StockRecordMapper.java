package com.swxt.manager.mysql;

import com.swxt.manager.entity.StockRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 出入库记录 Mapper：插入出入库记录、分页查询历史记录
 */
public interface StockRecordMapper {

    /**
     * 插入一条出入库记录
     */
    @Insert("INSERT INTO stock_record(product_id, type, quantity, operator, remark) " +
            "VALUES(#{productId}, #{type}, #{quantity}, #{operator}, #{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(StockRecord record);

    /**
     * 按商品 ID 查询出入库记录
     */
    @Select("SELECT * FROM stock_record WHERE product_id = #{productId} ORDER BY created_at DESC")
    List<StockRecord> listByProductId(@Param("productId") Long productId);

    /**
     * 查询全部出入库记录（按时间倒序）
     */
    @Select("SELECT * FROM stock_record ORDER BY created_at DESC")
    List<StockRecord> listAll();

    /**
     * 统计总记录数
     */
    @Select("SELECT COUNT(*) FROM stock_record")
    long count();
}
