package com.swxt.manager.mysql;

import com.swxt.manager.entity.Category;
import org.apache.ibatis.annotations.*;

import java.util.List;


public interface CategoryMapper {

/**
 * 查询全部分类，按排序号、ID 升序排列
 */
    @Select("SELECT * FROM category ORDER BY sort_order ASC, id ASC")
    List<Category> listAll();

/**
 * 按 ID 查询单个分类
 */
    @Select("SELECT * FROM category WHERE id = #{id}")
    Category findById(Long id);

/**
 * 新增分类
 */
    @Insert("INSERT INTO category(name, parent_id, sort_order) VALUES(#{name}, #{parentId}, #{sortOrder})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Category category);

/**
 * 更新分类信息
 */
    @Update("UPDATE category SET name = #{name}, parent_id = #{parentId}, sort_order = #{sortOrder} WHERE id = #{id}")
    int update(Category category);

/**
 * 统计指定父分类下的子分类数量（任意层级直接子级）
 */
    @Select("SELECT COUNT(*) FROM category WHERE parent_id = #{parentId}")
    long countByParentId(Long parentId);

/**
 * 统计被商品引用的数量（product.category_id 指向该分类）
 */
    @Select("SELECT COUNT(*) FROM product WHERE category_id = #{categoryId}")
    long countProductsByCategoryId(Long categoryId);

/**
 * 按 ID 删除分类
 */
    @Delete("DELETE FROM category WHERE id = #{id}")
    int deleteById(Long id);
}
