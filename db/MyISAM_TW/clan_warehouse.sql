/*
Navicat MySQL Data Transfer

Source Server         : LocalHost
Source Server Version : 50402
Source Host           : localhost:3306
Source Database       : l1jdb_tw

Target Server Type    : MYSQL
Target Server Version : 50402
File Encoding         : 65001

Date: 2009-10-13 13:07:55
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `clan_warehouse`
-- ----------------------------
DROP TABLE IF EXISTS `clan_warehouse`;
CREATE TABLE `clan_warehouse` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `clan_name` varchar(45) DEFAULT NULL,
  `item_id` int(11) DEFAULT NULL,
  `item_name` varchar(255) DEFAULT NULL,
  `count` int(11) DEFAULT NULL,
  `is_equipped` int(11) DEFAULT NULL,
  `enchantlvl` int(11) DEFAULT NULL,
  `is_id` int(11) DEFAULT NULL,
  `durability` int(11) DEFAULT NULL,
  `charge_count` int(11) DEFAULT NULL,
  `remaining_time` int(11) DEFAULT NULL,
  `last_used` datetime DEFAULT NULL,
  `bless` int(11) DEFAULT NULL,
  `attr_enchant_kind` int(11) DEFAULT NULL,
  `attr_enchant_level` int(11) DEFAULT NULL,
  `addsp` int(11) DEFAULT '0',
  `addhp` int(11) DEFAULT '0',
  `addmp` int(11) DEFAULT '0',
  `hpr` int(11) DEFAULT '0',
  `mpr` int(11) DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `key_id` (`clan_name`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of clan_warehouse
-- ----------------------------
