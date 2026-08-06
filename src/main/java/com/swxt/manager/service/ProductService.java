package com.swxt.manager.service;

import com.swxt.manager.dto.PageResult;
import com.swxt.manager.entity.Product;
import com.swxt.manager.mysql.UserMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品服务：商品分页查询、详情、搜索、增删改
 */
@Service
public class ProductService {

    @Autowired
    private UserMapping userMapping;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private FileService fileService;

    /**
     * 商品分页查询，并对 page/size 做非法值兜底
     */
    public PageResult<Product> listProductsPage(int page, int size) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        int offset = (page - 1) * size;
        List<Product> list = userMapping.listProductsPage(offset, size);
        long total = userMapping.countProducts();
        return new PageResult<>(list, total, page, size);
    }

    /**
     * 商品分页查询，支持组合筛选：keyword（名称模糊）、categoryId（精确）、priceMin/priceMax（价格区间）、status（状态）；
     * 全部条件为空时回退到全量分页
     */
    public PageResult<Product> listProductsPage(int page, int size, String keyword, Long categoryId,
                                                BigDecimal priceMin, BigDecimal priceMax, Integer status) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        int offset = (page - 1) * size;
        boolean hasFilter = (keyword != null && !keyword.trim().isEmpty())
                || categoryId != null || priceMin != null || priceMax != null || status != null;
        List<Product> list;
        long total;
        if (hasFilter) {
            list = userMapping.listProductsFilterPage(
                    keyword == null ? null : keyword.trim(), categoryId, priceMin, priceMax, status, offset, size);
            total = userMapping.countProductsFilter(
                    keyword == null ? null : keyword.trim(), categoryId, priceMin, priceMax, status);
        } else {
            list = userMapping.listProductsPage(offset, size);
            total = userMapping.countProducts();
        }
        return new PageResult<>(list, total, page, size);
    }

    /**
     * 商品详情：按 ID 查询（含分类名称），不存在返回 null
     */
    public Product getProductDetail(Long id) {
        return userMapping.getProductById(id);
    }

    /**
     * 按关键词搜索商品（内存分页）
     */
    public PageResult<Product> searchProductsByCondition(String value, int page, int size) {
        if (value == null || value.isEmpty()) return listProductsPage(page, size);
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        int offset = (page - 1) * size;
        List<Product> list = userMapping.searchProductsPage(value, offset, size);
        long total = userMapping.countSearchProducts(value);
        return new PageResult<>(list, total, page, size);
    }

    /**
     * 新增商品：商品默认状态取自系统配置 default_product_status
     */
    public boolean createProduct(Product product) {
        product.setStatus(systemConfigService.getSystemConfigInt("default_product_status"));
        return userMapping.createProduct(product) > 0;
    }

    /**
     * 修改商品信息，返回是否更新成功
     */
    public boolean updateProduct(Product product) {
        return userMapping.updateProduct(product) > 0;
    }

    /**
     * 删除商品，同步删除本地图片
     */
    public boolean deleteProduct(Long id) {
        Product product = userMapping.getProductById(id);
        if (product == null) return false;
        boolean deleted = userMapping.deleteProductById(id) > 0;
        if (deleted) {
            fileService.deleteByUrl(product.getImage());
        }
        return deleted;
    }
}
