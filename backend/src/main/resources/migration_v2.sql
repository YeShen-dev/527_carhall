-- ============================================================
-- 收货地址表
-- ============================================================
CREATE TABLE IF NOT EXISTS address (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id       BIGINT NOT NULL COMMENT '用户ID',
    receiver_name VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    phone         VARCHAR(20) NOT NULL COMMENT '联系电话',
    province      VARCHAR(50) NOT NULL COMMENT '省',
    city          VARCHAR(50) NOT NULL COMMENT '市',
    district      VARCHAR(50) NOT NULL COMMENT '区',
    detail_address VARCHAR(200) NOT NULL COMMENT '详细地址',
    postal_code   VARCHAR(10) COMMENT '邮编',
    is_default    TINYINT(1) DEFAULT 0 COMMENT '是否默认地址',
    create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 退款/售后表
-- ============================================================
CREATE TABLE IF NOT EXISTS refund_order (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id       BIGINT NOT NULL COMMENT '订单ID',
    user_id        BIGINT NOT NULL COMMENT '用户ID',
    reason         VARCHAR(500) NOT NULL COMMENT '退款原因',
    images         JSON COMMENT '凭证图片',
    refund_amount  DECIMAL(12,2) NOT NULL COMMENT '退款金额',
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/APPROVED/REJECTED/REFUNDING/REFUNDED',
    admin_note     VARCHAR(500) COMMENT '管理员备注',
    create_time    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time    DATETIME ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 轮播图管理表
-- ============================================================
CREATE TABLE IF NOT EXISTS banner (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(100) NOT NULL COMMENT '标题',
    image_url   VARCHAR(500) NOT NULL COMMENT '图片URL',
    link_url    VARCHAR(500) COMMENT '跳转链接',
    sort_order  INT DEFAULT 0 COMMENT '排序',
    enabled     TINYINT(1) DEFAULT 1 COMMENT '是否启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================================
-- 系统日志表
-- ============================================================
CREATE TABLE IF NOT EXISTS system_log (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT COMMENT '操作用户ID',
    username    VARCHAR(50) COMMENT '操作用户名',
    module      VARCHAR(50) COMMENT '操作模块',
    action      VARCHAR(100) COMMENT '操作动作',
    detail      VARCHAR(500) COMMENT '操作详情',
    ip          VARCHAR(50) COMMENT 'IP地址',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
