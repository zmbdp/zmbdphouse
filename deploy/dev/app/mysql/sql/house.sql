DROP TABLE IF EXISTS `house`;
CREATE TABLE `house`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `user_id` bigint(20) UNSIGNED NOT NULL COMMENT '房东id',
  `title` varchar(50) NOT NULL COMMENT '标题',
  `rent_type` varchar(20) NOT NULL COMMENT '租房类型',
  `floor` int(11) NOT NULL COMMENT '所在楼层',
  `all_floor` int(11) NOT NULL COMMENT '总楼层',
  `house_type` varchar(20) NOT NULL COMMENT '户型',
  `rooms` varchar(20) NOT NULL COMMENT '居室',
  `position` varchar(20) NOT NULL COMMENT '朝向',
  `area` decimal(10, 2) NOT NULL COMMENT '面积（平方米）',
  `price` decimal(10, 2) NOT NULL COMMENT '价格（元）',
  `intro` varchar(2047) NOT NULL COMMENT '房屋介绍',
  `devices` varchar(255) NOT NULL COMMENT '设备',
  `head_image` varchar(110) NOT NULL COMMENT '头图',
  `images` varchar(2047) NULL DEFAULT NULL COMMENT '房源图',
  `city_id` bigint(20) NOT NULL COMMENT '城市id',
  `city_name` varchar(40) NOT NULL COMMENT '城市名',
  `region_id` bigint(20) NOT NULL COMMENT '区域id',
  `region_name` varchar(40) NOT NULL COMMENT '区域名',
  `community_name` varchar(40) NOT NULL COMMENT '社区名',
  `detail_address` varchar(255) NOT NULL COMMENT '详细地址',
  `longitude` decimal(10, 7) NOT NULL COMMENT '经度',
  `latitude` decimal(10, 7) NOT NULL COMMENT '纬度',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id`) USING BTREE,
  INDEX `idx_rent_type`(`rent_type`) USING BTREE,
  INDEX `idx_title`(`title`) USING BTREE,
  INDEX `idx_community_name`(`community_name`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1000001 CHARACTER SET = utf8mb4 comment = '房源表';

DROP TABLE IF EXISTS `house_status`;
CREATE TABLE `house_status`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `house_id` bigint(20) NOT NULL COMMENT '房源id',
  `status` varchar(10) NOT NULL COMMENT '房源状态',
  `rent_time_code` varchar(20) NULL DEFAULT NULL COMMENT '出租时长（字典配置）',
  `rent_start_time` bigint(20) NULL DEFAULT NULL COMMENT '开始出租时间（时间戳）',
  `rent_end_time` bigint(20) NULL DEFAULT NULL COMMENT '结束出租时间（时间戳）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_house_id`(`house_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1000001 CHARACTER SET = utf8mb4 comment = '房源状态表';

DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `tag_code` varchar(20) NOT NULL COMMENT '标签码',
  `tag_name` varchar(20) NOT NULL COMMENT '标签名',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tag_code`(`tag_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1000001 CHARACTER SET = utf8mb4 comment = '标签表';


DROP TABLE IF EXISTS `tag_house`;
CREATE TABLE `tag_house`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `tag_code` varchar(20) NOT NULL COMMENT '标签码',
  `house_id` bigint(20) NOT NULL COMMENT '房源id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tag_code`(`tag_code`) USING BTREE,
  INDEX `idx_house_id`(`house_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1000001 CHARACTER SET = utf8mb4 comment = '标签房源关系表';

DROP TABLE IF EXISTS `city_house`;
CREATE TABLE `city_house`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键id',
  `city_id` bigint(20) NOT NULL COMMENT '城市id',
  `city_name` varchar(40) NOT NULL COMMENT '城市名',
  `house_id` bigint(20) NOT NULL COMMENT '房源id',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_city_id`(`city_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1000001 CHARACTER SET = utf8mb4 comment = '城市房源关系表';