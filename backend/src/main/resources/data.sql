-- ============================================
-- 预设食物分类
-- ============================================
INSERT INTO food_category (id, name, icon, sort_order, created_at, updated_at) VALUES
(1, '主食', 'rice', 1, NOW(), NOW()),
(2, '肉类', 'meat', 2, NOW(), NOW()),
(3, '蔬菜', 'vegetable', 3, NOW(), NOW()),
(4, '水果', 'fruit', 4, NOW(), NOW()),
(5, '饮品', 'drink', 5, NOW(), NOW()),
(6, '零食', 'snack', 6, NOW(), NOW()),
(7, '其他', 'other', 7, NOW(), NOW());

-- ============================================
-- 预设食物条目
-- ============================================

-- 主食 (category_id = 1)
INSERT INTO food_item (name, category_id, unit, calories, protein, fat, carbs, created_at, updated_at) VALUES
('米饭', 1, '碗', 232, 2.6, 0.3, 50.0, NOW(), NOW()),
('面条', 1, '碗', 280, 8.0, 1.0, 60.0, NOW(), NOW()),
('馒头', 1, '个', 223, 7.0, 1.1, 47.0, NOW(), NOW()),
('全麦面包', 1, '片', 89, 3.5, 1.2, 16.0, NOW(), NOW()),
('白粥', 1, '碗', 88, 2.0, 0.2, 18.5, NOW(), NOW()),
('红薯', 1, '个', 116, 1.5, 0.1, 27.0, NOW(), NOW()),
('玉米', 1, '根', 112, 4.0, 1.5, 22.0, NOW(), NOW()),
('燕麦', 1, '份', 155, 5.5, 2.8, 27.0, NOW(), NOW());

-- 肉类 (category_id = 2)
INSERT INTO food_item (name, category_id, unit, calories, protein, fat, carbs, created_at, updated_at) VALUES
('鸡胸肉', 2, '份', 165, 31.0, 3.6, 0.0, NOW(), NOW()),
('鸡蛋', 2, '个', 72, 6.5, 4.8, 0.6, NOW(), NOW()),
('瘦牛肉', 2, '份', 208, 26.0, 12.0, 0.0, NOW(), NOW()),
('猪瘦肉', 2, '份', 245, 27.0, 15.0, 0.0, NOW(), NOW()),
('三文鱼', 2, '份', 208, 22.0, 13.0, 0.0, NOW(), NOW()),
('虾仁', 2, '份', 85, 18.0, 0.6, 0.0, NOW(), NOW()),
('豆腐', 2, '份', 76, 8.0, 4.2, 2.0, NOW(), NOW()),
('鸭肉', 2, '份', 240, 22.0, 17.0, 0.0, NOW(), NOW());

-- 蔬菜 (category_id = 3)
INSERT INTO food_item (name, category_id, unit, calories, protein, fat, carbs, created_at, updated_at) VALUES
('西兰花', 3, '份', 34, 2.8, 0.4, 6.6, NOW(), NOW()),
('菠菜', 3, '份', 23, 2.9, 0.4, 3.6, NOW(), NOW()),
('番茄', 3, '个', 18, 0.9, 0.2, 3.9, NOW(), NOW()),
('黄瓜', 3, '根', 15, 0.7, 0.1, 3.6, NOW(), NOW()),
('生菜', 3, '份', 15, 1.4, 0.2, 2.9, NOW(), NOW()),
('胡萝卜', 3, '根', 25, 0.6, 0.1, 5.8, NOW(), NOW()),
('蘑菇', 3, '份', 22, 3.1, 0.3, 3.3, NOW(), NOW()),
('白菜', 3, '份', 13, 1.5, 0.2, 2.2, NOW(), NOW());

-- 水果 (category_id = 4)
INSERT INTO food_item (name, category_id, unit, calories, protein, fat, carbs, created_at, updated_at) VALUES
('苹果', 4, '个', 95, 0.5, 0.3, 25.0, NOW(), NOW()),
('香蕉', 4, '根', 105, 1.3, 0.4, 27.0, NOW(), NOW()),
('橙子', 4, '个', 62, 1.2, 0.2, 15.0, NOW(), NOW()),
('葡萄', 4, '串', 115, 1.0, 0.3, 28.0, NOW(), NOW()),
('蓝莓', 4, '份', 57, 0.7, 0.3, 14.5, NOW(), NOW()),
('草莓', 4, '份', 55, 1.0, 0.5, 13.0, NOW(), NOW()),
('猕猴桃', 4, '个', 42, 0.8, 0.4, 10.0, NOW(), NOW()),
('西瓜', 4, '片', 85, 1.7, 0.4, 21.0, NOW(), NOW());

