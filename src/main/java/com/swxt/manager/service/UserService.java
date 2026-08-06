package com.swxt.manager.service;

import com.swxt.manager.config.BusinessException;
import com.swxt.manager.config.Core;
import com.swxt.manager.dto.PageResult;
import com.swxt.manager.dto.Result;
import com.swxt.manager.dto.register.RegisterRequest;
import com.swxt.manager.dto.user.UpdateUserRequest;
import com.swxt.manager.entity.Product;
import com.swxt.manager.entity.User;
import com.swxt.manager.mysql.UserMapping;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


@Slf4j
@Service
public class UserService {

    @Autowired
    UserMapping userMapping;

    @Autowired
    private SystemConfigService systemConfigService;

    @Autowired
    private FileService fileService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

/**
 * 登录校验：keyword 支持用户名或用户 ID，校验通过返回用户，失败返回 null
 */
    public User login(String keyword, String password) {
        User user = null;

        try {
            Long id = Long.parseLong(keyword);
            user = userMapping.loginById(id.toString());
        } catch (NumberFormatException e) {

        }

        if (user == null) {
            int count = userMapping.countByUsername(keyword);
            if (count > 1) {
                throw new RuntimeException("存在多个同名用户，请使用ID登录");
            }
            if (count == 1) {
                user = userMapping.loginByUsername(keyword);
            }
        }

        if (user == null) {
            log.warn("用户不存在: {}", keyword);
            return null;
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("密码验证失败: {}", keyword);
            return null;
        }

        log.info("登录成功: {}", user.getUsername());
        return user;
    }

/**
 * 注册新用户：校验系统配置（是否开放注册、密码长度），密码加密后入库
 */
    public User register(RegisterRequest request,boolean skipSystemConfig) {
        if (!systemConfigService.getSystemConfigBoolean("allow_register")
                && !skipSystemConfig) {
            throw new BusinessException(Core.ResultCode.REGISTER_CLOSED);
        }
        int minLen = systemConfigService.getSystemConfigInt("password_min_length");
        if (minLen > 0 && request.getPassword().length() < minLen) {
            throw new BusinessException(Core.ResultCode.PASSWORD_TOO_SHORT);
        }

        User user = new User();
        BeanUtils.copyProperties(request, user);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        if (userMapping.createNewUser(user) <= 0) {
            throw new BusinessException(Core.ResultCode.SERVER_ERROR, "注册失败");
        }

        return user;
    }

/**
 * 查询全部用户（不分页）
 */
    public List<User> listUsers() {
        return userMapping.listUsers();
    }

/**
 * 按字段与值分页搜索用户：key 为空时回退到普通分页列表，否则模糊查询后内存分页
 */
    public PageResult<User> searchUsersByCondition(String key, String value, int page, int size) {
        if (key.isEmpty()) return listUsersPage(page, size);
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        int offset = (page - 1) * size;
        List<User> list = userMapping.searchUsersPage(key, value, offset, size);
        long total = userMapping.countSearchUsers(key, value);
        return new PageResult<>(list, total, page, size);
    }

/**
 * 用户分页查询，并对 page/size 做非法值兜底
 */
    public PageResult<User> listUsersPage(int page, int size) {
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        int offset = (page - 1) * size;
        List<User> list = userMapping.listUsersPage(offset, size);
        long total = userMapping.countUsers();
        return new PageResult<>(list, total, page, size);
    }

    /**
     * 用户分页查询，支持按关键词模糊搜索（用户名/真实姓名/手机号/邮箱）；关键词为空时回退到全量分页
     */
    public PageResult<User> listUsersPage(int page, int size, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return listUsersPage(page, size);
        }
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        int offset = (page - 1) * size;
        List<User> list = userMapping.listUsersKeywordPage(keyword.trim(), offset, size);
        long total = userMapping.countUsersKeyword(keyword.trim());
        return new PageResult<>(list, total, page, size);
    }

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
 * 删除用户，返回是否删除成功
 */
    public boolean deleteUser(Long id) {
        return userMapping.deleteUserById(id) > 0;
    }

/**
 * 更新用户信息与权限，返回是否更新成功
 */
    public boolean updateUser(UpdateUserRequest request) {
        User user = new User();
        BeanUtils.copyProperties(request, user);
        return userMapping.updateUser(user) > 0;
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
