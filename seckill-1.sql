/*
 Navicat Premium Data Transfer

 Source Server         : localhost_MySQL
 Source Server Type    : MySQL
 Source Server Version : 80021
 Source Host           : localhost:3306
 Source Schema         : seckill

 Target Server Type    : MySQL
 Target Server Version : 80021
 File Encoding         : 65001

 Date: 15/11/2020 13:49:53
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for seckill_activity
-- ----------------------------
DROP TABLE IF EXISTS `seckill_activity`;
CREATE TABLE `seckill_activity`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT COMMENT 'the ID of commodity',
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'the name of commodity',
  `commodity_id` bigint NOT NULL,
  `old_price` decimal(10, 2) NOT NULL COMMENT 'original price',
  `seckill_price` decimal(10, 2) NOT NULL COMMENT 'flash sale price',
  `activity_status` int NOT NULL DEFAULT 0 COMMENT 'the status of seckill activity，0:take off from stock 1:normal',
  `start_time` datetime(0) NULL DEFAULT NULL COMMENT 'the start time of seckill activity',
  `end_time` datetime(0) NULL DEFAULT NULL COMMENT 'the end time of seckill activity',
  `total_stock` bigint UNSIGNED NOT NULL COMMENT 'total stock',
  `available_stock` int NOT NULL COMMENT 'available stock',
  `lock_stock` bigint UNSIGNED NOT NULL DEFAULT 0 COMMENT 'lock the number of stock',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `id`(`id`, `name`, `commodity_id`) USING BTREE,
  INDEX `name`(`name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of seckill_activity
-- ----------------------------
INSERT INTO `seckill_activity` VALUES (1, 'test1', 999, 2.88, 99.00, 0, '2020-11-01 19:21:20', NULL, 0, 0, 0);
INSERT INTO `seckill_activity` VALUES (2, 'test2', 999, 3.88, 99.00, 0, '2020-11-01 19:21:20', NULL, 0, 0, 0);
INSERT INTO `seckill_activity` VALUES (3, 'test3', 999, 8.99, 99.00, 0, '2020-11-01 19:21:20', NULL, 0, 0, 0);
INSERT INTO `seckill_activity` VALUES (4, 'test4', 999, 0.00, 99.00, 0, '2020-11-01 19:21:20', NULL, 0, 0, 0);
INSERT INTO `seckill_activity` VALUES (5, 'test5', 999, 0.00, 99.00, 0, '2020-11-01 19:21:20', NULL, 0, 0, 0);
INSERT INTO `seckill_activity` VALUES (6, 'test6', 999, 0.00, 99.00, 0, '2020-11-01 19:21:20', NULL, 0, 0, 0);
INSERT INTO `seckill_activity` VALUES (7, 'test', 999, 0.00, 99.00, 16, '2020-11-01 19:21:20', NULL, 0, 0, 0);
INSERT INTO `seckill_activity` VALUES (8, 'test', 999, 0.00, 99.00, 16, '2020-11-01 19:21:20', NULL, 0, 0, 0);
INSERT INTO `seckill_activity` VALUES (9, 'macbook air', 999, 99.99, 88.88, 1, '2020-11-01 19:21:20', NULL, 0, 0, 0);
INSERT INTO `seckill_activity` VALUES (10, 'iPhone 14 Pro cell phone deep purple 256GB', 999, 8769.00, 7769.00, 1, '2020-11-01 19:21:20', NULL, 0, 0, 0);
INSERT INTO `seckill_activity` VALUES (11, 'iPhone 14 Pro cell phone deep purple 256GB', 999, 8769.00, 7769.00, 1, '2020-11-01 19:21:20', '2020-11-20 16:50:40', 10, 0, 0);
INSERT INTO `seckill_activity` VALUES (12, 'iPhone 14 Pro cell phone deep purple 256GB', 999, 99.99, 88.88, 1, '2020-11-01 19:21:20', '2020-11-18 16:50:33', 10, 0, 0);
INSERT INTO `seckill_activity` VALUES (19, 'iPhone14 Pro flash sale activity', 1001, 7888.00, 5888.00, 1, '2020-11-05 08:39:24', '2020-11-05 08:39:24', 10, 9, 0);

-- ----------------------------
-- Table structure for seckill_commodity
-- ----------------------------
DROP TABLE IF EXISTS `seckill_commodity`;
CREATE TABLE `seckill_commodity`  (
  `id` bigint UNSIGNED NOT NULL AUTO_INCREMENT,
  `commodity_name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `commodity_desc` varchar(1000) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `commodity_price` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1002 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of seckill_commodity
-- ----------------------------
INSERT INTO `seckill_commodity` VALUES (11, '11', '11', 100);
INSERT INTO `seckill_commodity` VALUES (12, '11', '11', 100);
INSERT INTO `seckill_commodity` VALUES (999, 'iphone 14 pro', 'Super Retina XDR display¹, Dynamic Island, Emergency SOS via satellitefootnote³, Pro camera system, Up to 29 hours, A16 Bionic chip, Face ID, Superfast 5G cellularfootnote⁵', 999);
INSERT INTO `seckill_commodity` VALUES (1001, 'iphone 12 pro', 'A total powerhouse, iphone12 pro best for you', 9999);

