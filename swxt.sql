-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: swxt
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '分类名称',
  `parent_id` bigint DEFAULT '0' COMMENT '父分类ID',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (1,'数码家电',0,1),(2,'服装鞋帽',0,2),(3,'食品饮料',0,3),(4,'家居生活',0,4),(5,'美妆个护',0,5),(6,'手机通讯',1,1),(7,'电脑办公',1,2),(8,'影音设备',1,3),(9,'智能穿戴',1,4),(10,'男装',2,1),(11,'女装',2,2),(12,'鞋靴',2,3),(13,'箱包配饰',2,4),(14,'休闲零食',3,1),(15,'茶饮咖啡',3,2),(16,'进口食品',3,3),(17,'营养保健',3,4),(18,'家具',4,1),(19,'厨具',4,2),(20,'家纺',4,3),(21,'收纳整理',4,4),(22,'护肤',5,1),(23,'彩妆',5,2),(24,'洗护清洁',5,3),(25,'香水香氛',5,4);
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_item`
--

DROP TABLE IF EXISTS `order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_name` varchar(200) DEFAULT NULL COMMENT '商品名称',
  `price` decimal(10,2) NOT NULL COMMENT '购买价格',
  `quantity` int NOT NULL COMMENT '购买数量',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_item`
--

LOCK TABLES `order_item` WRITE;
/*!40000 ALTER TABLE `order_item` DISABLE KEYS */;
/*!40000 ALTER TABLE `order_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '下单用户ID',
  `username` varchar(50) DEFAULT NULL COMMENT '下单用户名',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT '订单状态',
  `remark` varchar(200) DEFAULT NULL COMMENT '备注',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL COMMENT '商品名称',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `image` varchar(500) DEFAULT NULL COMMENT '图片URL',
  `description` text COMMENT '描述',
  `stock` int DEFAULT '0' COMMENT '库存',
  `status` tinyint DEFAULT '1' COMMENT '状态：0禁用，1启用',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product`
--

LOCK TABLES `product` WRITE;
/*!40000 ALTER TABLE `product` DISABLE KEYS */;
INSERT INTO `product` VALUES (1,'旗舰智能手机',1,4999.00,'/uploads/images/electronics/smartphone.jpg','6.7寸OLED全面屏，256GB存储，5000万像素',120,1),(2,'轻薄笔记本电脑',1,6899.00,'/uploads/images/electronics/laptop.jpg','14寸2.8K屏，16GB+512GB SSD，长续航',29,1),(3,'主动降噪无线耳机',1,899.00,'/uploads/images/electronics/headphones.jpg','蓝牙5.3，ANC降噪40dB，40小时续航',600,0),(4,'便携平板电脑',1,2599.00,'/uploads/images/electronics/tablet.jpg','11寸2K屏，128GB存储，支持触控笔',160,1),(5,'智能运动手表',1,1299.00,'/uploads/images/electronics/watch.jpg','1.4寸AMOLED屏，心率血氧监测，5ATM防水',210,1),(6,'4K高清摄像头',1,349.00,'/uploads/images/electronics/camera.jpg','4K分辨率，自动对焦，带麦克风直播必备',18,1),(7,'纯棉圆领T恤',2,79.00,'/uploads/images/clothing/tshirt.jpg','100%纯棉，经典圆领，舒适透气',500,1),(8,'修身直筒牛仔裤',2,249.00,'/uploads/images/clothing/jeans.jpg','高品质水洗棉，经典直筒版型',300,1),(9,'冬季保暖羽绒服',2,899.00,'/uploads/images/clothing/jacket.jpg','90%白鸭绒填充，防风防水面料',150,1),(10,'轻便透气运动鞋',2,399.00,'/uploads/images/clothing/sneakers.jpg','飞织鞋面，减震中底，日常跑步通勤',420,1),(11,'商务休闲衬衫',2,199.00,'/uploads/images/clothing/shirt.jpg','免烫面料，修身剪裁，多色可选',260,1),(12,'经典休闲卫衣',2,269.00,'/uploads/images/clothing/hoodie.jpg','加绒内里，连帽设计，秋冬百搭',230,1),(13,'精品阿拉比卡咖啡豆',3,118.00,'/uploads/images/food/coffee.jpg','中度烘焙，200g袋装，浓郁香气',180,1),(14,'72%纯黑巧克力',3,58.00,'/uploads/images/food/chocolate.jpg','可可含量72%，比利时工艺，100g装',260,1),(15,'手工黄油曲奇',3,46.00,'/uploads/images/food/biscuits.jpg','进口黄油制作，酥脆香甜，300g盒装',200,1),(16,'明前龙井茶礼盒',3,199.00,'/uploads/images/food/tea.jpg','浙江原产，雨前嫩芽，250g礼盒装',120,1),(17,'进口混合坚果',3,89.00,'/uploads/images/food/nuts.jpg','6种坚果果干混合，每日坚果，500g罐装',310,1),(18,'云南小粒速溶咖啡',3,69.00,'/uploads/images/food/instant-coffee.jpg','独立包装30条装，冷热双泡，醇香丝滑',340,1),(19,'法式马卡龙礼盒',3,128.00,'/uploads/images/food/macaron.jpg','6口味12枚装，经典法式甜点，送礼佳品',150,1),(20,'原木护眼台灯',4,259.00,'/uploads/images/home/lamp.jpg','三档色温，可调节灯臂，USB充电',180,1),(21,'北欧风格实木餐椅',4,389.00,'/uploads/images/home/chair.jpg','白橡木框架，布艺软垫座面',90,1),(22,'柔软针织毛毯',4,169.00,'/uploads/images/home/blanket.jpg','腈纶混纺材质，四季通用，130x170cm',220,1),(23,'大容量电热水壶',4,149.00,'/uploads/images/home/kettle.jpg','1.7L大容量，304不锈钢内胆，快速沸腾',260,1),(24,'香薰蜡烛礼盒',4,99.00,'/uploads/images/home/candle.jpg','大豆蜡，3种香型组合，50小时燃烧',280,1),(25,'多功能收纳柜',4,459.00,'/uploads/images/home/storage.jpg','5层抽屉，环保板材，卧室客厅通用',140,1);
/*!40000 ALTER TABLE `product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `stock_record`
--

DROP TABLE IF EXISTS `stock_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock_record` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `product_id` bigint NOT NULL,
  `type` varchar(10) NOT NULL,
  `quantity` int NOT NULL,
  `operator` varchar(50) DEFAULT NULL,
  `remark` varchar(200) DEFAULT NULL,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_product` (`product_id`),
  KEY `idx_type` (`type`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=105 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `stock_record`
--

LOCK TABLES `stock_record` WRITE;
/*!40000 ALTER TABLE `stock_record` DISABLE KEYS */;
INSERT INTO `stock_record` VALUES (1,1,'IN',150,'zhangsan','首批入库','2026-07-01 09:30:00'),(2,1,'OUT',30,'zhangsan','线下门店调拨','2026-07-05 14:20:00'),(3,1,'IN',60,'zhangsan','常规补货','2026-07-20 10:15:00'),(4,1,'OUT',40,'zhangsan','电商渠道发货','2026-07-28 16:45:00'),(5,2,'IN',100,'zhangsan','首批入库','2026-07-01 10:00:00'),(6,2,'OUT',20,'zhangsan','企业客户采购','2026-07-08 11:15:00'),(7,2,'IN',40,'zhangsan','常规补货','2026-07-22 09:40:00'),(8,2,'OUT',30,'zhangsan','促销活动出货','2026-07-30 14:30:00'),(9,3,'IN',400,'zhangsan','大批量入库','2026-07-02 09:00:00'),(10,3,'OUT',50,'zhangsan','促销活动出货','2026-07-10 16:30:00'),(11,3,'OUT',30,'zhangsan','电商渠道发货','2026-07-25 11:20:00'),(12,3,'IN',80,'zhangsan','常规补货','2026-07-31 10:00:00'),(13,4,'IN',200,'zhangsan','首批入库','2026-07-02 10:30:00'),(14,4,'OUT',40,'zhangsan','电商渠道发货','2026-07-12 13:45:00'),(15,4,'IN',50,'zhangsan','常规补货','2026-07-24 15:10:00'),(16,4,'OUT',30,'zhangsan','线下门店调拨','2026-07-29 09:50:00'),(17,5,'IN',550,'zhangsan','大批量入库','2026-07-03 09:15:00'),(18,5,'OUT',50,'zhangsan','线下门店调拨','2026-07-06 15:00:00'),(19,5,'OUT',80,'zhangsan','电商渠道发货','2026-07-23 14:25:00'),(20,5,'IN',100,'zhangsan','常规补货','2026-07-31 16:30:00'),(21,6,'IN',350,'zhangsan','首批入库','2026-07-03 14:00:00'),(22,6,'OUT',30,'zhangsan','企业客户采购','2026-07-09 10:20:00'),(23,6,'OUT',50,'zhangsan','促销活动出货','2026-07-26 13:40:00'),(24,6,'IN',40,'zhangsan','常规补货','2026-07-30 11:15:00'),(25,7,'IN',300,'lisi','换季备货','2026-07-01 11:00:00'),(26,7,'OUT',80,'lisi','电商渠道发货','2026-07-07 14:30:00'),(27,7,'IN',100,'lisi','常规补货','2026-07-21 10:30:00'),(28,7,'OUT',50,'lisi','线下门店调拨','2026-07-28 15:20:00'),(29,8,'IN',250,'lisi','常规补货','2026-07-04 09:45:00'),(30,8,'OUT',30,'lisi','线下门店调拨','2026-07-11 16:00:00'),(31,8,'OUT',40,'lisi','电商渠道发货','2026-07-25 10:10:00'),(32,8,'IN',60,'lisi','常规补货','2026-07-31 09:30:00'),(33,9,'IN',600,'lisi','冬季备货','2026-07-02 13:00:00'),(34,9,'OUT',100,'lisi','促销活动出货','2026-07-13 11:30:00'),(35,9,'OUT',80,'lisi','电商渠道发货','2026-07-22 14:50:00'),(36,9,'IN',120,'lisi','常规补货','2026-07-30 15:45:00'),(37,10,'IN',500,'lisi','大批量入库','2026-07-04 10:00:00'),(38,10,'OUT',80,'lisi','电商渠道发货','2026-07-08 15:45:00'),(39,10,'OUT',60,'lisi','企业客户采购','2026-07-24 11:25:00'),(40,10,'IN',70,'lisi','常规补货','2026-07-31 14:00:00'),(41,11,'IN',280,'lisi','常规补货','2026-07-05 09:30:00'),(42,11,'OUT',20,'lisi','企业客户采购','2026-07-12 14:00:00'),(43,11,'OUT',30,'lisi','电商渠道发货','2026-07-26 16:15:00'),(44,11,'IN',50,'lisi','常规补货','2026-07-30 10:20:00'),(45,12,'IN',350,'lisi','首批入库','2026-07-05 14:15:00'),(46,12,'OUT',40,'lisi','线下门店调拨','2026-07-14 10:30:00'),(47,12,'OUT',50,'lisi','促销活动出货','2026-07-23 13:50:00'),(48,12,'IN',60,'lisi','常规补货','2026-07-31 11:40:00'),(49,13,'IN',200,'wangwu','首批入库','2026-07-06 09:00:00'),(50,13,'OUT',20,'wangwu','电商渠道发货','2026-07-09 16:15:00'),(51,13,'IN',40,'wangwu','常规补货','2026-07-22 11:00:00'),(52,13,'OUT',30,'wangwu','线下门店调拨','2026-07-29 14:45:00'),(53,14,'IN',300,'wangwu','常规补货','2026-07-06 11:30:00'),(54,14,'OUT',40,'wangwu','促销活动出货','2026-07-11 13:00:00'),(55,14,'OUT',50,'wangwu','电商渠道发货','2026-07-25 15:30:00'),(56,14,'IN',70,'wangwu','常规补货','2026-07-31 10:15:00'),(57,15,'IN',250,'wangwu','首批入库','2026-07-07 09:45:00'),(58,15,'OUT',50,'wangwu','线下门店调拨','2026-07-13 15:30:00'),(59,15,'OUT',40,'wangwu','电商渠道发货','2026-07-24 10:45:00'),(60,15,'IN',60,'wangwu','常规补货','2026-07-30 16:00:00'),(61,16,'IN',150,'wangwu','首批入库','2026-07-07 14:00:00'),(62,16,'OUT',30,'wangwu','电商渠道发货','2026-07-14 10:00:00'),(63,16,'IN',40,'wangwu','常规补货','2026-07-23 11:30:00'),(64,16,'OUT',20,'wangwu','企业客户采购','2026-07-29 15:15:00'),(65,17,'IN',360,'wangwu','大批量入库','2026-07-08 10:15:00'),(66,17,'OUT',40,'wangwu','企业客户采购','2026-07-15 14:45:00'),(67,17,'OUT',50,'wangwu','电商渠道发货','2026-07-26 10:50:00'),(68,17,'IN',60,'wangwu','常规补货','2026-07-31 13:30:00'),(69,18,'IN',400,'wangwu','常规补货','2026-07-08 13:30:00'),(70,18,'OUT',60,'wangwu','电商渠道发货','2026-07-15 16:00:00'),(71,18,'OUT',50,'wangwu','促销活动出货','2026-07-25 11:10:00'),(72,18,'IN',80,'wangwu','常规补货','2026-07-31 15:45:00'),(73,19,'IN',320,'wangwu','首批入库','2026-07-09 09:30:00'),(74,19,'OUT',50,'wangwu','线下门店调拨','2026-07-16 11:15:00'),(75,19,'OUT',40,'wangwu','电商渠道发货','2026-07-27 14:20:00'),(76,19,'IN',60,'wangwu','常规补货','2026-07-31 10:30:00'),(77,20,'IN',250,'zhaoliu','首批入库','2026-07-09 15:00:00'),(78,20,'OUT',30,'zhaoliu','促销活动出货','2026-07-16 14:30:00'),(79,20,'OUT',40,'zhaoliu','电商渠道发货','2026-07-24 16:00:00'),(80,20,'IN',50,'zhaoliu','常规补货','2026-07-31 14:15:00'),(81,21,'IN',300,'zhaoliu','常规补货','2026-07-10 10:00:00'),(82,21,'OUT',40,'zhaoliu','企业客户采购','2026-07-17 13:45:00'),(83,21,'OUT',30,'zhaoliu','线下门店调拨','2026-07-28 11:30:00'),(84,21,'IN',50,'zhaoliu','常规补货','2026-07-31 16:45:00'),(85,22,'IN',200,'zhaoliu','首批入库','2026-07-10 14:30:00'),(86,22,'OUT',20,'zhaoliu','电商渠道发货','2026-07-17 16:15:00'),(87,22,'IN',40,'zhaoliu','常规补货','2026-07-23 15:20:00'),(88,22,'OUT',30,'zhaoliu','促销活动出货','2026-07-30 10:00:00'),(89,23,'IN',350,'zhaoliu','大批量入库','2026-07-11 09:15:00'),(90,23,'OUT',30,'zhaoliu','线下门店调拨','2026-07-18 11:00:00'),(91,23,'OUT',40,'zhaoliu','电商渠道发货','2026-07-26 13:15:00'),(92,23,'IN',50,'zhaoliu','常规补货','2026-07-31 11:50:00'),(93,24,'IN',280,'zhaoliu','首批入库','2026-07-11 15:45:00'),(94,24,'OUT',40,'zhaoliu','促销活动出货','2026-07-18 14:30:00'),(95,24,'OUT',30,'zhaoliu','电商渠道发货','2026-07-25 16:30:00'),(96,24,'IN',50,'zhaoliu','常规补货','2026-07-31 15:00:00'),(97,25,'IN',200,'zhaoliu','常规补货','2026-07-12 10:30:00'),(98,25,'OUT',20,'zhaoliu','电商渠道发货','2026-07-19 13:15:00'),(99,25,'OUT',30,'zhaoliu','企业客户采购','2026-07-27 10:45:00'),(100,25,'IN',40,'zhaoliu','常规补货','2026-07-31 12:30:00'),(101,26,'IN',180,'qianqi','首批入库','2026-07-12 16:00:00'),(102,26,'OUT',40,'qianqi','企业客户采购','2026-07-19 15:30:00'),(103,26,'OUT',30,'qianqi','电商渠道发货','2026-07-28 10:15:00'),(104,26,'IN',50,'qianqi','常规补货','2026-07-31 14:30:00');
/*!40000 ALTER TABLE `stock_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `system_config`
--

DROP TABLE IF EXISTS `system_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text COMMENT '配置值',
  `description` varchar(200) DEFAULT NULL COMMENT '描述',
  PRIMARY KEY (`id`),
  UNIQUE KEY `config_key` (`config_key`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `system_config`
--

LOCK TABLES `system_config` WRITE;
/*!40000 ALTER TABLE `system_config` DISABLE KEYS */;
INSERT INTO `system_config` VALUES (1,'site_name','商城管理系统','站点名称'),(2,'site_description','一站式商品管理平台','站点描述'),(3,'contact_phone','81097566','联系电话'),(4,'contact_email','25555555@swxt.com','联系邮箱'),(5,'default_product_status','1','新商品默认状态'),(6,'low_stock_threshold','80','库存预警阈值'),(7,'enable_stock_warning','false','是否开启库存预警'),(8,'allow_register','true','是否允许用户自注册'),(9,'default_user_role','USER','默认用户角色'),(10,'password_min_length','6','密码最小长度'),(11,'currency_symbol','$','货币符号'),(14,'homepage_background','','网站首页背景图URL'),(15,'page_size','15','列表每页显示数据量'),(16,'enable_log','true','是否开启操作日志');
/*!40000 ALTER TABLE `system_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `system_log`
--

DROP TABLE IF EXISTS `system_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `system_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '操作用户',
  `user_id` bigint DEFAULT NULL COMMENT '操作用户ID',
  `action_type` varchar(20) DEFAULT NULL COMMENT '操作类型：CREATE/UPDATE/DELETE/LOGIN/LOGOUT',
  `target_type` varchar(50) DEFAULT NULL COMMENT '操作对象类型：USER/PRODUCT/CATEGORY/STOCK/CONFIG',
  `target_id` bigint DEFAULT NULL COMMENT '操作对象ID',
  `result` varchar(20) DEFAULT 'SUCCESS' COMMENT '操作结果：SUCCESS/FAIL',
  `ip` varchar(50) DEFAULT NULL COMMENT 'IP地址',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_username` (`username`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=125 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `system_log`
--

LOCK TABLES `system_log` WRITE;
/*!40000 ALTER TABLE `system_log` DISABLE KEYS */;
INSERT INTO `system_log` VALUES (1,'admin',1,'LOGIN','USER',1,'SUCCESS','192.168.1.10','2026-07-28 08:30:12'),(2,'admin',1,'LOGOUT','USER',1,'SUCCESS','192.168.1.10','2026-07-28 18:05:43'),(3,'admin',1,'LOGIN','USER',1,'SUCCESS','192.168.1.10','2026-07-29 08:15:22'),(4,'admin',1,'LOGOUT','USER',1,'SUCCESS','192.168.1.10','2026-07-29 17:48:10'),(5,'zhangsan',2,'LOGIN','USER',2,'SUCCESS','192.168.1.22','2026-07-29 09:02:33'),(6,'zhangsan',2,'LOGOUT','USER',2,'SUCCESS','192.168.1.22','2026-07-29 17:30:00'),(7,'admin',1,'LOGIN','USER',1,'SUCCESS','192.168.1.10','2026-07-30 08:20:15'),(8,'lisi',3,'LOGIN','USER',3,'FAIL','192.168.1.33','2026-07-30 09:10:45'),(9,'lisi',3,'LOGIN','USER',3,'SUCCESS','192.168.1.33','2026-07-30 09:12:20'),(10,'admin',1,'LOGOUT','USER',1,'SUCCESS','192.168.1.10','2026-07-30 18:10:00'),(11,'wangwu',4,'LOGIN','USER',4,'SUCCESS','192.168.1.44','2026-07-31 08:45:30'),(12,'zhangsan',2,'LOGIN','USER',2,'SUCCESS','192.168.1.22','2026-07-31 09:15:10'),(13,'admin',1,'LOGIN','USER',1,'SUCCESS','192.168.1.10','2026-08-01 08:00:00'),(14,'admin',1,'LOGOUT','USER',1,'SUCCESS','192.168.1.10','2026-08-01 17:55:22'),(15,'zhaoliu',5,'LOGIN','USER',5,'SUCCESS','192.168.1.55','2026-08-01 10:20:33'),(16,'qianqi',6,'LOGIN','USER',6,'FAIL','192.168.1.66','2026-08-01 14:30:15'),(17,'qianqi',6,'LOGIN','USER',6,'SUCCESS','192.168.1.66','2026-08-01 14:32:40'),(18,'admin',1,'LOGIN','USER',1,'SUCCESS','192.168.1.10','2026-08-02 08:10:12'),(19,'wangwu',4,'LOGOUT','USER',4,'SUCCESS','192.168.1.44','2026-08-02 17:40:00'),(20,'admin',1,'LOGOUT','USER',1,'SUCCESS','192.168.1.10','2026-08-02 18:00:00'),(21,'admin',1,'LOGIN','USER',1,'SUCCESS','192.168.1.10','2026-08-03 08:05:00'),(22,'zhangsan',2,'LOGIN','USER',2,'SUCCESS','192.168.1.22','2026-08-03 09:00:15'),(23,'admin',1,'LOGOUT','USER',1,'SUCCESS','192.168.1.10','2026-08-03 18:15:30'),(24,'admin',1,'LOGIN','USER',1,'SUCCESS','192.168.1.10','2026-08-04 08:00:00'),(25,'lisi',3,'LOGIN','USER',3,'SUCCESS','192.168.1.33','2026-08-04 09:30:22'),(26,'admin',1,'CREATE','USER',7,'SUCCESS','192.168.1.10','2026-07-28 10:15:30'),(27,'admin',1,'UPDATE','USER',3,'SUCCESS','192.168.1.10','2026-07-28 14:22:18'),(28,'admin',1,'DELETE','USER',8,'SUCCESS','192.168.1.10','2026-07-29 11:05:40'),(29,'admin',1,'UPDATE','USER',4,'SUCCESS','192.168.1.10','2026-07-29 15:30:12'),(30,'admin',1,'CREATE','USER',9,'SUCCESS','192.168.1.10','2026-07-30 10:40:55'),(31,'admin',1,'UPDATE','USER',5,'FAIL','192.168.1.10','2026-07-30 16:12:33'),(32,'admin',1,'DELETE','USER',10,'SUCCESS','192.168.1.10','2026-07-31 09:20:18'),(33,'admin',1,'UPDATE','USER',2,'SUCCESS','192.168.1.10','2026-08-01 11:45:30'),(34,'admin',1,'CREATE','USER',11,'SUCCESS','192.168.1.10','2026-08-01 14:10:22'),(35,'admin',1,'UPDATE','USER',6,'SUCCESS','192.168.1.10','2026-08-02 10:05:15'),(36,'admin',1,'UPDATE','USER',3,'SUCCESS','192.168.1.10','2026-08-03 13:22:40'),(37,'admin',1,'DELETE','USER',12,'FAIL','192.168.1.10','2026-08-03 16:30:55'),(38,'admin',1,'CREATE','PRODUCT',1,'SUCCESS','192.168.1.10','2026-07-28 11:20:33'),(39,'admin',1,'CREATE','PRODUCT',2,'SUCCESS','192.168.1.10','2026-07-28 13:45:10'),(40,'admin',1,'UPDATE','PRODUCT',1,'SUCCESS','192.168.1.10','2026-07-29 10:30:22'),(41,'admin',1,'CREATE','PRODUCT',3,'SUCCESS','192.168.1.10','2026-07-29 14:15:40'),(42,'admin',1,'UPDATE','PRODUCT',2,'FAIL','192.168.1.10','2026-07-29 16:20:15'),(43,'admin',1,'DELETE','PRODUCT',4,'SUCCESS','192.168.1.10','2026-07-30 09:50:30'),(44,'admin',1,'CREATE','PRODUCT',5,'SUCCESS','192.168.1.10','2026-07-30 15:10:22'),(45,'admin',1,'UPDATE','PRODUCT',3,'SUCCESS','192.168.1.10','2026-07-31 11:40:18'),(46,'admin',1,'CREATE','PRODUCT',6,'SUCCESS','192.168.1.10','2026-07-31 14:25:30'),(47,'admin',1,'UPDATE','PRODUCT',1,'SUCCESS','192.168.1.10','2026-08-01 10:15:50'),(48,'admin',1,'UPDATE','PRODUCT',5,'SUCCESS','192.168.1.10','2026-08-01 16:30:12'),(49,'admin',1,'DELETE','PRODUCT',7,'SUCCESS','192.168.1.10','2026-08-02 09:40:33'),(50,'admin',1,'CREATE','PRODUCT',8,'SUCCESS','192.168.1.10','2026-08-02 14:20:45'),(51,'admin',1,'UPDATE','PRODUCT',6,'FAIL','192.168.1.10','2026-08-03 10:10:20'),(52,'admin',1,'UPDATE','PRODUCT',2,'SUCCESS','192.168.1.10','2026-08-03 15:45:30'),(53,'admin',1,'CREATE','PRODUCT',9,'SUCCESS','192.168.1.10','2026-08-04 10:30:15'),(54,'zhangsan',2,'CREATE','STOCK',1,'SUCCESS','192.168.1.22','2026-07-28 12:15:22'),(55,'lisi',3,'CREATE','STOCK',2,'SUCCESS','192.168.1.33','2026-07-28 15:30:10'),(56,'wangwu',4,'CREATE','STOCK',3,'SUCCESS','192.168.1.44','2026-07-29 10:20:45'),(57,'zhangsan',2,'UPDATE','STOCK',1,'SUCCESS','192.168.1.22','2026-07-29 14:50:30'),(58,'zhaoliu',5,'CREATE','STOCK',4,'SUCCESS','192.168.1.55','2026-07-30 11:10:15'),(59,'lisi',3,'CREATE','STOCK',5,'FAIL','192.168.1.33','2026-07-30 16:40:22'),(60,'qianqi',6,'CREATE','STOCK',6,'SUCCESS','192.168.1.66','2026-07-31 09:30:40'),(61,'wangwu',4,'UPDATE','STOCK',3,'SUCCESS','192.168.1.44','2026-07-31 13:15:50'),(62,'zhangsan',2,'UPDATE','STOCK',2,'SUCCESS','192.168.1.22','2026-08-01 10:45:20'),(63,'zhaoliu',5,'CREATE','STOCK',7,'SUCCESS','192.168.1.55','2026-08-01 14:20:33'),(64,'lisi',3,'UPDATE','STOCK',5,'SUCCESS','192.168.1.33','2026-08-02 11:30:15'),(65,'qianqi',6,'CREATE','STOCK',8,'SUCCESS','192.168.1.66','2026-08-02 15:10:40'),(66,'wangwu',4,'UPDATE','STOCK',4,'FAIL','192.168.1.44','2026-08-03 10:05:22'),(67,'zhangsan',2,'UPDATE','STOCK',1,'SUCCESS','192.168.1.22','2026-08-03 14:40:30'),(68,'zhaoliu',5,'CREATE','STOCK',9,'SUCCESS','192.168.1.55','2026-08-04 09:15:10'),(69,'lisi',3,'UPDATE','STOCK',2,'SUCCESS','192.168.1.33','2026-08-04 11:20:45'),(70,'admin',1,'UPDATE','CONFIG',1,'SUCCESS','192.168.1.10','2026-07-28 09:30:15'),(71,'admin',1,'UPDATE','CONFIG',2,'SUCCESS','192.168.1.10','2026-07-28 09:30:15'),(72,'admin',1,'UPDATE','CONFIG',6,'SUCCESS','192.168.1.10','2026-07-29 10:10:22'),(73,'admin',1,'UPDATE','CONFIG',7,'SUCCESS','192.168.1.10','2026-07-29 10:10:22'),(74,'admin',1,'UPDATE','CONFIG',9,'SUCCESS','192.168.1.10','2026-07-30 14:20:30'),(75,'admin',1,'UPDATE','CONFIG',10,'SUCCESS','192.168.1.10','2026-07-30 14:20:30'),(76,'admin',1,'UPDATE','CONFIG',1,'FAIL','192.168.1.10','2026-07-31 11:15:40'),(77,'admin',1,'UPDATE','CONFIG',3,'SUCCESS','192.168.1.10','2026-08-01 09:50:22'),(78,'admin',1,'UPDATE','CONFIG',4,'SUCCESS','192.168.1.10','2026-08-01 09:50:22'),(79,'admin',1,'UPDATE','CONFIG',8,'SUCCESS','192.168.1.10','2026-08-02 10:30:15'),(80,'admin',1,'UPDATE','CONFIG',9,'SUCCESS','192.168.1.10','2026-08-02 16:45:30'),(81,'admin',1,'UPDATE','CONFIG',2,'SUCCESS','192.168.1.10','2026-08-03 11:20:10'),(82,'admin',1,'UPDATE','CONFIG',5,'SUCCESS','192.168.1.10','2026-08-03 11:20:10'),(83,'admin',1,'UPDATE','CONFIG',10,'FAIL','192.168.1.10','2026-08-04 09:40:33'),(84,'admin',1,'UPDATE','CONFIG',6,'SUCCESS','192.168.1.10','2026-08-04 09:40:33'),(85,'admin',1,'DELETE','LOG',NULL,'SUCCESS','192.168.1.10','2026-08-04 10:00:00'),(86,'zyy',13,'UPDATE','CONFIG',6,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:24:18'),(87,'zyy',13,'UPDATE','CONFIG',7,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:24:18'),(88,'zyy',13,'UPDATE','CONFIG',1,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:24:43'),(89,'zyy',13,'UPDATE','CONFIG',2,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:24:43'),(90,'zyy',13,'UPDATE','CONFIG',3,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:24:43'),(91,'zyy',13,'UPDATE','CONFIG',4,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:24:43'),(92,'zyy',13,'UPDATE','CONFIG',11,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:24:43'),(93,'zyy',13,'UPDATE','CONFIG',1,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:24:47'),(94,'zyy',13,'UPDATE','CONFIG',2,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:24:47'),(95,'zyy',13,'UPDATE','CONFIG',3,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:24:47'),(96,'zyy',13,'UPDATE','CONFIG',4,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:24:47'),(97,'zyy',13,'UPDATE','CONFIG',11,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:24:47'),(98,'zyy',13,'UPDATE','CONFIG',1,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:24:51'),(99,'zyy',13,'UPDATE','CONFIG',2,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:24:51'),(100,'zyy',13,'UPDATE','CONFIG',3,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:24:51'),(101,'zyy',13,'UPDATE','CONFIG',4,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:24:51'),(102,'zyy',13,'UPDATE','CONFIG',11,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:24:51'),(103,'zyy',13,'LOGOUT','USER',13,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:25:26'),(104,'anonymousUser',NULL,'LOGIN','USER',13,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:25:45'),(105,'zyy',13,'UPDATE','CONFIG',1,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:26:02'),(106,'zyy',13,'UPDATE','CONFIG',2,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:26:02'),(107,'zyy',13,'UPDATE','CONFIG',3,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:26:02'),(108,'zyy',13,'UPDATE','CONFIG',4,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:26:02'),(109,'zyy',13,'UPDATE','CONFIG',11,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:26:02'),(110,'zyy',13,'UPDATE','CONFIG',8,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:26:06'),(111,'zyy',13,'UPDATE','CONFIG',9,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:26:06'),(112,'zyy',13,'UPDATE','CONFIG',10,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:26:06'),(113,'zyy',13,'LOGOUT','USER',13,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:26:08'),(114,'anonymousUser',NULL,'REGISTER','USER',18,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:26:16'),(115,'anonymousUser',NULL,'LOGIN','USER',18,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:26:22'),(116,'user',18,'LOGOUT','USER',18,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:29:14'),(117,'anonymousUser',NULL,'LOGIN','USER',13,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:29:20'),(118,'zyy',13,'UPDATE','CONFIG',1,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:30:18'),(119,'zyy',13,'UPDATE','CONFIG',2,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:30:18'),(120,'zyy',13,'UPDATE','CONFIG',3,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:30:18'),(121,'zyy',13,'UPDATE','CONFIG',4,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:30:18'),(122,'zyy',13,'UPDATE','CONFIG',11,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:30:18'),(123,'zyy',13,'UPDATE','CONFIG',14,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:30:18'),(124,'zyy',13,'UPDATE','CONFIG',15,'SUCCESS','0:0:0:0:0:0:0:1','2026-08-04 18:30:18');
/*!40000 ALTER TABLE `system_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart`
--

DROP TABLE IF EXISTS `cart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '数量',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`,`product_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart`
--

LOCK TABLES `cart` WRITE;
/*!40000 ALTER TABLE `cart` DISABLE KEYS */;
/*!40000 ALTER TABLE `cart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(200) NOT NULL COMMENT '密码（加密）',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `wechat_openid` varchar(100) DEFAULT NULL COMMENT '微信OpenID',
  `role` varchar(20) DEFAULT 'USER' COMMENT '角色：ADMIN/USER',
  `status` tinyint DEFAULT '1' COMMENT '状态：0禁用，1启用',
  `deleted` tinyint DEFAULT '0' COMMENT '软删除：0正常，1已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'admin','$2a$10$oR.bbcr/cOhZh7YrSfRAxeibtHDRvgjkDlm/XZYNN.QAa4pKrKd9a','管理员','13800000000','admin@swxt.com',NULL,'ADMIN',1,0),(2,'zhangsan','$2a$10$saKzRRKD7cSV08oSHiUi6e6U2jm5YS.iI5xttZS0AVTOh.W71koGq','张三','13800000001','zhangsan@swxt.com',NULL,'USER',1,0),(3,'lisi','$2a$10$hh8TNSb.DV.Ay5F5sRnZFeGCzP0BTS3kOnkP7Gq1exk.xYIKAC1Vu','李四','13800000002','lisi@swxt.com',NULL,'USER',1,0),(4,'wangwu','$2a$10$Awoa005VoJVQMEIg51AcoOVzTWmXke9WeOTZOsRJw4dV7Ui/EtieK','王五','13800000003','wangwu@swxt.com',NULL,'USER',1,0),(5,'zhaoliu','$2a$10$8LBZzkTTKikiARKhG6NjM.pP75hIE9uTwoYaK3./IVeLnZa5OA2sS','赵六','13800000004','zhaoliu@swxt.com',NULL,'USER',1,0),(6,'qianqi','$2a$10$sopcQac47dO2JVjaCchn1.UPNi6vR4yrA/FgHhlNnQXpvl.hkD6cG','钱七','13800000005','qianqi@swxt.com',NULL,'USER',1,0),(7,'sunba','$2a$10$be6C4lmbyl4fmrxdjdx9keo2x44t9uWv7od2fvHrf9uiO7dgwcpvC','孙八','13800000006','sunba@swxt.com',NULL,'USER',1,0),(8,'zhoujiu','$2a$10$1HolNj.CiRUMBHntp/f4u.uHlPO./I3.ipUgkorSQCpffJhneJjCq','周九','13800000007','zhoujiu@swxt.com',NULL,'USER',1,0),(9,'wushi','$2a$10$6wYvr1FXqHTZJ6Irm54KuudlZyAk1vvNyLWc8lF/TOxlACTJTpSsi','吴十','13800000008','wushi@swxt.com',NULL,'USER',1,0),(10,'user10','$2a$10$tCZGIL4EquL49K5.tD0FVea7hotidwOjnMApQJNiwiCHe48M9xa1q','郑十一','13800000009','user10@swxt.com',NULL,'USER',1,0),(11,'user11','$2a$10$df.J0XYPqKPneZSRlUPKmubpYC0RwJ.nIakyny1clMad/k3NqVdo6','王十二','13800000010','user11@swxt.com',NULL,'USER',1,0),(12,'user12','$2a$10$h8ot9CM/FP7Tm9t3xNjq6uXq6vCXxIJ6PRfFnNA2uOO4lQ9NeyQA2','李十三','13800000011','user12@swxt.com',NULL,'USER',1,0),(13,'zyy','$2a$10$RLio56iXQOnLAxiTNVBpAO.6SJNEi04Mq3p7lFvIdAHgeaWSXPy5S','ZYY','1','2',NULL,'ADMIN',1,0),(15,'123231','$2a$10$Y4jIYQ0OTY2BwLRob4B8EeFxmtyvurW7i3fNG2vpkH2GbmKJbqkB6','213','12313','12313',NULL,'USER',1,1),(16,'aaaa','$2a$10$NkmbujO.Sx1XTvFkUzrW.OSIc5Igc.cquN6WNmGuzLEQPPjv9ddku','d12e','da','qwd',NULL,'USER',1,1),(17,'ad','$2a$10$qZuK170amaV4r7WRrT4yQ.GsluFS8ptfJp8LGA6zmk.dol9tZK/OC','dadddddddddddaaaaaaaaaaaa','da','da',NULL,'USER',1,1),(18,'user','$2a$10$e5bbyO1angD4gwZqKDmu7uh74/Taio11XKLKfmOjqUomjjajyG5f2',NULL,NULL,NULL,NULL,'USER',1,0),(19,'operator','$2a$10$saKzRRKD7cSV08oSHiUi6e6U2jm5YS.iI5xttZS0AVTOh.W71koGq','操作员',NULL,NULL,NULL,'OPERATOR',1,0);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-08-05 13:00:45
