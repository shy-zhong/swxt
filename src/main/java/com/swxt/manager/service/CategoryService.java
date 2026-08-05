package com.swxt.manager.service;

import com.swxt.manager.entity.Category;
import com.swxt.manager.mysql.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 商品分类服务：提供分类的增删改查
 */
@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 查询全部分类
     */
    public List<Category> listAll() {
        return categoryMapper.listAll();
    }

    /**
     * 新增分类，返回是否成功
     */
    public boolean create(Category category) {
        if (category.getParentId() == null) {
            category.setParentId(0L);
        }
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        return categoryMapper.insert(category) > 0;
    }

    /**
     * 更新分类，返回是否成功
     */
    public boolean update(Category category) {
        return categoryMapper.update(category) > 0;
    }

    /**
     * 按 ID 删除分类，返回是否成功
     */
    public boolean delete(Long id) {
        return categoryMapper.deleteById(id) > 0;
    }
}
