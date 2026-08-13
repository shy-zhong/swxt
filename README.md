# swxt — 商品管理系统

基于 Spring Boot 4.0.7 + Vue 3 的双角色（ADMIN / USER）商品管理后台，涵盖登录注册、用户管理、商品与分类管理、库存出入库、系统配置、操作日志、统计报表及微信登录等功能。

## 技术栈

| 层面 | 技术 |
|------|------|
| 后端 | Spring Boot 4.0.7、Java 17、MyBatis、Spring Security + JWT |
| 数据库 | MySQL 8.0 |
| 前端 | Vue 3、TypeScript、Vite |
| 构建 | Maven（mvnw）、npm |
| 其他 | Hutool（二维码生成）、Lombok、Jackson |

## 项目结构

```
swxt/
├── src/main/java/com/swxt/manager/
│   ├── config/          # 公共配置：Core 枚举、Security、CORS、JWT 过滤器、异常处理、WebMvc
│   ├── controller/      # REST 控制器：User、Product、Statistics、SystemConfig、SystemLog、QrCode、File
│   ├── service/         # 业务逻辑：User、Product、Statistics、SystemConfig、SystemLog、WeChat、QrCode、File
│   ├── mysql/           # MyBatis Mapper 接口（SQL 写在注解中）
│   ├── entity/          # 数据库实体
│   ├── dto/             # 请求/响应模型
│   └── Utils/           # JwtUtil 等工具类
├── src/main/resources/
│   └── application.properties
├── frontend/
│   └── src/
│       ├── components/  # 页面组件（Login、Register、UserHome、AdminHome、ProductManager 等）
│       ├── utils/       # request.ts（fetch 封装）、configStore.ts（配置状态）
│       └── route/       # Router.ts（路由守卫）
├── swxt.sql             # 建库脚本（含初始数据）
├── pom.xml
└── mvnw.cmd
```

## 快速开始

### 环境要求

- JDK 17
- MySQL 8.0
- Node.js 18+
- Maven 3.9+（或使用项目自带 mvnw）

### 1. 初始化数据库

```sql
CREATE DATABASE swxt DEFAULT CHARACTER SET utf8mb4;
```

执行 `swxt.sql` 建表并导入初始数据。连接配置见 `src/main/resources/application.properties`。

### 2. 启动后端

```powershell
# 设置环境（Windows）
$env:JAVA_HOME = 'D:\Program Files\Java\jdk-17'
$env:PATH = 'D:\Program Files\MySQL\MySQL Server 8.0\bin;' + $env:PATH

# 运行
.\mvnw.cmd spring-boot:run
```

后端启动于 `http://localhost:8887/api`，启动时若 `user` 表为空会自动创建测试账号（密码统一 `123456`）。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端开发服务器默认端口 5173，`/api` 请求自动代理到 `http://localhost:8887`。

### 4. 生产构建

```bash
cd frontend
npm run build    # 产物在 frontend/dist
```

## 核心功能

### 用户与权限

- 双角色体系：ADMIN（管理员）、USER（普通用户，含 OPERATOR 操作员）
- JWT 无状态认证，Spring Security 方法级鉴权（`@PreAuthorize`）
- 注册开关、密码长度、默认角色等由 `system_config` 表控制
- 微信登录：微信浏览器内静默授权 + PC 端二维码扫码登录

### 商品管理

- 商品 CRUD、分类管理、上下架控制
- 商品可见性由角色与 `show_disabled_products` 配置共同控制
- 图片上传（扩展名白名单 + UUID 重命名）

### 库存管理

- 出入库操作（仅 USER 角色可执行）
- 库存预警阈值可配置

### 系统配置

- key-value 形式存储，管理员可通过后台实时修改
- 涵盖站点信息、业务开关、阈值等 14 项配置
- 三级 API 权限：匿名（站点信息）、登录用户（常规配置）、管理员（全部配置）

### 操作日志

- 关键写操作自动记录日志
- 支持按用户名、操作类型、目标类型、结果筛选
- 日志开关可配置

### 统计报表

- 商品、用户、库存等多维度统计数据

## 数据库表

| 表名 | 说明 |
|------|------|
| `user` | 用户表（含软删除 `deleted`、微信 `wechat_openid`） |
| `category` | 商品分类表 |
| `product` | 商品表 |
| `stock_record` | 库存出入库记录 |
| `system_config` | 系统配置表（key-value） |
| `system_log` | 操作日志表 |

## API 约定

- 统一前缀：`/api`（context-path）
- 统一返回格式：`Result<T>`（code / message / success / data）
- 认证方式：请求头 `Authorization: Bearer <token>`
- 放行白名单：`/login`、`/register`、`/system-config/public`、`/uploads/**`、`OPTIONS` 预检

## 默认账号

启动时 `DataInitializer` 自动创建测试账号，密码统一为 `123456`，具体角色分布见 `swxt.sql` 初始数据。

## 许可证

私有项目，未开源。