-- 饮品 (category_id = 5)
INSERT INTO food_item (name, category_id, unit, calories, protein, fat, carbs, created_at, updated_at) VALUES
('纯牛奶', 5, '杯', 135, 6.5, 7.5, 10.0, NOW(), NOW()),
('美式咖啡', 5, '杯', 5, 0.3, 0.0, 0.7, NOW(), NOW()),
('拿铁咖啡', 5, '杯', 150, 8.0, 7.0, 14.0, NOW(), NOW()),
('绿茶', 5, '杯', 2, 0.1, 0.0, 0.3, NOW(), NOW()),
('橙汁', 5, '杯', 110, 1.5, 0.0, 26.0, NOW(), NOW()),
('豆浆', 5, '杯', 80, 4.5, 3.0, 7.0, NOW(), NOW()),
('酸奶', 5, '杯', 120, 4.0, 3.5, 18.0, NOW(), NOW()),
('可乐', 5, '罐', 140, 0.0, 0.0, 35.0, NOW(), NOW());

-- 零食 (category_id = 6)
INSERT INTO food_item (name, category_id, unit, calories, protein, fat, carbs, created_at, updated_at) VALUES
('薯片', 6, '份', 275, 3.5, 18.0, 27.0, NOW(), NOW()),
('巧克力', 6, '块', 220, 3.0, 14.0, 24.0, NOW(), NOW()),
('饼干', 6, '份', 200, 3.0, 8.0, 30.0, NOW(), NOW()),
('坚果混合', 6, '份', 175, 5.0, 16.0, 5.0, NOW(), NOW()),
('蛋糕', 6, '块', 280, 3.5, 14.0, 36.0, NOW(), NOW()),
('冰淇淋', 6, '份', 200, 4.0, 12.0, 22.0, NOW(), NOW()),
('果冻', 6, '份', 70, 0.0, 0.0, 17.0, NOW(), NOW()),
('爆米花', 6, '份', 110, 2.5, 3.5, 19.0, NOW(), NOW());

-- ============================================
-- 示例饮食记录（最近一周）
-- ============================================

-- 今天：早餐 - 鸡蛋 + 牛奶 + 全麦面包
INSERT INTO meal_record (meal_date, meal_type, food_item_id, quantity, unit, record_time, note, created_at, updated_at)
SELECT CURDATE(), 'breakfast', f.id, 1.0, f.unit, CONCAT(CURDATE(), ' 08:00:00'), NULL, NOW(), NOW()
FROM food_item f WHERE f.name = '鸡蛋';
INSERT INTO meal_record (meal_date, meal_type, food_item_id, quantity, unit, record_time, note, created_at, updated_at)
SELECT CURDATE(), 'breakfast', f.id, 1.0, f.unit, CONCAT(CURDATE(), ' 08:00:00'), NULL, NOW(), NOW()
FROM food_item f WHERE f.name = '纯牛奶';
INSERT INTO meal_record (meal_date, meal_type, food_item_id, quantity, unit, record_time, note, created_at, updated_at)
SELECT CURDATE(), 'breakfast', f.id, 2.0, f.unit, CONCAT(CURDATE(), ' 08:05:00'), '涂了花生酱', NOW(), NOW()
FROM food_item f WHERE f.name = '全麦面包';

-- 今天：午餐 - 米饭 + 鸡胸肉 + 西兰花
INSERT INTO meal_record (meal_date, meal_type, food_item_id, quantity, unit, record_time, note, created_at, updated_at)
SELECT CURDATE(), 'lunch', f.id, 1.0, f.unit, CONCAT(CURDATE(), ' 12:30:00'), NULL, NOW(), NOW()
FROM food_item f WHERE f.name = '米饭';
INSERT INTO meal_record (meal_date, meal_type, food_item_id, quantity, unit, record_time, note, created_at, updated_at)
SELECT CURDATE(), 'lunch', f.id, 1.5, f.unit, CONCAT(CURDATE(), ' 12:30:00'), '奥尔良口味', NOW(), NOW()
FROM food_item f WHERE f.name = '鸡胸肉';
INSERT INTO meal_record (meal_date, meal_type, food_item_id, quantity, unit, record_time, note, created_at, updated_at)
SELECT CURDATE(), 'lunch', f.id, 1.0, f.unit, CONCAT(CURDATE(), ' 12:30:00'), NULL, NOW(), NOW()
FROM food_item f WHERE f.name = '西兰花';

