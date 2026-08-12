package com.swxt.manager.controller;

import com.swxt.manager.dto.PageResult;
import com.swxt.manager.dto.Result;
import com.swxt.manager.dto.order.CreateOrderRequest;
import com.swxt.manager.entity.OrderInfo;
import com.swxt.manager.service.CartService;
import com.swxt.manager.service.OrderService;
import com.swxt.manager.Utils.SecurityUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;

    public OrderController(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    /**
     * 创建订单
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER','OPERATOR')")
    public Result<OrderInfo> createOrder(
            @RequestBody CreateOrderRequest request,
            @RequestParam(value = "fromCart") boolean fromCart
    ) {
        Long userId = SecurityUtil.getUserId();
        String username = SecurityUtil.getUsername();
        OrderInfo order = orderService.createOrder(userId, username, request);
        if (fromCart) cartService.clear(userId);
        return Result.success(order);
    }

    /**
     * 分页查询当前用户的订单
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('USER')")
    public Result<PageResult<OrderInfo>> myOrders(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "keyword", required = false) String keyword) {
        Long userId = SecurityUtil.getUserId();
        return Result.success(orderService.listByUserIdPage(userId, page, size, status, keyword));
    }

    /**
     * 查询全部订单
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
     * 查询订单详情
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
            return Result.success();
        }
        return Result.error(404, "订单不存在");
    }
}
