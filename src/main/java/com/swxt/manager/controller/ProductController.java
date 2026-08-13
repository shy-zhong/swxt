package com.swxt.manager.controller;

import com.swxt.manager.Utils.SecurityUtil;
import com.swxt.manager.config.Core;
import com.swxt.manager.dto.PageResult;
import com.swxt.manager.dto.Result;
import com.swxt.manager.entity.Product;
import com.swxt.manager.service.ProductService;
import com.swxt.manager.service.SystemConfigService;
import com.swxt.manager.service.SystemLogService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 商品控制器
 */
@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final SystemConfigService systemConfigService;
    private final SystemLogService logService;

    public ProductController(ProductService productService, SystemConfigService systemConfigService, SystemLogService logService) {
        this.productService = productService;
        this.systemConfigService = systemConfigService;
        this.logService = logService;
    }

    /**
     * 商品分页列表
     */
    @GetMapping
    public Result<PageResult<Product>> list(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "priceMin", required = false) BigDecimal priceMin,
            @RequestParam(value = "priceMax", required = false) BigDecimal priceMax,
            @RequestParam(value = "status", required = false) Integer status
    ) {
        if (status == null && !"ADMIN".equals(SecurityUtil.getRole())) {
            if (!systemConfigService.getSystemConfigBoolean("show_disabled_products")) {
                status = 1;
            }
        }
        PageResult<Product> result = productService.listProductsPage(page, size, keyword, categoryId, priceMin, priceMax, status);
        return Result.success(result);
    }

    /**
     * 商品详情：按 ID 查询（含分类名称），供用户端详情页与管理端查询使用
     */
    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        Product product = productService.getProductDetail(id);
        if (product == null) {
            return Result.error(404, "商品不存在");
        }
        return Result.success(product);
    }

    /**
     * 按关键词搜索商品
     */
    @GetMapping("/search")
    public Result<PageResult<Product>> search(
            @RequestParam(value = "value") String value,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        PageResult<Product> result;
        if ("ADMIN".equals(SecurityUtil.getRole())) {
            result = productService.searchProductsByCondition(value, page, size);
        } else {
            // 非管理员：根据配置决定是否展示已下架商品
            Integer productStatus = systemConfigService.getSystemConfigBoolean("show_disabled_products") ? null : 1;
            result = productService.listProductsPage(page, size, value, null, null, null, productStatus);
        }
        return Result.success(result);
    }

    /**
     * 新增商品
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> create(@RequestBody Product product) {
        boolean created = productService.createProduct(product);
        logService.record(Core.ActionType.CREATE, Core.TargetType.PRODUCT, product.getId(),
                created ? Core.LogResult.SUCCESS : Core.LogResult.FAIL);
        if (created) {
            return Result.success();
        }
        return Result.error(500, "商品创建失败");
    }

    /**
     * 修改商品信息
     */
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@RequestBody Product product) {
        boolean updated = productService.updateProduct(product);
        logService.record(Core.ActionType.UPDATE, Core.TargetType.PRODUCT, product.getId(),
                updated ? Core.LogResult.SUCCESS : Core.LogResult.FAIL);
        if (updated) {
            return Result.success();
        }
        return Result.error(404, "商品不存在");
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        boolean deleted = productService.deleteProduct(id);
        logService.record(Core.ActionType.DELETE, Core.TargetType.PRODUCT, id,
                deleted ? Core.LogResult.SUCCESS : Core.LogResult.FAIL);
        if (deleted) {
            return Result.success();
        }
        return Result.error(404, "商品不存在");
    }
}
