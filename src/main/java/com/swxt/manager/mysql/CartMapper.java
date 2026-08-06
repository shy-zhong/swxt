package com.swxt.manager.mysql;

import com.swxt.manager.dto.cart.CartItemVO;
import com.swxt.manager.entity.CartItem;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 购物车 Mapper：查询/加入/改量/删除/清空
 */
public interface CartMapper {

    /**
     * 查询用户购物车列表（join 商品表取名称/价格/图片/库存）
     */
    @Select("SELECT c.id, c.product_id, p.name, p.price, p.image, c.quantity, p.stock " +
            "FROM cart c JOIN product p ON c.product_id = p.id " +
            "WHERE c.user_id = #{userId} ORDER BY c.id ASC")
    List<CartItemVO> listByUserId(@Param("userId") Long userId);

    /**
     * 查询用户购物车总件数（角标用）
     */
    @Select("SELECT COALESCE(SUM(quantity), 0) FROM cart WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);

    /**
     * 加入购物车：已存在（user_id + product_id 唯一）则数量累加
     */
    @Insert("INSERT INTO cart(user_id, product_id, quantity) VALUES(#{userId}, #{productId}, #{quantity}) " +
            "ON DUPLICATE KEY UPDATE quantity = quantity + #{quantity}")
    int addItem(@Param("userId") Long userId, @Param("productId") Long productId, @Param("quantity") int quantity);

    /**
     * 修改指定购物车行数量（限本人）
     */
    @Update("UPDATE cart SET quantity = #{quantity} WHERE id = #{id} AND user_id = #{userId}")
    int updateQuantity(@Param("id") Long id, @Param("userId") Long userId, @Param("quantity") int quantity);

    /**
     * 删除指定购物车行（限本人），返回是否删除
     */
    @Delete("DELETE FROM cart WHERE id = #{id} AND user_id = #{userId}")
    int deleteById(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 清空用户购物车
     */
    @Delete("DELETE FROM cart WHERE user_id = #{userId}")
    int clearByUserId(@Param("userId") Long userId);
}
