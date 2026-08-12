package com.swxt.manager.controller;

import com.swxt.manager.config.Core;
import com.swxt.manager.dto.Result;
import com.swxt.manager.entity.Category;
import com.swxt.manager.service.CategoryService;
import com.swxt.manager.service.SystemLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器：查询分类（登录可用），增删改分类（仅管理员）
 */
@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final SystemLogService logService;

    public CategoryController(CategoryService categoryService, SystemLogService logService) {
        this.categoryService = categoryService;
        this.logService = logService;
    }

    /**
     * 查询全部分类
     */
    @GetMapping
    public Result<List<Category>> list() {
        return Result.success(categoryService.listAll());
    }

    /**
     * 新增分类
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> create(@RequestBody Category category) {
        boolean created = categoryService.create(category);
        if (created) {
            logService.record(Core.ActionType.CREATE, Core.TargetType.CATEGORY, category.getId(), Core.LogResult.SUCCESS);
            return Result.success();
        }
        logService.record(Core.ActionType.CREATE, Core.TargetType.CATEGORY, category.getId(), Core.LogResult.FAIL);
        return Result.error(500, "分类创建失败");
    }

    /**
     * 更新分类
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@RequestBody Category category) {
        boolean updated = categoryService.update(category);
        if (updated) {
            logService.record(Core.ActionType.UPDATE, Core.TargetType.CATEGORY, category.getId(), Core.LogResult.SUCCESS);
            return Result.success();
        }
        logService.record(Core.ActionType.UPDATE, Core.TargetType.CATEGORY, category.getId(), Core.LogResult.FAIL);
        return Result.error(404, "分类不存在");
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        boolean deleted = categoryService.delete(id);
        if (deleted) {
            logService.record(Core.ActionType.DELETE, Core.TargetType.CATEGORY, id, Core.LogResult.SUCCESS);
            return Result.success();
        }
        logService.record(Core.ActionType.DELETE, Core.TargetType.CATEGORY, id, Core.LogResult.FAIL);
        return Result.error(404, "分类不存在");
    }
}
