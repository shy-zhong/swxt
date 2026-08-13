package com.swxt.manager.service;

import com.swxt.manager.config.BusinessException;
import com.swxt.manager.config.Core;
import com.swxt.manager.entity.OrderInfo;
import com.swxt.manager.entity.OrderItem;
import com.swxt.manager.entity.StockRecord;
import com.swxt.manager.mysql.OrderMapper;
import com.swxt.manager.mysql.ProductMapper;
import com.swxt.manager.mysql.StockRecordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 出入库服务：处理商品入库、出库逻辑，并写入出入库记录
 */
@Service
public class StockService {

    @Autowired
    private StockRecordMapper stockRecordMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 入库：增加商品库存，并写入入库记录
     */
    public boolean stockIn(Long productId, int quantity, String operator, String remark) {
        if (quantity <= 0) {
            throw new BusinessException(Core.ResultCode.BAD_REQUEST, "入库数量必须大于 0");
        }
        int rows = productMapper.updateStock(productId, quantity);
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
        int rows = productMapper.updateStock(productId, -quantity);
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

    /**
     * 根据订单出库：库存已在创建订单时扣减，此处仅写出库记录，订单状态置为已完成
     */
    @Transactional
    public boolean stockOutByOrder(Long orderId, String operator) {
        OrderInfo order = orderMapper.findById(orderId);
        if (order == null) {
            throw new BusinessException(Core.ResultCode.NOT_FOUND, "订单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException(Core.ResultCode.BAD_REQUEST, "订单状态不允许出库，当前状态: " + order.getStatus());
        }
        List<OrderItem> items = orderMapper.listItemsByOrderId(orderId);
        if (items == null || items.isEmpty()) {
            throw new BusinessException(Core.ResultCode.BAD_REQUEST, "订单项为空");
        }
        for (OrderItem item : items) {
            StockRecord record = new StockRecord();
            record.setProductId(item.getProductId());
            record.setType("OUT");
            record.setQuantity(item.getQuantity());
            record.setOperator(operator);
            record.setRemark("订单出库，订单号: " + orderId);
            stockRecordMapper.insert(record);
        }
        orderMapper.updateStatus(orderId, "COMPLETED");
        return true;
    }

    /**
     * 根据订单退货入库：遍历订单项退回库存并写入库记录，订单状态置为已取消
     */
    @Transactional
    public boolean stockInByOrder(Long orderId, String operator) {
        OrderInfo order = orderMapper.findById(orderId);
        if (order == null) {
            throw new BusinessException(Core.ResultCode.NOT_FOUND, "订单不存在");
        }
        if (!"COMPLETED".equals(order.getStatus())) {
            throw new BusinessException(Core.ResultCode.BAD_REQUEST, "订单状态不允许退货入库，当前状态: " + order.getStatus());
        }
        List<OrderItem> items = orderMapper.listItemsByOrderId(orderId);
        if (items == null || items.isEmpty()) {
            throw new BusinessException(Core.ResultCode.BAD_REQUEST, "订单项为空");
        }
        for (OrderItem item : items) {
            int rows = productMapper.updateStock(item.getProductId(), item.getQuantity());
            if (rows == 0) {
                throw new BusinessException(Core.ResultCode.BAD_REQUEST, "退货入库失败: " + item.getProductName());
            }
            StockRecord record = new StockRecord();
            record.setProductId(item.getProductId());
            record.setType("IN");
            record.setQuantity(item.getQuantity());
            record.setOperator(operator);
            record.setRemark("订单退货入库，订单号: " + orderId);
            stockRecordMapper.insert(record);
        }
        orderMapper.updateStatus(orderId, "CANCELLED");
        return true;
    }
}
