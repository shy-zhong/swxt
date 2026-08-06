package com.swxt.manager.controller;

import com.swxt.manager.config.Core;
import com.swxt.manager.dto.PageResult;
import com.swxt.manager.dto.Result;
import com.swxt.manager.dto.order.CreateOrderRequest;
import com.swxt.manager.entity.OrderInfo;
import com.swxt.manager.service.CartService;
import com.swxt.manager.service.OrderService;
import com.swxt.manager.service.SystemLogService;
import com.swxt.manager.Utils.SecurityUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单控制器：用户下单/查看自己的订单，操作员与管理员查看/处理所有订单
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final SystemLogService logService;
    private final CartService cartService;

    public OrderController(OrderService orderService, SystemLogService logService, CartService cartService) {
        this.orderService = orderService;
        this.logService = logService;
        this.cartService = cartService;
    }

    /**
     * 创建订单
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER','OPERATOR')")
    public Result<OrderInfo> createOrder(@RequestBody CreateOrderRequest request) {
        Long userId = SecurityUtil.getUserId();
        String username = SecurityUtil.getUsername();
        OrderInfo order = orderService.createOrder(userId, username, request);
        logService.record(Core.ActionType.CREATE, Core.TargetType.ORDER, order.getId(), Core.LogResult.SUCCESS);
        // 下单成功后清空该用户购物车
        cartService.clear(userId);
        return Result.success(order);
    }

    /**
     * 查询当前用户的订单
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER','OPERATOR')")
    public Result<List<OrderInfo>> myOrders() {
        Long userId = SecurityUtil.getUserId();
        return Result.success(orderService.listByUserId(userId));
    }

    /**
     * 查询全部订单（分页，支持按状态与关键词筛选）
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR','ADMIN')")
    public Result<PageResult<OrderInfo>> listAll(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "keyword", required = false) String keyword) {
        return Result.success(orderService.listAllPage(page, size, status, keyword));
    }

    /**
     * 查询订单详情（操作员与管理员可用，避免普通用户越权查看他人订单）
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERATOR','ADMIN')")
    public Result<OrderInfo> detail(@PathVariable Long id) {
        OrderInfo order = orderService.getOrderDetail(id);
        if (order == null) {
            return Result.error(404, "订单不存在");
        }
        return Result.success(order);
    }

    /**
     * 更新订单状态
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('OPERATOR','ADMIN')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        boolean updated = orderService.updateStatus(id, status);
        if (updated) {
            logService.record(Core.ActionType.UPDATE, Core.TargetType.ORDER, id, Core.LogResult.SUCCESS);
            return Result.success();
        }
        logService.record(Core.ActionType.UPDATE, Core.TargetType.ORDER, id, Core.LogResult.FAIL);
        return Result.error(404, "订单不存在");
    }
}
