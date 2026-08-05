package com.swxt.manager;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@MapperScan("com.swxt.manager.mysql")
@SpringBootApplication
public class SwxtApplication {

/**
 * 程序入口：启动 Spring Boot 容器
 */
    public static void main(String[] args) {
        SpringApplication.run(SwxtApplication.class, args);
    }
}
