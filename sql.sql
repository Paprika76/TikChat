CREATE TABLE `group_info`(
    `group_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '群ID',
    `group_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '群组名',
    `group_owner_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '群主id',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `group_notice` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT  NULL COMMENT '群公告',
    `join_type` tinyint(1) NULL DEFAULT NULL COMMENT '0:直接加入 1:管理员同意后加入',
    `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态 1:正常 0:解散',
    PRIMARY KEY (`group_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

CREATE TABLE `user_contact` (
    `user_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户ID',
    `contact_id`  varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '联系人ID或者群组ID ',
    `contact_type` tinyint(1) NULL DEFAULT NULL COMMENT '联系人类型o:好友1:群组',
    `create_time`  datetime NULL DEFAULT NULL COMMENT '创建时间',
    `status` tinyint(1) NULL DEFAULT NULL COMMENT '状态0:非好友1:好友2:已删除3:拉黑',
    `last_update_time` timestamp NULL DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
        PRIMARY KEY (`user_id`, `contact_id`) USING BTREE,
    INDEX `idx_contact_id` (`contact_id`) USING BTREE

) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '联系人' ROW_FORMAT = DYNAMIC;


CREATE TABLE `user_contact_apply` (
    `apply_id` int(12) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
    `apply_user_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '申请人ID',
    `receive_user_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'ID',
    `contact_type` tinyint(1) NOT NULL COMMENT '联系人类型g:好友1：群组',
    `contact_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系人群组ID',
    `last_apply_time` bigint(20) NULL DEFAULT NULL COMMENT '最后申请时间',
    `status` tinyint(1) NULL DEFAULT NULL COMMENT '状态g:待处理1：已同意2：已拒绝3：已拉黑',
    `apply_info` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '申请信息',
    PRIMARY KEY (`apply_id`) USING BTREE,
    UNIQUE INDEX `idx_key` (`apply_user_id`,`receive_user_id`,`contact_id`) USING BTREE,
    INDEX `idx_last_apply_time` (`last_apply_time`) USING BTREE
) ENGINE InnoDB AUTO_INCREMENT 136886 CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci COMMENT = '联系人申请' ROW_FORMAT=DYNAMIC;



CREATE TABLE `app_update`(
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT'自增ID',
    `version` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '版本号',
    `update_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新描述',
    `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
    `status` tinyint(1) NULL DEFAULT NULL COMMENT 'e:未发布1:灰度发布2:全网发布',
    `grayscale_uid` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '灰度uid',
    `file_type` tinyint(1) NULL DEFAULT NULL COMMENT '文件类型0:本地文件1:外链',
    `outer_link` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '外链地址',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = 'app发布';

CREATE TABLE `chat_session` (
    `session_id` varchar (32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '会话ID',
    `last_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '最后接受的消息',
    `last_receive_time` bigint(11) NULL DEFAULT NULL COMMENT '最后接受消息时间毫秒',
    PRIMARY KEY (`session_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '会话信息' ROW_FORMAT=DYNAMIC;

CREATE TABLE `chat_message` (
    `message_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息自增ID',
    `session_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '会话T0',
    `message_type` tinyint(1) NOT NULL COMMENT '消息类型',
    `message_content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '消息内容',
    `send_user_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发送人TD',
    `send_user_nick_name` varchar(20) CHARACTER SET utf8mb4 coLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '发送人昵称',
    `send_time` bigint(20) NULL DEFAULT NULL COMMENT '发送时间',
    `contact_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '接收联系人TD',
    `contact_type` tinyint(1) NULL DEFAULT NULL COMMENT '联系人类型0:单聊―1:群聊',
    `file_size` bigint(20) NULL DEFAULT NULL COMMENT '文件大小',
    `file_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '文件名',
    `file_type` tinyint(1) NULL DEFAULT NULL COMMENT '文件类型',
    `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态0:正在发送 1:已发送',
    PRIMARY KEY (message_id ) USING BTREE,
    INDEX `idx_session_id` ( `session_id`) USING BTREE,
    INDEX `idx_send_user_id` ( `send_user_id`) USING BTREE,
    INDEX `idx_receive_contact_id` (`contact_id`) USING BTREE,
    INDEX `idx_send_time` (`send_time`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1545 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '聊天消息表';


CREATE TABLE `chat_session_user`(
    `user_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户TD',
    `contact_id` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '联系人ITo',
    `session_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '会话TD',
    `contact_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '联系人名称',
    PRIMARY KEY ( `user_id`, `contact_id`) USING BTREE,
    INDEX idx_user_id ( `user_id` ) USING BTREE,
    INDEX idx_session_id ( `session_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '会话用户';



