package com.swxt.manager.service;

import com.swxt.manager.config.BusinessException;
import com.swxt.manager.config.Core;
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
     *
     * <p>保护规则：分类不存在、存在子分类、被商品引用时拒绝删除，
     * 抛出 BusinessException 由全局异常处理器转为 JSON。</p>
     */
    public boolean delete(Long id) {
        Category existing = categoryMapper.findById(id);
        if (existing == null) {
            throw new BusinessException(Core.ResultCode.NOT_FOUND, "分类不存在");
        }
        if (categoryMapper.countByParentId(id) > 0) {
            throw new BusinessException(Core.ResultCode.BAD_REQUEST, "该分类下存在子分类，请先删除子分类");
        }
        if (categoryMapper.countProductsByCategoryId(id) > 0) {
            throw new BusinessException(Core.ResultCode.BAD_REQUEST, "该分类已被商品引用，无法删除");
        }
        return categoryMapper.deleteById(id) > 0;
    }
}
