package com.swxt.manager.config;

import com.swxt.manager.dto.register.RegisterRequest;
import com.swxt.manager.entity.User;
import com.swxt.manager.mysql.UserMapping;
import com.swxt.manager.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
@Order(1)
public class DataInitializer implements CommandLineRunner {
    /**
     * 应用启动时执行：用户表为空则批量创建测试账号（密码统一 123456）
     */
    @Override
    public void run(String... args) {

    }

}
