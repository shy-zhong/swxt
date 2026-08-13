package com.swxt.manager.mysql;

import com.swxt.manager.dto.cart.CartItemVO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 购物车 Mapper
 */
public interface CartMapper {

    /**
     * 查询用户购物车列表
     */
    @Select("SELECT c.id, c.product_id, p.name, p.price, p.image, c.quantity, p.stock " +
            "FROM cart c JOIN product p ON c.product_id = p.id " +
            "WHERE c.user_id = #{userId} ORDER BY c.id ASC")
    List<CartItemVO> listByUserId(@Param("userId") Long userId);

    /**
     * 查询用户购物车商品种类数
     */
    @Select("SELECT COUNT(*) FROM cart WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);

    /**
     * 加入购物车
     */
    @Insert("INSERT INTO cart(user_id, product_id, quantity) VALUES(#{userId}, #{productId}, #{quantity}) " +
            "ON DUPLICATE KEY UPDATE quantity = quantity + #{quantity}")
    int addItem(@Param("userId") Long userId, @Param("productId") Long productId, @Param("quantity") int quantity);

    /**
     * 修改指定购物车行数量
     */
    @Update("UPDATE cart SET quantity = #{quantity} WHERE id = #{id} AND user_id = #{userId}")
    int updateQuantity(@Param("id") Long id, @Param("userId") Long userId, @Param("quantity") int quantity);

    /**
     * 删除指定购物车行，返回是否删除
     */
    @Delete("DELETE FROM cart WHERE id = #{id} AND user_id = #{userId}")
    int deleteById(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 清空用户购物车
     */
    @Delete("DELETE FROM cart WHERE user_id = #{userId}")
    int clearByUserId(@Param("userId") Long userId);
}