-- 今天：加餐 - 苹果
INSERT INTO meal_record (meal_date, meal_type, food_item_id, quantity, unit, record_time, note, created_at, updated_at)
SELECT CURDATE(), 'snack', f.id, 1.0, f.unit, CONCAT(CURDATE(), ' 15:30:00'), NULL, NOW(), NOW()
FROM food_item f WHERE f.name = '苹果';

-- 昨天：早餐 - 燕麦 + 酸奶
INSERT INTO meal_record (meal_date, meal_type, food_item_id, quantity, unit, record_time, note, created_at, updated_at)
SELECT DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'breakfast', f.id, 1.0, f.unit, CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 07:45:00'), '加了一勺蜂蜜', NOW(), NOW()
FROM food_item f WHERE f.name = '燕麦';
INSERT INTO meal_record (meal_date, meal_type, food_item_id, quantity, unit, record_time, note, created_at, updated_at)
SELECT DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'breakfast', f.id, 1.0, f.unit, CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 07:45:00'), NULL, NOW(), NOW()
FROM food_item f WHERE f.name = '酸奶';

-- 昨天：午餐 - 面条 + 鸡蛋 + 菠菜
INSERT INTO meal_record (meal_date, meal_type, food_item_id, quantity, unit, record_time, note, created_at, updated_at)
SELECT DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'lunch', f.id, 1.0, f.unit, CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 12:00:00'), '清汤面', NOW(), NOW()
FROM food_item f WHERE f.name = '面条';
INSERT INTO meal_record (meal_date, meal_type, food_item_id, quantity, unit, record_time, note, created_at, updated_at)
SELECT DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'lunch', f.id, 1.0, f.unit, CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 12:00:00'), '溏心蛋', NOW(), NOW()
FROM food_item f WHERE f.name = '鸡蛋';
INSERT INTO meal_record (meal_date, meal_type, food_item_id, quantity, unit, record_time, note, created_at, updated_at)
SELECT DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'lunch', f.id, 1.0, f.unit, CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 12:00:00'), NULL, NOW(), NOW()
FROM food_item f WHERE f.name = '菠菜';

-- 昨天：晚餐 - 米饭 + 三文鱼 + 番茄 + 蘑菇
INSERT INTO meal_record (meal_date, meal_type, food_item_id, quantity, unit, record_time, note, created_at, updated_at)
SELECT DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'dinner', f.id, 0.8, f.unit, CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 18:30:00'), NULL, NOW(), NOW()
FROM food_item f WHERE f.name = '米饭';
INSERT INTO meal_record (meal_date, meal_type, food_item_id, quantity, unit, record_time, note, created_at, updated_at)
SELECT DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'dinner', f.id, 1.0, f.unit, CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 18:30:00'), '香煎三文鱼', NOW(), NOW()
FROM food_item f WHERE f.name = '三文鱼';
INSERT INTO meal_record (meal_date, meal_type, food_item_id, quantity, unit, record_time, note, created_at, updated_at)
SELECT DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'dinner', f.id, 1.0, f.unit, CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 18:30:00'), '番茄蛋汤', NOW(), NOW()
FROM food_item f WHERE f.name = '番茄';
INSERT INTO meal_record (meal_date, meal_type, food_item_id, quantity, unit, record_time, note, created_at, updated_at)
SELECT DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'dinner', f.id, 1.0, f.unit, CONCAT(DATE_SUB(CURDATE(), INTERVAL 1 DAY), ' 18:30:00'), NULL, NOW(), NOW()
FROM food_item f WHERE f.name = '蘑菇';

