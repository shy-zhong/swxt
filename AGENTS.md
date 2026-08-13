# swxt — Spring Boot 4.0.7 + Vue 3 的商品/用户管理后台

`swxt`(com.swxt.manager):双角色(ADMIN/USER)管理系统,含登录注册、用户/商品/分类管理、库存、系统配置与操作日志、统计报表。后端 Spring Boot 4.0.7 (Java 17) + MyBatis + MySQL,前端 Vue 3 + TypeScript + Vite。

## Commands

- 后端运行: `.\mvnw.cmd spring-boot:run`(Windows;启动于 `http://localhost:8887/api`,入口 `src/main/java/com/swxt/manager/SwxtApplication.java`)
- 后端测试: `.\mvnw.cmd test`;打包: `.\mvnw.cmd package`
- 前端开发: `cd frontend && npm run dev`(Vite,默认端口 5173,`/api` 代理到 `http://localhost:8887`)
- 前端构建: `cd frontend && npm run build`(vue-tsc -b && vite build,产物在 `frontend/dist`)
- 建库脚本: `swxt.sql`(库名 `swxt`;连接配置见 `src/main/resources/application.properties`)

## 环境工具路径

本机未将以下工具加入系统 PATH，执行编译/运行/git/mysql 命令前需临时补全环境变量:

- JDK 17（JAVA_HOME）：`D:\Program Files\Java\jdk-17`
- MySQL 客户端：`D:\Program Files\MySQL\MySQL Server 8.0\bin`
- Git：`D:\Program Files\Git\bin`
- PowerShell（`mvnw.cmd` 内部调用）：`C:\Windows\System32\WindowsPowerShell\v1.0`

编译/运行前设置环境:
```powershell
$env:PATH = 'D:\Program Files\Git\bin;D:\Program Files\MySQL\MySQL Server 8.0\bin;C:\Windows\System32\WindowsPowerShell\v1.0;' + $env:PATH
$env:JAVA_HOME = 'D:\Program Files\Java\jdk-17'
```

## Architecture

- `src/main/java/com/swxt/manager/config/` — 公共配置:`Core.java`(枚举:Role/ActionType/TargetType/LogResult/ResultCode)、`JwtUtil`+`JwtAuthenticationFilter`(JWT 认证)、`SecurityConfig`(放行 `/login` `/register` `/system-config/public` 与 OPTIONS,其余需认证)、`CorsConfig`、`GlobalExceptionHandler`+`BusinessException`、`DataInitializer`
- `controller/` — REST 入口:业务接口集中在 `UserController`(登录/注册/用户 CRUD/商品 CRUD),另有 Statistics/SystemConfig/SystemLog 三个控制器;统一返回 `dto/Result<T>`(code/message/success/data);管理员接口用 `@PreAuthorize("hasRole('ADMIN')")`。注意:无独立 Product/Category 控制器,`CategoryMapper` 无调用方(分类表未暴露接口,前端商品表单直接填 `categoryId`)
- `service/` — 业务逻辑(UserService/StatisticsService/SystemConfigService/SystemLogService)
- `mysql/` — MyBatis Mapper 接口(`@MapperScan("com.swxt.manager.mysql")`);SQL 全部写在接口注解里,`resources/mapper/` 目录为空(仅 `mybatis.mapper-locations` 配置仍指向它,无需处理)
- `entity/`、`dto/` — 表实体与请求/响应模型(login/register/statistics/system_config/user 分组)
- `frontend/src/` — `components/` 按页面一个组件(Login/Register/UserHome/AdminHome/UserManagement/ProductManager/Statistics/SystemSettings/SystemLogs/SystemFooter),`utils/request.ts` 封装 fetch(get/post/put/del,自动带 Bearer token,401/403 清 token),`utils/configStore.ts` 管理系统配置,`route/Router.ts` 按 token 与 role 守卫
- 数据库表:`user` `category` `product` `stock_record` `system_config` `system_log`

## Conventions

- 后端接口一律返回 `Result<T>`(静态工厂 `success/error/of(ResultCode)`),业务校验失败抛 `BusinessException`,由 `GlobalExceptionHandler` 统一转 JSON;不要直接返回裸对象或抛出非业务异常
- 方法/类上方用 `/** 中文说明 */` Javadoc 注释(项目现有风格);实体用 Lombok `@Data`
- MyBatis 已开启 `map-underscore-to-camel-case`,Java 驼峰 ↔ 数据库下划线自动映射
- 管理员写操作(UserController 等)成功后调用 `logService.record(ActionType, TargetType, id, LogResult)` 记录操作日志
- 前端请求必须走 `utils/request.ts`,`BASE_URL='/api'` 与后端 context-path 一致;token/role/username 存 `localStorage`
- 路由在 `route/Router.ts` 中懒加载注册;新增管理页需放 `/admin/*` 并校验 ADMIN 角色
- 写操作确认规则:任何文件写入/编辑(edit_file、multi_edit、write_file、delete_range、move_file 等)若单次改动新增+删除合计超过 25 行,必须先向用户询问确认;合计 ≤10 行可直接执行,无需询问
- reasonix.toml 修改规则:任何对 reasonix.toml(沙箱/工作区配置)的修改,无论改动行数多少,都必须先向用户询问确认
- 每次修改前自动提交git
- 每次修改后不提交git

## Notes

- `user` 表有软删除列 `deleted`(所有用户 SQL 都带 `deleted = 0/1` 条件);此前 `swxt.sql` 缺该列曾导致启动失败(`Unknown column 'deleted'`),现已在建表语句中补上
- `DataInitializer` 启动时若 `user` 表为空,自动创建 12 个测试账号(含 admin/管理员,密码统一 `123456`)
- `swxt.sql` 部分行注释为乱码(编码问题,不影响执行);库连接配置在 `application.properties`(数据库密码、JWT 密钥等敏感信息勿写入本文件)
- (待补充:部署方式、联调注意事项等)