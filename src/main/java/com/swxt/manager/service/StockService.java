package com.swxt.manager.service;

import com.swxt.manager.config.BusinessException;
import com.swxt.manager.config.Core;
import com.swxt.manager.entity.StockRecord;
import com.swxt.manager.mysql.StockRecordMapper;
import com.swxt.manager.mysql.UserMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 出入库服务：处理商品入库、出库逻辑，并写入出入库记录
 */
@Service
public class StockService {

    @Autowired
    private StockRecordMapper stockRecordMapper;

    @Autowired
    private UserMapping userMapping;

    /**
     * 入库：增加商品库存，并写入入库记录
     */
    public boolean stockIn(Long productId, int quantity, String operator, String remark) {
        if (quantity <= 0) {
            throw new BusinessException(Core.ResultCode.BAD_REQUEST, "入库数量必须大于 0");
        }
        int rows = userMapping.updateStock(productId, quantity);
        if (rows == 0) {
            return false;
        }
        StockRecord record = new StockRecord();
        record.setProductId(productId);
        record.setType("IN");
        record.setQuantity(quantity);
        record.setOperator(operator);
        record.setRemark(remark);
        stockRecordMapper.insert(record);
        return true;
    }

    /**
     * 出库：减少商品库存（校验不为负），并写出库记录
     */
    public boolean stockOut(Long productId, int quantity, String operator, String remark) {
        if (quantity <= 0) {
            throw new BusinessException(Core.ResultCode.BAD_REQUEST, "出库数量必须大于 0");
        }
        int rows = userMapping.updateStock(productId, -quantity);
        if (rows == 0) {
            return false;
        }
        StockRecord record = new StockRecord();
        record.setProductId(productId);
        record.setType("OUT");
        record.setQuantity(quantity);
        record.setOperator(operator);
        record.setRemark(remark);
        stockRecordMapper.insert(record);
        return true;
    }

    /**
     * 查询全部出入库记录
     */
    public List<StockRecord> listAll() {
        return stockRecordMapper.listAll();
    }

    /**
     * 按商品 ID 查询出入库记录
     */
    public List<StockRecord> listByProductId(Long productId) {
        return stockRecordMapper.listByProductId(productId);
    }
}
