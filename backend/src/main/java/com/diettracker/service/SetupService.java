package com.diettracker.service;

import com.diettracker.entity.FoodCategory;
import com.diettracker.entity.FoodItem;
import com.diettracker.entity.MealRecord;
import com.diettracker.entity.MealRecord.MealType;
import com.diettracker.repository.FoodCategoryRepository;
import com.diettracker.repository.FoodItemRepository;
import com.diettracker.repository.MealRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SetupService {

    private static final Logger log = LoggerFactory.getLogger(SetupService.class);

    private final FoodCategoryRepository categoryRepo;
    private final FoodItemRepository foodItemRepo;
    private final MealRecordRepository mealRecordRepo;

    public SetupService(FoodCategoryRepository categoryRepo,
                        FoodItemRepository foodItemRepo,
                        MealRecordRepository mealRecordRepo) {
        this.categoryRepo = categoryRepo;
        this.foodItemRepo = foodItemRepo;
        this.mealRecordRepo = mealRecordRepo;
    }

    @Transactional
    public Map<String, Object> seed() {
        Map<String, Object> result = new LinkedHashMap<>();

        if (categoryRepo.count() > 0) {
            log.info("Database already seeded, skipping");
            result.put("skipped", true);
            result.put("message", "Data already exists, seed skipped");
            return result;
        }

        // 1. Seed categories
        FoodCategory[] cats = seedCategories();
        result.put("categories", cats.length);

        // 2. Seed food items
        int foodCount = seedFoodItems(cats);
        result.put("foodItems", foodCount);

        // 3. Seed sample records
        int recordCount = seedSampleRecords();
        result.put("sampleRecords", recordCount);

        log.info("Seed complete: {} categories, {} food items, {} records",
                cats.length, foodCount, recordCount);
        result.put("skipped", false);
        return result;
    }

    private FoodCategory[] seedCategories() {
        FoodCategory[] cats = new FoodCategory[7];
        String[][] data = {
            {"主食", "rice", "1"},
            {"肉类", "meat", "2"},
            {"蔬菜", "vegetable", "3"},
            {"水果", "fruit", "4"},
            {"饮品", "drink", "5"},
            {"零食", "snack", "6"},
            {"其他", "other", "7"},
        };
        for (int i = 0; i < data.length; i++) {
            FoodCategory c = new FoodCategory();
            c.setId((long) (i + 1));
            c.setName(data[i][0]);
            c.setIcon(data[i][1]);
            c.setSortOrder(Integer.parseInt(data[i][2]));
            cats[i] = categoryRepo.save(c);
        }
        return cats;
    }

    private int seedFoodItems(FoodCategory[] cats) {
        // Each row: [name, categoryIndex, unit, calories, protein, fat, carbs]
        String[][] items = {
            // 主食 (cats[0])
            {"米饭", "0", "碗", "232", "2.6", "0.3", "50.0"},
            {"面条", "0", "碗", "280", "8.0", "1.0", "60.0"},
            {"馒头", "0", "个", "223", "7.0", "1.1", "47.0"},
            {"全麦面包", "0", "片", "89", "3.5", "1.2", "16.0"},
            {"白粥", "0", "碗", "88", "2.0", "0.2", "18.5"},
            {"红薯", "0", "个", "116", "1.5", "0.1", "27.0"},
            {"玉米", "0", "根", "112", "4.0", "1.5", "22.0"},
            {"燕麦", "0", "份", "155", "5.5", "2.8", "27.0"},
            // 肉类 (cats[1])
            {"鸡胸肉", "1", "份", "165", "31.0", "3.6", "0.0"},
            {"鸡蛋", "1", "个", "72", "6.5", "4.8", "0.6"},
            {"瘦牛肉", "1", "份", "208", "26.0", "12.0", "0.0"},
            {"猪瘦肉", "1", "份", "245", "27.0", "15.0", "0.0"},
            {"三文鱼", "1", "份", "208", "22.0", "13.0", "0.0"},
            {"虾仁", "1", "份", "85", "18.0", "0.6", "0.0"},
            {"豆腐", "1", "份", "76", "8.0", "4.2", "2.0"},
            {"鸭肉", "1", "份", "240", "22.0", "17.0", "0.0"},
            // 蔬菜 (cats[2])
            {"西兰花", "2", "份", "34", "2.8", "0.4", "6.6"},
            {"菠菜", "2", "份", "23", "2.9", "0.4", "3.6"},
            {"番茄", "2", "个", "18", "0.9", "0.2", "3.9"},
            {"黄瓜", "2", "根", "15", "0.7", "0.1", "3.6"},
            {"生菜", "2", "份", "15", "1.4", "0.2", "2.9"},
            {"胡萝卜", "2", "根", "25", "0.6", "0.1", "5.8"},
            {"蘑菇", "2", "份", "22", "3.1", "0.3", "3.3"},
            {"白菜", "2", "份", "13", "1.5", "0.2", "2.2"},
            // 水果 (cats[3])
            {"苹果", "3", "个", "95", "0.5", "0.3", "25.0"},
            {"香蕉", "3", "根", "105", "1.3", "0.4", "27.0"},
            {"橙子", "3", "个", "62", "1.2", "0.2", "15.0"},
            {"葡萄", "3", "串", "115", "1.0", "0.3", "28.0"},
            {"蓝莓", "3", "份", "57", "0.7", "0.3", "14.5"},
            {"草莓", "3", "份", "55", "1.0", "0.5", "13.0"},
            {"猕猴桃", "3", "个", "42", "0.8", "0.4", "10.0"},
            {"西瓜", "3", "片", "85", "1.7", "0.4", "21.0"},
            // 饮品 (cats[4])
            {"纯牛奶", "4", "杯", "135", "6.5", "7.5", "10.0"},
            {"美式咖啡", "4", "杯", "5", "0.3", "0.0", "0.7"},
            {"拿铁咖啡", "4", "杯", "150", "8.0", "7.0", "14.0"},
            {"绿茶", "4", "杯", "2", "0.1", "0.0", "0.3"},
            {"橙汁", "4", "杯", "110", "1.5", "0.0", "26.0"},
            {"豆浆", "4", "杯", "80", "4.5", "3.0", "7.0"},
            {"酸奶", "4", "杯", "120", "4.0", "3.5", "18.0"},
            {"可乐", "4", "罐", "140", "0.0", "0.0", "35.0"},
            // 零食 (cats[5])
            {"薯片", "5", "份", "275", "3.5", "18.0", "27.0"},
            {"巧克力", "5", "块", "220", "3.0", "14.0", "24.0"},
            {"饼干", "5", "份", "200", "3.0", "8.0", "30.0"},
            {"坚果混合", "5", "份", "175", "5.0", "16.0", "5.0"},
            {"蛋糕", "5", "块", "280", "3.5", "14.0", "36.0"},
            {"冰淇淋", "5", "份", "200", "4.0", "12.0", "22.0"},
            {"果冻", "5", "份", "70", "0.0", "0.0", "17.0"},
            {"爆米花", "5", "份", "110", "2.5", "3.5", "19.0"},
            // 其他 (cats[6])
        };

        int count = 0;
        for (String[] row : items) {
            int catIdx = Integer.parseInt(row[1]);
            FoodItem fi = new FoodItem();
            fi.setName(row[0]);
            fi.setCategory(cats[catIdx]);
            fi.setUnit(row[2]);
            fi.setCalories(new BigDecimal(row[3]));
            fi.setProtein(new BigDecimal(row[4]));
            fi.setFat(new BigDecimal(row[5]));
            fi.setCarbs(new BigDecimal(row[6]));
            foodItemRepo.save(fi);
            count++;
        }
        return count;
    }

    private int seedSampleRecords() {
        LocalDate today = LocalDate.now();
        int count = 0;

        // Today: breakfast - 鸡蛋 + 纯牛奶 + 全麦面包
        count += addRecord(today, MealType.breakfast, "鸡蛋", 1, "08:00", null);
        count += addRecord(today, MealType.breakfast, "纯牛奶", 1, "08:00", null);
        count += addRecord(today, MealType.breakfast, "全麦面包", 2, "08:05", "抹了花生酱");
        // Today: lunch - 米饭 + 鸡胸肉 + 西兰花
        count += addRecord(today, MealType.lunch, "米饭", 1, "12:30", null);
        count += addRecord(today, MealType.lunch, "鸡胸肉", 1.5, "12:30", "奥尔良口味");
        count += addRecord(today, MealType.lunch, "西兰花", 1, "12:30", null);
        // Today: snack - 苹果
        count += addRecord(today, MealType.snack, "苹果", 1, "15:30", null);

        // Yesterday
        LocalDate yesterday = today.minusDays(1);
        // breakfast - 燕麦 + 酸奶
        count += addRecord(yesterday, MealType.breakfast, "燕麦", 1, "07:45", "加了一勺蜂蜜");
        count += addRecord(yesterday, MealType.breakfast, "酸奶", 1, "07:45", null);
        // lunch - 面条 + 鸡蛋 + 菠菜
        count += addRecord(yesterday, MealType.lunch, "面条", 1, "12:00", "清汤面");
        count += addRecord(yesterday, MealType.lunch, "鸡蛋", 1, "12:00", "溏心蛋");
        count += addRecord(yesterday, MealType.lunch, "菠菜", 1, "12:00", null);
        // dinner - 米饭 + 三文鱼 + 番茄 + 蘑菇
        count += addRecord(yesterday, MealType.dinner, "米饭", 0.8, "18:30", null);
        count += addRecord(yesterday, MealType.dinner, "三文鱼", 1, "18:30", "香煎三文鱼");
        count += addRecord(yesterday, MealType.dinner, "番茄", 1, "18:30", "番茄蛋汤");
        count += addRecord(yesterday, MealType.dinner, "蘑菇", 1, "18:30", null);

        // 2 days ago: lunch - 瘦牛肉 + 米饭 + 胡萝卜 + 黄瓜
        LocalDate day2 = today.minusDays(2);
        count += addRecord(day2, MealType.lunch, "米饭", 1, "12:15", null);
        count += addRecord(day2, MealType.lunch, "瘦牛肉", 1, "12:15", "红烧牛肉");
        count += addRecord(day2, MealType.lunch, "胡萝卜", 1, "12:15", "清炒胡萝卜丝");
        count += addRecord(day2, MealType.lunch, "黄瓜", 0.5, "12:15", "凉拌");

        // 4 days ago: dinner - 虾仁 + 西兰花 + 红薯
        LocalDate day4 = today.minusDays(4);
        count += addRecord(day4, MealType.dinner, "虾仁", 1.5, "18:00", "蒜蓉虾仁");
        count += addRecord(day4, MealType.dinner, "西兰花", 1, "18:00", null);
        count += addRecord(day4, MealType.dinner, "红薯", 1, "18:00", null);

        // 6 days ago: dinner - 猪瘦肉 + 白菜 + 米饭 + 豆腐
        LocalDate day6 = today.minusDays(6);
        count += addRecord(day6, MealType.dinner, "米饭", 1, "17:45", null);
        count += addRecord(day6, MealType.dinner, "猪瘦肉", 1, "17:45", "回锅肉");
        count += addRecord(day6, MealType.dinner, "白菜", 1, "17:45", "清炒白菜");
        count += addRecord(day6, MealType.dinner, "豆腐", 1, "17:45", "麻婆豆腐");

        return count;
    }

    private int addRecord(LocalDate date, MealType mealType, String foodName,
                          double quantity, String time, String note) {
        FoodItem food = foodItemRepo.searchFoods(foodName, "")
                .stream().findFirst().orElse(null);
        if (food == null) {
            log.warn("Food not found for sample record: {}", foodName);
            return 0;
        }
        MealRecord r = new MealRecord();
        r.setMealDate(date);
        r.setMealType(mealType);
        r.setFoodItem(food);
        r.setQuantity(BigDecimal.valueOf(quantity));
        r.setUnit(food.getUnit());
        r.setRecordTime(LocalDateTime.parse(date.toString() + "T" + time + ":00"));
        r.setNote(note);
        mealRecordRepo.save(r);
        return 1;
    }
}
