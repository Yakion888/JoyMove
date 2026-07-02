-- =============================================
-- 悦动宝 — Demo 数据 + 补缺表（Docker 部署用）
-- =============================================

-- 补缺：init.sql 遗漏的 moment_like 表
CREATE TABLE IF NOT EXISTS `moment_like` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `moment_id` BIGINT NOT NULL COMMENT '运动记录ID',
    `user_id` BIGINT NOT NULL COMMENT '点赞用户ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_moment_user` (`moment_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';

-- 测试用户 mama / admin123
INSERT IGNORE INTO user (username, nickname, password, role, family_role, join_date, total_days, longest_streak) VALUES
('mama', '乐乐妈妈', '$2a$10$DfTO/yl7AgRRzdtYkPFkoesFSG3bvOJUqVVqsxf/idk8JQkiJIMvS', 0, 1, '2026-05-01', 20, 7);

INSERT IGNORE INTO child_profile (user_id, name, gender, birth_date)
SELECT id, '乐乐', 0, '2020-06-15' FROM user WHERE username = 'mama';

-- 管理员 admin / admin123
INSERT IGNORE INTO user (username, nickname, password, role, family_role, join_date, total_days, longest_streak) VALUES
('admin', '系统管理员', '$2a$10$DfTO/yl7AgRRzdtYkPFkoesFSG3bvOJUqVVqsxf/idk8JQkiJIMvS', 1, NULL, '2026-01-01', 0, 0);
