package com.swxt.manager.service;

import com.swxt.manager.config.BusinessException;
import com.swxt.manager.config.Core;
import com.swxt.manager.dto.PageResult;
import com.swxt.manager.dto.order.CreateOrderRequest;
import com.swxt.manager.entity.OrderInfo;
import com.swxt.manager.entity.OrderItem;
import com.swxt.manager.entity.Product;
import com.swxt.manager.mysql.OrderMapper;
import com.swxt.manager.mysql.UserMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单服务：创建订单（扣减库存）、查询订单与更新状态
 */
@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapping userMapping;

    /**
     * 创建订单：校验商品与库存，写入订单主表与订单项（不再自动扣减库存，由操作员在出入库管理中按订单出库）
     */
    @Transactional
    public OrderInfo createOrder(Long userId, String username, CreateOrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException(Core.ResultCode.BAD_REQUEST, "订单项不能为空");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (CreateOrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Product product = userMapping.getProductById(itemReq.getProductId());
            if (product == null) {
                throw new BusinessException(Core.ResultCode.NOT_FOUND, "商品不存在: " + itemReq.getProductId());
            }
            if (product.getStatus() != 1) {
                throw new BusinessException(Core.ResultCode.BAD_REQUEST, "商品已下架: " + product.getName());
            }
            if (product.getStock() < itemReq.getQuantity()) {
                throw new BusinessException(Core.ResultCode.BAD_REQUEST, "库存不足: " + product.getName());
            }

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setPrice(product.getPrice());
            item.setQuantity(itemReq.getQuantity());
            items.add(item);

            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }

        OrderInfo order = new OrderInfo();
        order.setUserId(userId);
        order.setUsername(username);
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING");
        order.setRemark(request.getRemark());
        orderMapper.insertOrder(order);

        for (OrderItem item : items) {
            item.setOrderId(order.getId());
            orderMapper.insertOrderItem(item);
        }

        order.setItems(items);
        return order;
    }

    /**
     * 按用户 ID 查询订单列表（含订单项）
     */
    public List<OrderInfo> listByUserId(Long userId) {
        List<OrderInfo> orders = orderMapper.listByUserId(userId);
        for (OrderInfo order : orders) {
            order.setItems(orderMapper.listItemsByOrderId(order.getId()));
        }
        return orders;
    }

    /**
     * 查询全部订单（含订单项）
     */
    public List<OrderInfo> listAll() {
        List<OrderInfo> orders = orderMapper.listAll();
        for (OrderInfo order : orders) {
            order.setItems(orderMapper.listItemsByOrderId(order.getId()));
        }
        return orders;
    }

    /**
     * 分页查询全部订单（含订单项，支持按状态与关键词筛选）
     */
    public PageResult<OrderInfo> listAllPage(int page, int size, String status, String keyword) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        int offset = (page - 1) * size;
        List<OrderInfo> orders = orderMapper.listAllPage(offset, size, status, keyword);
        for (OrderInfo order : orders) {
            order.setItems(orderMapper.listItemsByOrderId(order.getId()));
        }
        long total = orderMapper.countAll(status, keyword);
        return new PageResult<>(orders, total, page, size);
    }

    /**
     * 查询订单详情（含订单项）
     */
    public OrderInfo getOrderDetail(Long id) {
        OrderInfo order = orderMapper.findById(id);
        if (order != null) {
            order.setItems(orderMapper.listItemsByOrderId(id));
        }
        return order;
    }

    /**
     * 更新订单状态，返回是否成功
     */
    public boolean updateStatus(Long id, String status) {
        return orderMapper.updateStatus(id, status) > 0;
    }
}