-- 前天：午餐 - 牛肉 + 米饭 + 胡萝卜 + 黄瓜
INSERT INTO meal_record (meal_date, meal_type, food_item_id, quantity, unit, record_time, note, created_at, updated_at)
SELECT DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'lunch', f.id, 1.0, f.unit, CONCAT(DATE_SUB(CURDATE(), INTERVAL 2 DAY), ' 12:15:00'), NULL, NOW(), NOW()
FROM food_item f WHERE f.name = '米饭';
INSERT INTO meal_record (meal_date, meal_type, food_item_id, quantity, unit, record_time, note, created_at, updated_at)
SELECT DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'lunch', f.id, 1.0, f.unit, CONCAT(DATE_SUB(CURDATE(), INTERVAL 2 DAY), ' 12:15:00'), '红烧牛肉', NOW(), NOW()
FROM food_item f WHERE f.name = '瘦牛肉';
INSERT INTO meal_record (meal_date, meal_type, food_item_id, quantity, unit, record_time, note, created_at, updated_at)
SELECT DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'lunch', f.id, 1.0, f.unit, CONCAT(DATE_SUB(CURDATE(), INTERVAL 2 DAY), ' 12:15:00'), '清炒胡萝卜丝', NOW(), NOW()
FROM food_item f WHERE f.name = '胡萝卜';
INSERT INTO meal_record (meal_date, meal_type, food_item_id, quantity, unit, record_time, note, created_at, updated_at)
SELECT DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'lunch', f.id, 0.5, f.unit, CONCAT(DATE_SUB(CURDATE(), INTERVAL 2 DAY), ' 12:15:00'), '凉拌', NOW(), NOW()
FROM food_item f WHERE f.name = '黄瓜';

-- 4天前：晚餐 - 虾仁 + 西兰花 + 红薯
INSERT INTO meal_record (meal_date, meal_type, food_item_id, quantity, unit, record_time, note, created_at, updated_at)
SELECT DATE_SUB(CURDATE(), INTERVAL 4 DAY), 'dinner', f.id, 1.5, f.unit, CONCAT(DATE_SUB(CURDATE(), INTERVAL 4 DAY), ' 18:00:00'), '蒜蓉虾仁', NOW(), NOW()
FROM food_item f WHERE f.name = '虾仁';
INSERT INTO meal_record (meal_date, meal_type, food_item_id, quantity, unit, record_time, note, created_at, updated_at)
SELECT DATE_SUB(CURDATE(), INTERVAL 4 DAY), 'dinner', f.id, 1.0, f.unit, CONCAT(DATE_SUB(CURDATE(), INTERVAL 4 DAY), ' 18:00:00'), NULL, NOW(), NOW()
FROM food_item f WHERE f.name = '西兰花';
INSERT INTO meal_record (meal_date, meal_type, food_item_id, quantity, unit, record_time, note, created_at, updated_at)
SELECT DATE_SUB(CURDATE(), INTERVAL 4 DAY), 'dinner', f.id, 1.0, f.unit, CONCAT(DATE_SUB(CURDATE(), INTERVAL 4 DAY), ' 18:00:00'), NULL, NOW(), NOW()
FROM food_item f WHERE f.name = '红薯';

-- 6天前：晚餐 - 猪肉 + 白菜 + 米饭 + 豆腐
INSERT INTO meal_record (meal_date, meal_type, food_item_id, quantity, unit, record_time, note, created_at, updated_at)
SELECT DATE_SUB(CURDATE(), INTERVAL 6 DAY), 'dinner', f.id, 1.0, f.unit, CONCAT(DATE_SUB(CURDATE(), INTERVAL 6 DAY), ' 17:45:00'), NULL, NOW(), NOW()
FROM food_item f WHERE f.name = '米饭';
INSERT INTO meal_record (meal_date, meal_type, food_item_id, quantity, unit, record_time, note, created_at, updated_at)
SELECT DATE_SUB(CURDATE(), INTERVAL 6 DAY), 'dinner', f.id, 1.0, f.unit, CONCAT(DATE_SUB(CURDATE(), INTERVAL 6 DAY), ' 17:45:00'), '回锅肉', NOW(), NOW()
FROM food_item f WHERE f.name = '猪瘦肉';
INSERT INTO meal_record (meal_date, meal_type, food_item_id, quantity, unit, record_time, note, created_at, updated_at)
SELECT DATE_SUB(CURDATE(), INTERVAL 6 DAY), 'dinner', f.id, 1.0, f.unit, CONCAT(DATE_SUB(CURDATE(), INTERVAL 6 DAY), ' 17:45:00'), '清炒白菜', NOW(), NOW()
FROM food_item f WHERE f.name = '白菜';
INSERT INTO meal_record (meal_date, meal_type, food_item_id, quantity, unit, record_time, note, created_at, updated_at)
SELECT DATE_SUB(CURDATE(), INTERVAL 6 DAY), 'dinner', f.id, 1.0, f.unit, CONCAT(DATE_SUB(CURDATE(), INTERVAL 6 DAY), ' 17:45:00'), '麻婆豆腐', NOW(), NOW()
FROM food_item f WHERE f.name = '豆腐';
