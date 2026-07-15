-- Provide a usable catalog on every new MySQL database while preserving any
-- system foods already present in an upgraded legacy database.
CREATE TEMPORARY TABLE system_food_seed (
    name VARCHAR(100) NOT NULL,
    category_name VARCHAR(50) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    calories NUMERIC(10,2) NOT NULL,
    protein NUMERIC(10,2) NOT NULL,
    fat NUMERIC(10,2) NOT NULL,
    carbs NUMERIC(10,2) NOT NULL,
    base_amount NUMERIC(10,2) NOT NULL,
    base_unit VARCHAR(20) NOT NULL,
    serving_amount NUMERIC(10,2),
    serving_unit VARCHAR(20),
    source VARCHAR(30) NOT NULL
);

INSERT INTO system_food_seed VALUES
('米饭', '主食', '碗', 116, 2.6, 0.3, 25.9, 100, 'g', NULL, NULL, 'SYSTEM'),
('面条', '主食', '碗', 280, 8.0, 1.0, 60.0, 1, '碗', 1, '碗', 'LEGACY_SYSTEM'),
('馒头', '主食', '个', 223, 7.0, 1.1, 47.0, 1, '个', 1, '个', 'LEGACY_SYSTEM'),
('全麦面包', '主食', '片', 89, 3.5, 1.2, 16.0, 1, '片', 1, '片', 'LEGACY_SYSTEM'),
('白粥', '主食', '碗', 88, 2.0, 0.2, 18.5, 1, '碗', 1, '碗', 'LEGACY_SYSTEM'),
('红薯', '主食', '个', 116, 1.5, 0.1, 27.0, 1, '个', 1, '个', 'LEGACY_SYSTEM'),
('玉米', '主食', '根', 112, 4.0, 1.5, 22.0, 1, '根', 1, '根', 'LEGACY_SYSTEM'),
('燕麦', '主食', '份', 155, 5.5, 2.8, 27.0, 1, '份', 1, '份', 'LEGACY_SYSTEM'),
('鸡胸肉', '肉类', '份', 165, 31.0, 3.6, 0.0, 100, 'g', NULL, NULL, 'SYSTEM'),
('鸡蛋', '肉类', '个', 144, 13.3, 8.8, 2.8, 100, 'g', NULL, NULL, 'SYSTEM'),
('瘦牛肉', '肉类', '份', 208, 26.0, 12.0, 0.0, 1, '份', 1, '份', 'LEGACY_SYSTEM'),
('猪瘦肉', '肉类', '份', 245, 27.0, 15.0, 0.0, 1, '份', 1, '份', 'LEGACY_SYSTEM'),
('三文鱼', '肉类', '份', 208, 22.0, 13.0, 0.0, 1, '份', 1, '份', 'LEGACY_SYSTEM'),
('虾仁', '肉类', '份', 85, 18.0, 0.6, 0.0, 1, '份', 1, '份', 'LEGACY_SYSTEM'),
('豆腐', '肉类', '份', 76, 8.0, 4.2, 2.0, 1, '份', 1, '份', 'LEGACY_SYSTEM'),
('鸭肉', '肉类', '份', 240, 22.0, 17.0, 0.0, 1, '份', 1, '份', 'LEGACY_SYSTEM'),
('西兰花', '蔬菜', '份', 36, 4.1, 0.6, 4.3, 100, 'g', NULL, NULL, 'SYSTEM'),
('菠菜', '蔬菜', '份', 23, 2.9, 0.4, 3.6, 1, '份', 1, '份', 'LEGACY_SYSTEM'),
('番茄', '蔬菜', '个', 18, 0.9, 0.2, 3.9, 1, '个', 1, '个', 'LEGACY_SYSTEM'),
('黄瓜', '蔬菜', '根', 15, 0.7, 0.1, 3.6, 1, '根', 1, '根', 'LEGACY_SYSTEM'),
('生菜', '蔬菜', '份', 15, 1.4, 0.2, 2.9, 1, '份', 1, '份', 'LEGACY_SYSTEM'),
('胡萝卜', '蔬菜', '根', 25, 0.6, 0.1, 5.8, 1, '根', 1, '根', 'LEGACY_SYSTEM'),
('蘑菇', '蔬菜', '份', 22, 3.1, 0.3, 3.3, 1, '份', 1, '份', 'LEGACY_SYSTEM'),
('白菜', '蔬菜', '份', 13, 1.5, 0.2, 2.2, 1, '份', 1, '份', 'LEGACY_SYSTEM'),
('苹果', '水果', '个', 95, 0.5, 0.3, 25.0, 1, '个', 1, '个', 'LEGACY_SYSTEM'),
('香蕉', '水果', '根', 105, 1.3, 0.4, 27.0, 1, '根', 1, '根', 'LEGACY_SYSTEM'),
('橙子', '水果', '个', 62, 1.2, 0.2, 15.0, 1, '个', 1, '个', 'LEGACY_SYSTEM'),
('葡萄', '水果', '串', 115, 1.0, 0.3, 28.0, 1, '串', 1, '串', 'LEGACY_SYSTEM'),
('蓝莓', '水果', '份', 57, 0.7, 0.3, 14.5, 1, '份', 1, '份', 'LEGACY_SYSTEM'),
('草莓', '水果', '份', 55, 1.0, 0.5, 13.0, 1, '份', 1, '份', 'LEGACY_SYSTEM'),
('猕猴桃', '水果', '个', 42, 0.8, 0.4, 10.0, 1, '个', 1, '个', 'LEGACY_SYSTEM'),
('西瓜', '水果', '片', 85, 1.7, 0.4, 21.0, 1, '片', 1, '片', 'LEGACY_SYSTEM'),
('纯牛奶', '饮品', '杯', 135, 6.5, 7.5, 10.0, 1, '杯', 1, '杯', 'LEGACY_SYSTEM'),
('美式咖啡', '饮品', '杯', 5, 0.3, 0.0, 0.7, 1, '杯', 1, '杯', 'LEGACY_SYSTEM'),
('拿铁咖啡', '饮品', '杯', 150, 8.0, 7.0, 14.0, 1, '杯', 1, '杯', 'LEGACY_SYSTEM'),
('绿茶', '饮品', '杯', 2, 0.1, 0.0, 0.3, 1, '杯', 1, '杯', 'LEGACY_SYSTEM'),
('橙汁', '饮品', '杯', 110, 1.5, 0.0, 26.0, 1, '杯', 1, '杯', 'LEGACY_SYSTEM'),
('豆浆', '饮品', '杯', 80, 4.5, 3.0, 7.0, 1, '杯', 1, '杯', 'LEGACY_SYSTEM'),
('酸奶', '饮品', '杯', 62, 3.1, 3.4, 4.8, 100, 'g', NULL, NULL, 'SYSTEM'),
('可乐', '饮品', '罐', 140, 0.0, 0.0, 35.0, 1, '罐', 1, '罐', 'LEGACY_SYSTEM'),
('薯片', '零食', '份', 275, 3.5, 18.0, 27.0, 1, '份', 1, '份', 'LEGACY_SYSTEM'),
('巧克力', '零食', '块', 220, 3.0, 14.0, 24.0, 1, '块', 1, '块', 'LEGACY_SYSTEM'),
('饼干', '零食', '份', 200, 3.0, 8.0, 30.0, 1, '份', 1, '份', 'LEGACY_SYSTEM'),
('坚果混合', '零食', '份', 175, 5.0, 16.0, 5.0, 1, '份', 1, '份', 'LEGACY_SYSTEM'),
('蛋糕', '零食', '块', 280, 3.5, 14.0, 36.0, 1, '块', 1, '块', 'LEGACY_SYSTEM'),
('冰淇淋', '零食', '份', 200, 4.0, 12.0, 22.0, 1, '份', 1, '份', 'LEGACY_SYSTEM'),
('果冻', '零食', '份', 70, 0.0, 0.0, 17.0, 1, '份', 1, '份', 'LEGACY_SYSTEM'),
('爆米花', '零食', '份', 110, 2.5, 3.5, 19.0, 1, '份', 1, '份', 'LEGACY_SYSTEM');

INSERT INTO food_item (
    name, category_id, unit, calories, protein, fat, carbs,
    base_amount, base_unit, serving_amount, serving_unit, source, user_id
)
SELECT
    seed.name, category.id, seed.unit, seed.calories, seed.protein, seed.fat, seed.carbs,
    seed.base_amount, seed.base_unit, seed.serving_amount, seed.serving_unit, seed.source, NULL
FROM system_food_seed seed
JOIN (
    SELECT name, MIN(id) AS id
    FROM food_category
    GROUP BY name
) category ON category.name = seed.category_name
LEFT JOIN food_item existing
    ON existing.name = seed.name AND existing.user_id IS NULL
WHERE existing.id IS NULL;

DROP TEMPORARY TABLE system_food_seed;
