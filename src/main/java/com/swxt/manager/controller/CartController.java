package com.swxt.manager.controller;

import com.swxt.manager.Utils.SecurityUtil;
import com.swxt.manager.dto.Result;
import com.swxt.manager.dto.cart.CartItemVO;
import com.swxt.manager.entity.CartItem;
import com.swxt.manager.service.CartService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 购物车
 */
@RestController
@RequestMapping("/cart")
@PreAuthorize("hasAnyRole('USER','OPERATOR')")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * 查询当前用户购物车列表
     */
    @GetMapping
    public Result<List<CartItemVO>> list() {
        return Result.success(cartService.listByUserId(SecurityUtil.getUserId()));
    }

    /**
     * 查询当前用户购物车总件数（角标用）
     */
    @GetMapping("/count")
    public Result<Integer> count() {
        return Result.success(cartService.countByUserId(SecurityUtil.getUserId()));
    }

    /**
     * 加入购物车
     */
    @PostMapping
    public Result<Void> add(@RequestBody CartItem cartItem) {
        cartService.add(SecurityUtil.getUserId(), cartItem.getProductId(), cartItem.getQuantity());
        return Result.success();
    }

    /**
     * 修改数量
     */
    @PutMapping
    public Result<Void> update(@RequestBody CartItem cartItem) {
        cartService.updateQuantity(SecurityUtil.getUserId(), cartItem.getId(), cartItem.getQuantity());
        return Result.success();
    }

    /**
     * 删除购物车行
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean deleted = cartService.delete(SecurityUtil.getUserId(), id);
        if (!deleted) {
            return Result.error(404, "购物车项不存在");
        }
        return Result.success();
    }

    /**
     * 清空当前用户购物车
     */
    @DeleteMapping
    public Result<Void> clear() {
        cartService.clear(SecurityUtil.getUserId());
        return Result.success();
    }
}
