package com.swxt.manager.service;

import com.swxt.manager.config.BusinessException;
import com.swxt.manager.config.Core;
import com.swxt.manager.entity.Category;
import com.swxt.manager.mysql.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
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
     * 按 ID 删除分类，级联删除其全部子孙分类，返回是否成功
     *
     * <p>规则：分类不存在时抛 404；自身或任一子孙被商品引用时拒绝删除；
     * 通过校验后连同全部子孙分类一并删除，事务保证原子性。</p>
     */
    @Transactional
    public boolean delete(Long id) {
        Category existing = categoryMapper.findById(id);
        if (existing == null) {
            throw new BusinessException(Core.ResultCode.NOT_FOUND, "分类不存在");
        }
        List<Long> ids = collectIdsWithDescendants(id);
        if (categoryMapper.countProductsByCategoryIds(ids) > 0) {
            throw new BusinessException(Core.ResultCode.BAD_REQUEST, "该分类或其子分类已被商品引用，无法删除");
        }
        return categoryMapper.deleteByIds(ids) > 0;
    }

    /**
     * 收集指定分类及其全部子孙的 ID（深度优先遍历，层级不限）
     */
    private List<Long> collectIdsWithDescendants(Long rootId) {
        List<Long> ids = new ArrayList<>();
        Deque<Long> stack = new ArrayDeque<>();
        stack.push(rootId);
        while (!stack.isEmpty()) {
            Long current = stack.pop();
            ids.add(current);
            for (Long childId : categoryMapper.findIdsByParentId(current)) {
                stack.push(childId);
            }
        }
        return ids;
    }
}
