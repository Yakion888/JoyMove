-- =============================================
-- 悦动宝 JoyMove — 数据库初始化脚本
-- =============================================
SET NAMES utf8mb4;

-- ----------------------------
-- 1. 用户表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL,
    `nickname` VARCHAR(50) DEFAULT NULL,
    `password` VARCHAR(255) NOT NULL,
    `avatar` VARCHAR(255) DEFAULT NULL,
    `role` TINYINT DEFAULT 0 COMMENT '0-家长 1-管理员',
    `phone` VARCHAR(20) DEFAULT NULL,
    `city` VARCHAR(50) DEFAULT NULL,
    `bio` VARCHAR(500) DEFAULT NULL,
    `family_role` TINYINT DEFAULT NULL COMMENT '0-爸爸 1-妈妈 2-其他家人',
    `join_date` DATE DEFAULT NULL COMMENT '首次注册日期',
    `total_days` INT DEFAULT 0 COMMENT '累计运动天数',
    `longest_streak` INT DEFAULT 0 COMMENT '最长连续打卡天数',
    `is_deleted` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 2. 孩子信息表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `child_profile` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `name` VARCHAR(50) NOT NULL,
    `gender` TINYINT DEFAULT NULL COMMENT '0-男 1-女',
    `birth_date` DATE DEFAULT NULL,
    `avatar` VARCHAR(255) DEFAULT NULL,
    `is_deleted` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='孩子信息表';

-- ----------------------------
-- 3. 运动项目模板库
-- ----------------------------
CREATE TABLE IF NOT EXISTS `sport_project` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `name` VARCHAR(50) NOT NULL,
    `age_range_min` INT DEFAULT 3,
    `age_range_max` INT DEFAULT 18,
    `duration_min` INT DEFAULT 10,
    `duration_max` INT DEFAULT 60,
    `equipment` VARCHAR(255) DEFAULT NULL,
    `ability_tags` VARCHAR(100) DEFAULT NULL,
    `description` TEXT,
    `difficulty_level` TINYINT DEFAULT 1 COMMENT '1-5',
    `activity_type` TINYINT DEFAULT 1 COMMENT '1-户外 2-室内 3-两者皆可',
    `season_recommend` TINYINT DEFAULT 5 COMMENT '1-春 2-夏 3-秋 4-冬 5-全年',
    `cover_image` VARCHAR(255) DEFAULT NULL,
    `status` TINYINT DEFAULT 1 COMMENT '1-启用 0-禁用',
    `is_deleted` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运动项目模板库';

