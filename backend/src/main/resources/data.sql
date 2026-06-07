-- Seed users: admin/123456 (ADMIN), user/123456 (USER)
INSERT INTO user (username, password, phone, email, role, created_at)
SELECT * FROM (
    SELECT 'admin' AS username, '$2b$10$ZZq.qM8.NpR19C0zQv8fIeYm1XsqvtSZSs0dw1lDezP5ElUO1zYG6' AS password, '13800000001' AS phone, 'admin@autoparts.com' AS email, 'ADMIN' AS role, NOW() AS created_at
    UNION ALL
    SELECT 'user', '$2b$10$ZZq.qM8.NpR19C0zQv8fIeYm1XsqvtSZSs0dw1lDezP5ElUO1zYG6', '13800000002', 'user@autoparts.com', 'USER', NOW()
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM user LIMIT 1);

-- Seed products with local SVG images
INSERT INTO product (name, category, brand, price, stock, image_url, description, spec, manufacturer, status, version, created_at, updated_at)
SELECT * FROM (
    SELECT '高性能刹车片' AS name, '刹车系统' AS category, '奥迪,大众,宝马' AS brand, 298.00 AS price, 200 AS stock, '/images/products/brake-pads.svg' AS image_url, '陶瓷配方刹车片，耐高温，低噪音，适用于奥迪A4/A6、大众迈腾/帕萨特、宝马3系/5系等车型。' AS description, '陶瓷配方' AS spec, '博世' AS manufacturer, 'ON' AS status, 0 AS version, NOW() AS created_at, NOW() AS updated_at
    UNION ALL
    SELECT '全合成机油 5W-30', '发动机', '通用', 358.00, 500, '/images/products/engine-oil.svg', '全合成发动机机油，5W-30粘度等级，API SP认证，换油周期可达10000公里。' AS description, '5W-30 4L', '美孚' AS manufacturer, 'ON' AS status, 0 AS version, NOW(), NOW()
    UNION ALL
    SELECT '空调滤清器', '滤清器', '通用', 85.00, 300, '/images/products/ac-filter.svg', '活性炭空调滤清器，有效过滤PM2.5、花粉、异味，建议每15000公里或每年更换一次。' AS description, '活性炭 PM2.5', '曼牌' AS manufacturer, 'ON' AS status, 0 AS version, NOW(), NOW()
    UNION ALL
    SELECT '火花塞套装（4支）', '点火系统', '大众,奥迪,奔驰', 168.00, 150, '/images/products/spark-plugs.svg', '双铂金火花塞，使用寿命长达80000公里，点火效率高，节省燃油。' AS description, '双铂金 0.8mm', 'NGK' AS manufacturer, 'ON' AS status, 0 AS version, NOW(), NOW()
    UNION ALL
    SELECT '前轮轴承总成', '底盘系统', '丰田,本田,日产', 420.00, 80, '/images/products/wheel-bearing.svg', '前轮轴承总成，原厂品质，经过严格动平衡测试。' AS description, '前轮 72mm', 'SKF' AS manufacturer, 'ON' AS status, 0 AS version, NOW(), NOW()
    UNION ALL
    SELECT '氧传感器', '传感器', '通用', 245.00, 120, '/images/products/o2-sensor.svg', '宽带氧传感器，精确检测尾气中氧含量，帮助ECU优化空燃比。' AS description, '宽带 5线', '博世' AS manufacturer, 'ON' AS status, 0 AS version, NOW(), NOW()
    UNION ALL
    SELECT '正时皮带套件', '发动机', '大众,奥迪,斯柯达', 680.00, 60, '/images/products/timing-belt.svg', '正时皮带更换套件，含张紧轮和惰轮。建议每80000公里更换。' AS description, '套装含张紧轮', '盖茨' AS manufacturer, 'ON' AS status, 0 AS version, NOW(), NOW()
    UNION ALL
    SELECT '雨刮器片（对装）', '车身附件', '通用', 59.00, 400, '/images/products/wiper-blades.svg', '无骨雨刮器片，静音刮水，耐磨橡胶材质。' AS description, '24寸+19寸 无骨', '博世' AS manufacturer, 'ON' AS status, 0 AS version, NOW(), NOW()
    UNION ALL
    SELECT '减震器（前）', '悬挂系统', '通用', 550.00, 90, '/images/products/shock-absorber.svg', '液压双筒减震器，提升行驶稳定性和舒适性。建议成对更换。' AS description, '液压双筒 前轮', 'KYB' AS manufacturer, 'ON' AS status, 0 AS version, NOW(), NOW()
    UNION ALL
    SELECT '燃油滤清器', '滤清器', '通用', 120.00, 250, '/images/products/fuel-filter.svg', '高压燃油滤清器，有效过滤燃油中的杂质和水分。' AS description, '高压 4.0Bar', '曼牌' AS manufacturer, 'ON' AS status, 0 AS version, NOW(), NOW()
    UNION ALL
    SELECT '发电机皮带', '发动机', '通用', 95.00, 180, '/images/products/serpentine-belt.svg', '多楔带，驱动发电机、空调压缩机、助力转向泵。' AS description, '6PK 2100mm EPDM', '盖茨' AS manufacturer, 'ON' AS status, 0 AS version, NOW(), NOW()
    UNION ALL
    SELECT 'LED大灯灯泡 H7', '车灯照明', '通用', 188.00, 220, '/images/products/led-headlight.svg', 'H7接口LED大灯灯泡，亮度是卤素灯泡的3倍。色温6500K白光。' AS description, 'H7 6500K 55W', '飞利浦' AS manufacturer, 'ON' AS status, 0 AS version, NOW(), NOW()
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM product LIMIT 1);

-- Seed seckill activities
INSERT INTO seckill_activity (product_id, seckill_price, stock, start_time, end_time, created_at)
SELECT * FROM (
    SELECT 1 AS product_id, 199.00 AS seckill_price, 50 AS stock,
           DATE_ADD(NOW(), INTERVAL -1 HOUR) AS start_time,
           DATE_ADD(NOW(), INTERVAL 24 HOUR) AS end_time,
           NOW() AS created_at
    UNION ALL
    SELECT 3, 49.00, 100,
           DATE_ADD(NOW(), INTERVAL -1 HOUR),
           DATE_ADD(NOW(), INTERVAL 24 HOUR),
           NOW()
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM seckill_activity LIMIT 1);
