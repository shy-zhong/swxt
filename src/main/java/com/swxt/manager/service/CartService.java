package com.swxt.manager.service;

import com.swxt.manager.config.BusinessException;
import com.swxt.manager.config.Core;
import com.swxt.manager.dto.cart.CartItemVO;
import com.swxt.manager.entity.Product;
import com.swxt.manager.mysql.CartMapper;
import com.swxt.manager.mysql.UserMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 购物车服务：查询、加入、改量、删除与清空
 */
@Service
public class CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private UserMapping userMapping;

    /**
     * 查询用户购物车列表（含商品实时信息）
     */
    public List<CartItemVO> listByUserId(Long userId) {
        return cartMapper.listByUserId(userId);
    }

    /**
     * 查询用户购物车总件数
     */
    public int countByUserId(Long userId) {
        return cartMapper.countByUserId(userId);
    }

    /**
     * 加入购物车：校验商品存在且在售，已存在则累加数量
     */
    public boolean add(Long userId, Long productId, Integer quantity) {
        if (productId == null) {
            throw new BusinessException(Core.ResultCode.BAD_REQUEST, "商品ID不能为空");
        }
        if (quantity == null || quantity <= 0) {
            quantity = 1;
        }
        Product product = userMapping.getProductById(productId);
        if (product == null) {
            throw new BusinessException(Core.ResultCode.NOT_FOUND, "商品不存在");
        }
        if (product.getStatus() != 1) {
            throw new BusinessException(Core.ResultCode.BAD_REQUEST, "商品已下架: " + product.getName());
        }
        return cartMapper.addItem(userId, productId, quantity) > 0;
    }

    /**
     * 修改购物车行数量
     */
    public boolean updateQuantity(Long userId, Long id, Integer quantity) {
        if (id == null) {
            throw new BusinessException(Core.ResultCode.BAD_REQUEST, "购物车项ID不能为空");
        }
        if (quantity == null || quantity <= 0) {
            return cartMapper.deleteById(id, userId) > 0;
        }
        return cartMapper.updateQuantity(id, userId, quantity) > 0;
    }

    /**
     * 删除购物车行（限本人）
     */
    public boolean delete(Long userId, Long id) {
        return cartMapper.deleteById(id, userId) > 0;
    }

    /**
     * 清空用户购物车
     */
    public void clear(Long userId) {
        cartMapper.clearByUserId(userId);
    }
}
