package com.swxt.manager.controller;

import com.swxt.manager.Utils.SecurityUtil;
import com.swxt.manager.config.Core;
import com.swxt.manager.Utils.JwtUtil;
import com.swxt.manager.dto.PageResult;
import com.swxt.manager.dto.Result;
import com.swxt.manager.dto.login.LoginRequest;
import com.swxt.manager.dto.login.LoginResponse;
import com.swxt.manager.dto.register.RegisterRequest;
import com.swxt.manager.dto.register.RegisterResponse;
import com.swxt.manager.dto.user.UpdateUserRequest;
import com.swxt.manager.entity.Product;
import com.swxt.manager.entity.User;
import com.swxt.manager.service.SystemLogService;
import com.swxt.manager.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final SystemLogService logService;
    private final SecurityUtil securityUtil;
    /**
     * 构造用户控制器：注入用户服务、JWT 工具与日志服务
     */
    public UserController(UserService userService, JwtUtil jwtUtil, SystemLogService logService, SecurityUtil securityUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.logService = logService;
        this.securityUtil = securityUtil;
    }

    /**
     * 登录：校验用户名/密码，成功则签发 JWT 令牌并记录日志
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        User user = userService.login(request.getUsername(), request.getPassword());
        if (user == null) {
            logService.record(Core.ActionType.LOGIN, Core.TargetType.USER, null, Core.LogResult.FAIL);
            return Result.of(Core.ResultCode.LOGIN_FAILED);
        }
        String token = jwtUtil.generateToken(user);

        logService.record(Core.ActionType.LOGIN, Core.TargetType.USER, user.getId(), Core.LogResult.SUCCESS);

        return Result.success(new LoginResponse(token, user.getUsername(), user.getRole().name()));
    }

    /**
     * 注册：校验失败由 BusinessException 抛出，全局异常处理器统一拦截
     */
    @PostMapping("/register")
    public Result<RegisterResponse> register(@RequestBody RegisterRequest request,
                                             @RequestParam(value = "usermanager" ,required = false, defaultValue = "") String skipSystemConfig) {

        User user;
        try {
            user = userService.register(request,!skipSystemConfig.isEmpty());
        } catch (RuntimeException e) {
            // 注册失败（注册关闭/密码过短/用户名重复等）记录 FAIL 日志后重新抛出，由全局异常处理器统一返回
            logService.record(Core.ActionType.REGISTER, Core.TargetType.USER, Core.LogResult.FAIL);
            throw e;
        }
        logService.record(Core.ActionType.REGISTER, Core.TargetType.USER, user.getId(), Core.LogResult.SUCCESS);
        return Result.success(new RegisterResponse(user.getUsername()));
    }

    @GetMapping("/login-out")
    public Result<Void> loginOut(){
        logService.record(Core.ActionType.LOGOUT,Core.TargetType.USER,securityUtil.getUserId(),null);
        return Result.success();
    }
    /**
     * 用户分页列表
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<User>> listUsers(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        PageResult<User> result = userService.listUsersPage(page, size);
        return Result.success(result);
    }

    /**
     * 按字段与值搜索用户
     */
    @GetMapping("/users/search")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<PageResult<User>> searchUsers(
            @RequestParam(value = "key") String key,
            @RequestParam(value = "value") String value,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        PageResult<User> result = userService.searchUsersByCondition(key, value, page, size);
        return Result.success(result);
    }

    /**
     * 商品分页列表
     */
    @GetMapping("/products")
    public Result<PageResult<Product>> listProducts(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        PageResult<Product> result = userService.listProductsPage(page, size);
        return Result.success(result);
    }

    @GetMapping("/products/search")
    public Result<PageResult<Product>> searchProducts(
            @RequestParam(value = "value") String value,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        PageResult<Product> result = userService.searchProductsByCondition(value, page, size);
        return Result.success(result);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteUser(@PathVariable Long id) {
        boolean deleted = userService.deleteUser(id);
        logService.record(Core.ActionType.DELETE, Core.TargetType.USER, id,
                deleted ? Core.LogResult.SUCCESS : Core.LogResult.FAIL);
        if (deleted) {
            return Result.success();
        }
        return Result.error(404, "用户不存在");
    }

    /**
     * 更新用户信息与权限
     */
    @PutMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateUser(@RequestBody UpdateUserRequest request) {
        boolean updated = userService.updateUser(request);
        logService.record(Core.ActionType.UPDATE, Core.TargetType.USER, request.getId(),
                updated ? Core.LogResult.SUCCESS : Core.LogResult.FAIL);
        if (updated) {
            return Result.success();
        }
        return Result.error(404, "用户不存在");
    }

    /**
     * 新增商品
     */
    @PostMapping("/products")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> createProduct(@RequestBody Product product) {
        boolean created = userService.createProduct(product);
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
    @PutMapping("/products")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateProduct(@RequestBody Product product) {
        boolean updated = userService.updateProduct(product);
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
    @DeleteMapping("/products/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        boolean deleted = userService.deleteProduct(id);
        logService.record(Core.ActionType.DELETE, Core.TargetType.PRODUCT, id,
                deleted ? Core.LogResult.SUCCESS : Core.LogResult.FAIL);
        if (deleted) {
            return Result.success();
        }
        return Result.error(404, "商品不存在");
    }
}
