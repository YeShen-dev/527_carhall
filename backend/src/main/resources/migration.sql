-- ============================================================
-- 数据库升级脚本 — avatar 字段迁移
-- 用途: 针对已存在的 user 表添加 avatar 列
-- 安全: IF NOT EXISTS 语法仅在 MySQL 8.0+ 支持,
--       低版本使用 information_schema 判断
-- ============================================================

-- 方法1: MySQL 8.0+ 原生语法 (推荐)
ALTER TABLE user ADD COLUMN IF NOT EXISTS avatar VARCHAR(500) DEFAULT NULL COMMENT '用户头像URL';

-- 方法2: 通用方法 (MySQL 5.7 兼容)
-- 如果上面语句执行失败, 用下面存储过程替代:
/*
DELIMITER $$
CREATE PROCEDURE IF NOT EXISTS add_avatar_column()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'user'
          AND COLUMN_NAME = 'avatar'
    ) THEN
        ALTER TABLE user ADD COLUMN avatar VARCHAR(500) DEFAULT NULL COMMENT '用户头像URL';
    END IF;
END$$
DELIMITER ;
CALL add_avatar_column();
DROP PROCEDURE IF EXISTS add_avatar_column;
*/