-- ----------------------------
-- 4. 亲子运动记录表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `family_moment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `project_id` BIGINT DEFAULT NULL,
    `child_id` BIGINT DEFAULT NULL,
    `child_age_at_moment` INT DEFAULT NULL COMMENT '运动时孩子年龄快照',
    `duration` INT DEFAULT NULL COMMENT '实际运动时长(分钟)',
    `location` VARCHAR(100) DEFAULT NULL,
    `content` TEXT COMMENT '文字记录/感受',
    `image_url` VARCHAR(255) DEFAULT NULL COMMENT '照片路径',
    `emotion` TINYINT DEFAULT NULL COMMENT '1-很开心 2-开心 3-一般 4-有点累',
    `stars` TINYINT DEFAULT NULL COMMENT '孩子自评1-5',
    `status` TINYINT DEFAULT 0 COMMENT '0-待审核 1-已发布 2-已驳回',
    `is_public` TINYINT DEFAULT 1 COMMENT '0-仅自己 1-公开',
    `like_count` INT DEFAULT 0,
    `comment_count` INT DEFAULT 0,
    `record_date` DATE DEFAULT NULL COMMENT '运动日期',
    `is_deleted` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_project_id` (`project_id`),
    KEY `idx_child_id` (`child_id`),
    KEY `idx_status` (`status`),
    KEY `idx_record_date` (`record_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='亲子运动记录表';

-- ----------------------------
-- 5. 社区互动表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `community_interaction` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `moment_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `parent_id` BIGINT DEFAULT 0 COMMENT '0=根评论',
    `content` VARCHAR(500) NOT NULL,
    `like_count` INT DEFAULT 0,
    `is_deleted` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_moment_id` (`moment_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区互动表';

-- ----------------------------
-- 6. 用户运动计划表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `family_plan` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `project_id` BIGINT DEFAULT NULL,
    `planned_date` DATE NOT NULL,
    `status` TINYINT DEFAULT 0 COMMENT '0-待执行 1-已完成 2-已跳过',
    `actual_duration` INT DEFAULT NULL,
    `child_feedback` VARCHAR(200) DEFAULT NULL,
    `is_deleted` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_planned_date` (`planned_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户运动计划表';

-- ----------------------------
-- 7. 勋章定义表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `medal` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `medal_code` VARCHAR(30) NOT NULL,
    `medal_name` VARCHAR(50) NOT NULL,
    `medal_icon` VARCHAR(50) DEFAULT NULL,
    `trigger_condition` VARCHAR(100) DEFAULT NULL,
    `condition_type` VARCHAR(50) NOT NULL,
    `condition_value` INT NOT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_medal_code` (`medal_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='勋章定义表';

-- ----------------------------
-- 8. 用户勋章记录表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `medal_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `medal_id` BIGINT NOT NULL,
    `earned_date` DATE DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_medal` (`user_id`, `medal_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户勋章记录表';

-- ----------------------------
-- 9. 每日打卡表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `check_in` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `child_id` BIGINT NOT NULL,
    `check_in_date` DATE NOT NULL,
    `moment_id` BIGINT DEFAULT NULL,
    `streak_days` INT DEFAULT 1,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_child_date` (`user_id`, `child_id`, `check_in_date`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_check_in_date` (`check_in_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日打卡表';

-- ----------------------------
-- 10. 通知表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `notification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `type` TINYINT DEFAULT 1 COMMENT '1-驳回 2-勋章 3-打卡提醒 4-新互动',
    `message` VARCHAR(500) NOT NULL,
    `related_id` BIGINT DEFAULT NULL,
    `is_read` TINYINT DEFAULT 0,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_user_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- ----------------------------
-- 11. 运动统计缓存表
-- ----------------------------
CREATE TABLE IF NOT EXISTS `sport_statistics` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL,
    `stat_date` DATE NOT NULL,
    `activity_count` INT DEFAULT 0,
    `total_duration` INT DEFAULT 0,
    `avg_emotion` DECIMAL(2,1) DEFAULT NULL,
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`, `stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运动统计缓存表';

-- =============================================
-- 种子数据
-- =============================================

-- 默认管理员 (密码: admin123)
INSERT IGNORE INTO `user` (`id`, `username`, `nickname`, `password`, `role`, `city`, `bio`, `family_role`, `join_date`) VALUES
(1, 'admin', '悦动管理员', '$2a$10$EqJsxCKsCgFBfGPhFy5CguQoHzz7VDxteUjZ3PvBqJwGYeMcBUfXO', 1, '北京', '悦动宝平台管理员', NULL, CURDATE());

-- 运动项目模板（20+，按年龄分层）
INSERT IGNORE INTO `sport_project` (`id`, `name`, `age_range_min`, `age_range_max`, `duration_min`, `duration_max`, `equipment`, `ability_tags`, `description`, `difficulty_level`, `activity_type`, `season_recommend`) VALUES
-- 3-5岁
(1,  '动物模仿操',   3,  5,  10, 15, '无',                      '协调性,模仿能力',       '模仿小青蛙跳、小鸟飞、小熊爬等动作，配合儿歌节奏', 1, 2, 5),
(2,  '感统平衡游戏', 3,  5,  15, 20, '枕头,凳子',               '平衡感,专注力',         '用枕头铺成小路，凳子当小桥，练习平衡行走', 1, 2, 5),
(3,  '亲子瑜伽',     3,  5,  10, 15, '瑜伽垫',                  '柔韧性,身体认知',       '猫式、狗式、树式等动物命名瑜伽体式', 1, 2, 5),
(4,  '泡泡追逐赛',   3,  5,  10, 15, '泡泡水',                  '奔跑,手眼协调',         '家长吹泡泡，孩子追逐拍打', 1, 1, 5),
(5,  '积木障碍跑',   3,  5,  15, 20, '积木,小筐',               '体能,规则意识',         '设置简单障碍，孩子搬运积木穿越', 1, 2, 5),
-- 6-8岁
(6,  '家庭障碍赛',   6,  8,  20, 30, '绳子,枕头,椅子',          '体能,规则意识',         '用家中物品搭建障碍赛道，计时穿越', 2, 3, 5),
(7,  '跳绳挑战',     6,  8,  15, 20, '跳绳',                    '协调性,耐力',           '亲子轮流跳绳，记录连续次数', 2, 1, 5),
(8,  '家庭接力跑',   6,  8,  15, 25, '接力棒(可用水瓶代替)',    '团队协作,速度',         '设置折返点，亲子分组接力', 2, 1, 5),
(9,  '飞盘游戏',     6,  8,  20, 30, '飞盘',                    '手眼协调,奔跑',         '练习投掷和接飞盘，逐步增加距离', 2, 1, 3),
(10, '趣味投篮',     6,  8,  20, 30, '小篮球,篮筐(或纸箱)',     '手眼协调,专注力',       '不同距离投篮挑战，计分比赛', 2, 3, 5),
(11, '舞蹈创编',     6,  8,  15, 20, '音乐播放器',              '节奏感,创造力',         '选择喜欢的音乐，亲子共创舞蹈动作', 1, 2, 5),
-- 9-11岁
(12, '亲子定向越野', 9,  11, 30, 45, '地图,指南针(或手机)',     '方向感,逻辑思维',       '制作简易地图，寻找隐藏标记点', 3, 1, 3),
(13, '自行车骑行',   9,  11, 30, 45, '自行车,头盔',             '平衡力,探险精神',       '沿绿道骑行，探索新路线', 3, 1, 3),
(14, '家庭羽毛球',   9,  11, 25, 40, '球拍,羽毛球',             '手眼协调,反应力',       '练习发球、高远球，亲子对打', 3, 3, 5),
(15, '跳绳花式挑战', 9,  11, 15, 25, '跳绳',                    '协调性,创造力',         '学习双摇、交叉跳等花式', 3, 1, 5),
(16, '家庭体能训练', 9,  11, 20, 30, '瑜伽垫',                  '力量,耐力',             '俯卧撑、深蹲、平板支撑循环训练', 3, 2, 5),
(17, '滑板入门',     9,  11, 25, 40, '滑板,护具',               '平衡力,勇气',           '学习上下板、滑行、转弯', 3, 1, 3),
-- 12-14岁
(18, '家庭篮球对抗', 12, 14, 30, 50, '篮球',                    '团队协作,竞技精神',     '2v2或3v3半场对抗', 4, 1, 5),
(19, '徒步登山',     12, 14, 45, 90, '登山鞋,水,背包',          '毅力,自然探索',         '选择城郊山路，亲子徒步登顶', 4, 1, 3),
(20, '家庭游泳',     12, 14, 30, 50, '泳衣,泳镜,泳帽',          '全身运动,勇气',         '练习不同泳姿，亲子计时赛', 4, 1, 2),
(21, '羽毛球比赛',   12, 14, 30, 50, '球拍,羽毛球',             '竞技,反应力',           '正式11分制亲子对决', 4, 3, 5),
(22, '家庭健身挑战', 12, 14, 25, 40, '瑜伽垫,哑铃(可选)',       '力量,体型管理',         'HIIT训练、核心力量练习', 4, 2, 5),
(23, '夜跑训练',     12, 14, 25, 40, '跑鞋,反光衣',             '耐力,自律',             '3-5公里夜跑，配速训练', 4, 1, 5),
(24, '攀岩体验',     12, 14, 40, 60, '攀岩馆门票',              '力量,勇气,策略',        '室内攀岩馆亲子攀岩体验', 5, 2, 5);

-- 勋章定义（10个）
INSERT IGNORE INTO `medal` (`id`, `medal_code`, `medal_name`, `medal_icon`, `trigger_condition`, `condition_type`, `condition_value`) VALUES
(1,  'NEW_SPROUT',     '运动新芽',   '🌱',  '完成第1次运动打卡',    'activity_count',   1),
(2,  'LITTLE_ATHLETE', '小小运动家', '⭐',  '连续打卡7天',          'streak_days',      7),
(3,  'SPORT_STAR',     '运动小达人', '🏅',  '累计运动10次',         'activity_count',   10),
(4,  'ALL_ROUNDER',    '全能小选手', '🌈',  '完成5种不同类型运动',  'category_variety', 5),
(5,  'SPORT_WARRIOR',  '运动小勇士', '🔥',  '连续打卡30天',         'streak_days',      30),
(6,  'BEST_PARTNER',   '最佳拍档',   '👨‍👦', '完成10次亲子运动',     'activity_count',   10),
(7,  'SPORT_CHAMPION', '运动小冠军', '🏆',  '累计运动50次',         'activity_count',   50),
(8,  'WEEK_STREAK',    '坚持一周',   '📅',  '连续打卡7天',          'streak_days',      7),
(9,  'MONTH_STREAK',   '坚持一月',   '💪',  '连续打卡30天',         'streak_days',      30),
(10, 'HAPPY_FAMILY',   '快乐家庭',   '😊',  '累计5次心情为"很开心"', 'emotion_count',    5);
