package com.swxt.manager.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


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
