package com.swxt.manager.mysql;

import com.swxt.manager.entity.Product;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;


/**
 * 商品 Mapper：商品分页/搜索/筛选、增删改查与库存增减
 */
public interface ProductMapper {

/**
 * 商品分页查询（含分类名称），按 ID 升序
 */
    @Select("SELECT p.*, c.name AS category_name FROM product p " +
            "LEFT JOIN category c ON p.category_id = c.id " +
            "ORDER BY p.id ASC LIMIT #{offset}, #{size}")
    List<Product> listProductsPage(@Param("offset") int offset, @Param("size") int size);

/**
 * 按关键词搜索商品（名称/描述模糊）
 */
    @Select("SELECT p.*, c.name AS category_name FROM product p " +
            "LEFT JOIN category c ON p.category_id = c.id " +
            "WHERE p.name LIKE CONCAT('%', #{value}, '%') " +
            "OR p.description LIKE CONCAT('%', #{value}, '%') " +
            "ORDER BY p.id ASC LIMIT #{offset}, #{size}")
    List<Product> searchProductsPage(@Param("value") String value,
                                     @Param("offset") int offset,
                                     @Param("size") int size);

    @Select("SELECT COUNT(*) FROM product " +
            "WHERE name LIKE CONCAT('%', #{value}, '%') " +
            "OR description LIKE CONCAT('%', #{value}, '%')")
    long countSearchProducts(@Param("value") String value);

    /**
     * 综合筛选商品：keyword（名称模糊）、categoryId（精确）、priceMin/priceMax（价格区间）、status（状态），动态拼接
     */
    @Select("<script>" +
            "SELECT p.*, c.name AS category_name FROM product p " +
            "LEFT JOIN category c ON p.category_id = c.id WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'> AND p.name LIKE CONCAT('%', #{keyword}, '%') </if>" +
            "<if test='categoryId != null'> AND p.category_id = #{categoryId} </if>" +
            "<if test='priceMin != null'> AND p.price &gt;= #{priceMin} </if>" +
            "<if test='priceMax != null'> AND p.price &lt;= #{priceMax} </if>" +
            "<if test='status != null'> AND p.status = #{status} </if>" +
            " ORDER BY p.id ASC LIMIT #{offset}, #{size}" +
            "</script>")
    List<Product> listProductsFilterPage(@Param("keyword") String keyword,
                                         @Param("categoryId") Long categoryId,
                                         @Param("priceMin") BigDecimal priceMin,
                                         @Param("priceMax") BigDecimal priceMax,
                                         @Param("status") Integer status,
                                         @Param("offset") int offset,
                                         @Param("size") int size);

    /**
     * 综合筛选商品总数（与 listProductsFilterPage 同条件）
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM product p WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'> AND p.name LIKE CONCAT('%', #{keyword}, '%') </if>" +
            "<if test='categoryId != null'> AND p.category_id = #{categoryId} </if>" +
            "<if test='priceMin != null'> AND p.price &gt;= #{priceMin} </if>" +
            "<if test='priceMax != null'> AND p.price &lt;= #{priceMax} </if>" +
            "<if test='status != null'> AND p.status = #{status} </if>" +
            "</script>")
    long countProductsFilter(@Param("keyword") String keyword,
                             @Param("categoryId") Long categoryId,
                             @Param("priceMin") BigDecimal priceMin,
                             @Param("priceMax") BigDecimal priceMax,
                             @Param("status") Integer status);

/**
 * 统计商品总数
 */
    @Select("SELECT COUNT(*) FROM product")
    long countProducts();

/**
 * 新增商品
 */
    @Insert("INSERT INTO product(name, category_id, price, image, description, stock, status) " +
            "VALUES(#{name}, #{categoryId}, #{price}, #{image}, #{description}, #{stock}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int createProduct(Product product);

/**
 * 按 ID 查询商品（含分类名称）
 */
    @Select("SELECT p.*, c.name AS category_name FROM product p " +
            "LEFT JOIN category c ON p.category_id = c.id WHERE p.id = #{id}")
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
 * 增减库存
 */
    @Update("UPDATE product SET stock = stock + #{quantity} WHERE id = #{id} AND stock + #{quantity} >= 0")
    int updateStock(@Param("id") Long id, @Param("quantity") int quantity);
}
