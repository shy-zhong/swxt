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
 * 查询指定父分类下的直接子分类 ID 列表
 */
    @Select("SELECT id FROM category WHERE parent_id = #{parentId}")
    List<Long> findIdsByParentId(Long parentId);

/**
 * 统计多个分类中被商品引用的数量
 */
    @Select("<script>" +
            "SELECT COUNT(*) FROM product WHERE category_id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    long countProductsByCategoryIds(@Param("ids") List<Long> ids);

/**
 * 批量删除多个分类
 */
    @Delete("<script>" +
            "DELETE FROM category WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    int deleteByIds(@Param("ids") List<Long> ids);
}
