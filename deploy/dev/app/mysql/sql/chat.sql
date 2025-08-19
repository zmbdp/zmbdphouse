use zmbdphouse_dev;
drop table if exists `session`;
CREATE TABLE `session`  (
  `id` bigint(20) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `user_id1` bigint(20) NOT NULL COMMENT '用户1',
  `user_id2` bigint(20) NOT NULL COMMENT '用户2',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_session`(`user_id1`, `user_id2`) USING BTREE COMMENT '确保任意两个用户之间的会话是唯一的，意味着 (A, B) 和 (B, A) 会被认为是同一个会话。'
) ENGINE = InnoDB AUTO_INCREMENT = 10000001 CHARACTER SET = utf8mb4 comment = '咨询会话表';

drop table if exists `message`;
CREATE TABLE `message`  (
  `id` bigint(20) UNSIGNED NOT NULL COMMENT '消息id',
  `from_id` bigint(20) UNSIGNED NOT NULL COMMENT '发送方id',
  `session_id` bigint(20) UNSIGNED NOT NULL COMMENT '会话id',
  `type` tinyint(1) UNSIGNED NOT NULL DEFAULT 1 COMMENT '消息类型',
  `content` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT '' COMMENT '消息正文',
  `visited` tinyint(2) NOT NULL COMMENT '消息是否浏览过  10未浏览 11未浏览',
  `status` tinyint(1) NOT NULL DEFAULT 0 COMMENT '消息状态 0未读 1已读',
  `create_time` bigint(20) NOT NULL COMMENT '消息发送时间（毫秒时间戳）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_from_id`(`from_id`) USING BTREE,
  INDEX `idx_session_id`(`session_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COMMENT = '消息表';