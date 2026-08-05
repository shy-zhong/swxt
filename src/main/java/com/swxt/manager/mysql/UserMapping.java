package com.swxt.manager.mysql;

import com.swxt.manager.entity.Product;
import com.swxt.manager.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;


public interface UserMapping {

/**
 * 按用户名查询用户（登录用）
 */
    @Select("SELECT * FROM user WHERE username = #{username} AND deleted = 0")
    User loginByUsername(String username);

    @Select("SELECT * FROM user WHERE id = #{id} AND deleted = 0")
    User loginById(String id);

    @Select("SELECT COUNT(*) FROM user WHERE username = #{username} AND deleted = 0")
    int countByUsername(String username);

    @Select("SELECT * FROM user WHERE deleted = 0")
    List<User> listUsers();

    @Select("SELECT * FROM user WHERE deleted = 0 ORDER BY id ASC LIMIT #{offset}, #{size}")
    List<User> listUsersPage(@Param("offset") int offset, @Param("size") int size);

    @Select("SELECT * FROM product ORDER BY id ASC LIMIT #{offset}, #{size}")
    List<Product> listProductsPage(@Param("offset") int offset, @Param("size") int size);

    @Select("SELECT * FROM product " +
            "WHERE name LIKE CONCAT('%', #{value}, '%') " +
            "OR description LIKE CONCAT('%', #{value}, '%') " +
            "ORDER BY id ASC LIMIT #{offset}, #{size}")
    List<Product> searchProductsPage(@Param("value") String value,
                                     @Param("offset") int offset,
                                     @Param("size") int size);

    @Select("SELECT COUNT(*) FROM product " +
            "WHERE name LIKE CONCAT('%', #{value}, '%') " +
            "OR description LIKE CONCAT('%', #{value}, '%')")
    long countSearchProducts(@Param("value") String value);

/**
 * 按指定字段模糊搜索用户（字段名动态拼接，值使用 LIKE 模糊匹配）
 */
    @Select("SELECT * FROM user " +
            "WHERE ${key} LIKE CONCAT('%', #{value}, '%') AND deleted = 0 " +
            "ORDER BY id ASC LIMIT #{offset}, #{size}")
    List<User> searchUsersPage(@Param("key") String key,
                               @Param("value") String value,
                               @Param("offset") int offset,
                               @Param("size") int size);

    @Select("SELECT COUNT(*) FROM user " +
            "WHERE ${key} LIKE CONCAT('%', #{value}, '%') AND deleted = 0")
    long countSearchUsers(@Param("key") String key, @Param("value") String value);

    @Select("SELECT COUNT(*) FROM user WHERE deleted = 0")
    long countUsers();

/**
 * 统计商品总数
 */
    @Select("SELECT COUNT(*) FROM product")
    long countProducts();

/**
 * 新增用户（密码为 BCrypt 加密后的密文）
 */
    @Insert("INSERT INTO user(username, password, role, real_name, phone, email, wechat_openid, status) " +
            "VALUES(#{username}, #{password}, #{role}, #{realName}, #{phone}, #{email}, #{wechatOpenid}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int createNewUser(User user);

    @Update("UPDATE user SET deleted = 1 WHERE id = #{id} AND deleted = 0")
    int deleteUserById(Long id);


/**
 * 更新用户基本信息、角色与状态（不修改密码）
 */
    @Update("UPDATE user SET username = #{username}, real_name = #{realName}, phone = #{phone}, " +
            "email = #{email}, wechat_openid = #{wechatOpenid}, role = #{role}, status = #{status} " +
            "WHERE id = #{id}")
    int updateUser(User user);

/**
 * 新增商品
 */
    @Insert("INSERT INTO product(name, category_id, price, image, description, stock, status) " +
            "VALUES(#{name}, #{categoryId}, #{price}, #{image}, #{description}, #{stock}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int createProduct(Product product);

/**
 * 按 ID 查询商品
 */
    @Select("SELECT * FROM product WHERE id = #{id}")
    Product getProductById(@Param("id") Long id);




/**
 * 更新商品信息
 */
    @Update("UPDATE product SET name = #{name}, category_id = #{categoryId}, price = #{price}, " +
        "image = #{image}, description = #{description}, stock = #{stock}, status = #{status} " +
        "WHERE id = #{id}")
    int updateProduct(Product product);

/**
 * 按 ID 删除商品
 */
    @Delete("DELETE FROM product WHERE id = #{id}")
    int deleteProductById(Long id);

/**
 * 增减库存：quantity 为正则入库，为负则出库；出库时校验库存不为负
 */
    @Update("UPDATE product SET stock = stock + #{quantity} WHERE id = #{id} AND stock + #{quantity} >= 0")
    int updateStock(@Param("id") Long id, @Param("quantity") int quantity);
}
