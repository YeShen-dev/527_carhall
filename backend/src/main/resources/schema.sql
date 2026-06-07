CREATE TABLE IF NOT EXISTS user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)    NOT NULL UNIQUE,
    password    VARCHAR(200)   NOT NULL,
    phone       VARCHAR(20),
    email       VARCHAR(100),
    role        VARCHAR(20)    NOT NULL DEFAULT 'USER',
    avatar      VARCHAR(500)   DEFAULT NULL COMMENT '用户头像URL',
    created_at  DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(200)   NOT NULL,
    category     VARCHAR(100)   NOT NULL,
    brand        VARCHAR(100)   NOT NULL,
    price        DECIMAL(10,2)  NOT NULL,
    stock        INT            NOT NULL,
    image_url    VARCHAR(500),
    description  TEXT,
    spec         VARCHAR(200),
    manufacturer VARCHAR(100),
    status       VARCHAR(10)    DEFAULT 'ON',
    version      INT            DEFAULT 0,
    created_at   DATETIME,
    updated_at   DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_master (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no     VARCHAR(32)    NOT NULL UNIQUE,
    user_id      BIGINT         NOT NULL,
    total_amount DECIMAL(12,2)  NOT NULL,
    status       VARCHAR(20)    NOT NULL DEFAULT 'PENDING',
    pay_method   VARCHAR(20)    COMMENT '支付方式 WECHAT/ALIPAY/BANKCARD',
    payment_no   VARCHAR(64)    COMMENT '支付流水号',
    paid_at      DATETIME       COMMENT '支付完成时间',
    created_at   DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS seckill_activity (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id    BIGINT         NOT NULL,
    seckill_price DECIMAL(10,2)  NOT NULL,
    stock         INT            NOT NULL,
    start_time    DATETIME       NOT NULL,
    end_time      DATETIME       NOT NULL,
    created_at    DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_item (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id      BIGINT         NOT NULL,
    product_id    BIGINT         NOT NULL,
    product_name  VARCHAR(200)   NOT NULL,
    product_image VARCHAR(500),
    price         DECIMAL(10,2)  NOT NULL,
    quantity      INT            NOT NULL,
    subtotal      DECIMAL(10,2)  NOT NULL,
    FOREIGN KEY (order_id) REFERENCES order_master(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS payment_record (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_no    VARCHAR(64) NOT NULL UNIQUE COMMENT '支付流水号（幂等键）',
    order_id      BIGINT NOT NULL COMMENT '订单ID',
    user_id       BIGINT NOT NULL COMMENT '用户ID',
    pay_method    VARCHAR(20) NOT NULL COMMENT '支付方式 WECHAT/ALIPAY/BANKCARD',
    amount        DECIMAL(12,2) NOT NULL COMMENT '支付金额',
    status        VARCHAR(20) NOT NULL DEFAULT 'CREATED' COMMENT 'CREATED/PROCESSING/SUCCESS/FAILED/CLOSED',
    third_trade_no VARCHAR(64) COMMENT '第三方交易号',
    qr_code_url   VARCHAR(500) COMMENT '二维码链接/支付页面URL',
    created_at    DATETIME NOT NULL,
    updated_at    DATETIME,
    INDEX idx_pay_order_id (order_id),
    INDEX idx_pay_user_id (user_id),
    INDEX idx_pay_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS refund_record (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    refund_no       VARCHAR(64) NOT NULL UNIQUE COMMENT '退款流水号',
    payment_no      VARCHAR(64) NOT NULL COMMENT '原支付流水号',
    order_id        BIGINT NOT NULL COMMENT '订单ID',
    user_id         BIGINT NOT NULL COMMENT '用户ID',
    refund_amount   DECIMAL(12,2) NOT NULL COMMENT '退款金额',
    reason          VARCHAR(500) COMMENT '退款原因',
    status          VARCHAR(20) NOT NULL DEFAULT 'PROCESSING' COMMENT 'PROCESSING/SUCCESS/FAILED',
    third_refund_no VARCHAR(64) COMMENT '第三方退款单号',
    created_at      DATETIME NOT NULL,
    updated_at      DATETIME,
    INDEX idx_rf_payment_no (payment_no),
    INDEX idx_rf_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS payment_notify_log (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    payment_no     VARCHAR(64) COMMENT '支付流水号',
    notify_type    VARCHAR(20) NOT NULL COMMENT 'PAY/REFUND',
    notify_body    TEXT NOT NULL COMMENT '回调原始内容',
    decrypt_body   TEXT COMMENT '解密后内容',
    verify_result  TINYINT(1) DEFAULT 0 COMMENT '验签结果 0失败 1成功',
    process_result TINYINT(1) DEFAULT 0 COMMENT '处理结果 0失败 1成功',
    error_msg      VARCHAR(500) COMMENT '错误信息',
    created_at     DATETIME NOT NULL,
    INDEX idx_plog_payment_no (payment_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Add payment columns to order_master (safe with continue-on-error=true)
ALTER TABLE order_master ADD COLUMN pay_method VARCHAR(20) COMMENT '支付方式 WECHAT/ALIPAY/BANKCARD';
ALTER TABLE order_master ADD COLUMN payment_no VARCHAR(64) COMMENT '支付流水号';
ALTER TABLE order_master ADD COLUMN paid_at DATETIME COMMENT '支付完成时间';

-- ============================================================
-- 商品评论系统
-- ============================================================
CREATE TABLE IF NOT EXISTS product_comment (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id    BIGINT NOT NULL COMMENT '商品ID',
    order_id      BIGINT NOT NULL COMMENT '订单ID',
    user_id       BIGINT NOT NULL COMMENT '用户ID',
    username      VARCHAR(50) NOT NULL COMMENT '用户名',
    avatar        VARCHAR(500) COMMENT '头像',
    content       VARCHAR(500) NOT NULL COMMENT '评论内容(10-500字)',
    rating        TINYINT NOT NULL COMMENT '评分 1-5星',
    images        JSON COMMENT '图片URL数组',
    like_count    INT DEFAULT 0 COMMENT '点赞数',
    reply_count   INT DEFAULT 0 COMMENT '回复数',
    is_anonymous  TINYINT(1) DEFAULT 0 COMMENT '是否匿名 0否 1是',
    is_append     TINYINT(1) DEFAULT 0 COMMENT '是否为追评 0首次 1追评',
    status        TINYINT DEFAULT 0 COMMENT '0待审核 1已通过 2已拒绝',
    create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_product_id (product_id),
    INDEX idx_user_id (user_id),
    INDEX idx_order_id (order_id),
    INDEX idx_status (status),
    INDEX idx_rating (rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_comment_reply (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment_id    BIGINT NOT NULL COMMENT '评论ID',
    user_id       BIGINT NOT NULL COMMENT '回复用户ID',
    username      VARCHAR(50) NOT NULL COMMENT '回复用户名',
    avatar        VARCHAR(500) COMMENT '头像',
    content       VARCHAR(500) NOT NULL COMMENT '回复内容',
    parent_id     BIGINT DEFAULT 0 COMMENT '父回复ID 0=直接回复评论',
    is_merchant   TINYINT(1) DEFAULT 0 COMMENT '是否商家回复',
    create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_comment_id (comment_id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_comment_like (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment_id    BIGINT NOT NULL COMMENT '评论ID',
    user_id       BIGINT NOT NULL COMMENT '用户ID',
    create_time   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_comment_user (comment_id, user_id),
    INDEX idx_comment_id (comment_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
