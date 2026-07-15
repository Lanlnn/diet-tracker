package com.diettracker.service;

import com.diettracker.api.ApiException;
import com.diettracker.entity.MealRecord;
import com.diettracker.entity.FoodItem;
import com.diettracker.dto.CreateMealRecordRequest;
import com.diettracker.repository.FoodCategoryRepository;
import com.diettracker.repository.FoodItemRepository;
import com.diettracker.repository.MealRecordRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MealRecordServiceTest {
    @Test
    void addRecordCapturesFoodSnapshotAndReturnsExistingIdempotentRequest() {
        MealRecordRepository records = mock(MealRecordRepository.class);
        FoodItemRepository foods = mock(FoodItemRepository.class);
        FoodCategoryRepository categories = mock(FoodCategoryRepository.class);
        FoodItem food = food(8L, "鸡胸肉", "system-owner");
        food.setUserId(null);
        when(foods.findById(8L)).thenReturn(Optional.of(food));
        when(records.findByUserIdAndClientRequestId("user-a", "request-1"))
                .thenReturn(Optional.empty())
                .thenAnswer(invocation -> Optional.of(invocationRecord));
        when(records.saveAndFlush(any(MealRecord.class))).thenAnswer(invocation -> {
            invocationRecord = invocation.getArgument(0);
            invocationRecord.setId(21L);
            return invocationRecord;
        });
        MealRecordService service = new MealRecordService(records, foods, categories);
        CreateMealRecordRequest request = new CreateMealRecordRequest(
                LocalDate.of(2026, 7, 15), MealRecord.MealType.lunch, 8L,
                new BigDecimal("150"), "g", null, "少油");

        MealRecord created = service.addRecord(request, "user-a", " request-1 ");
        MealRecord retried = service.addRecord(request, "user-a", "request-1");

        assertThat(retried).isSameAs(created);
        assertThat(created.getFoodNameSnapshot()).isEqualTo("鸡胸肉");
        assertThat(created.getBaseAmountSnapshot()).isEqualByComparingTo("100");
        assertThat(created.getCaloriesSnapshot()).isEqualByComparingTo("165");
        verify(records, times(1)).saveAndFlush(any(MealRecord.class));
    }

    private MealRecord invocationRecord;

    @Test
    void dailyStatsOnlyUsesNutritionSnapshot() {
        MealRecordRepository records = mock(MealRecordRepository.class);
        FoodItemRepository foods = mock(FoodItemRepository.class);
        FoodCategoryRepository categories = mock(FoodCategoryRepository.class);
        MealRecord record = new MealRecord();
        record.setQuantity(new BigDecimal("150"));
        record.setBaseAmountSnapshot(new BigDecimal("100"));
        record.setCaloriesSnapshot(new BigDecimal("165"));
        record.setProteinSnapshot(new BigDecimal("31"));
        record.setFatSnapshot(new BigDecimal("3.6"));
        record.setCarbsSnapshot(BigDecimal.ZERO);
        FoodItem changedFood = food(8L, "鸡胸肉（已修改）", null);
        changedFood.setCalories(new BigDecimal("999"));
        record.setFoodItem(changedFood);
        LocalDate date = LocalDate.of(2026, 7, 15);
        when(records.findByUserIdAndMealDateOrderByRecordTimeAsc("user-a", date)).thenReturn(List.of(record));
        MealRecordService service = new MealRecordService(records, foods, categories);

        Map<String, Object> stats = service.getDailyStats(date, "user-a");

        assertThat(stats.get("totalCalories")).isEqualTo(247.5);
        assertThat(stats.get("totalProtein")).isEqualTo(46.5);
    }

    @Test
    void addRecordCannotReferenceAnotherUsersFood() {
        MealRecordRepository records = mock(MealRecordRepository.class);
        FoodItemRepository foods = mock(FoodItemRepository.class);
        FoodCategoryRepository categories = mock(FoodCategoryRepository.class);
        when(foods.findById(8L)).thenReturn(Optional.of(food(8L, "私有食品", "owner")));
        MealRecordService service = new MealRecordService(records, foods, categories);
        CreateMealRecordRequest request = new CreateMealRecordRequest(
                LocalDate.now(), MealRecord.MealType.lunch, 8L, BigDecimal.ONE, "g", null, null);

        assertThatThrownBy(() -> service.addRecord(request, "another-user", "request-2"))
                .isInstanceOf(ApiException.class)
                .hasMessage("食品不存在");
        verify(records, never()).saveAndFlush(any());
    }

    @Test
    void deleteRecordRejectsAnotherUsersRecord() {
        MealRecordRepository records = mock(MealRecordRepository.class);
        FoodItemRepository foods = mock(FoodItemRepository.class);
        FoodCategoryRepository categories = mock(FoodCategoryRepository.class);
        MealRecord record = new MealRecord();
        record.setUserId("owner");
        when(records.findById(7L)).thenReturn(Optional.of(record));
        MealRecordService service = new MealRecordService(records, foods, categories);

        assertThatThrownBy(() -> service.deleteRecord(7L, "another-user"))
                .isInstanceOf(ApiException.class)
                .hasMessage("无权操作该记录");
        verifyNoInteractions(foods, categories);
    }

    private FoodItem food(Long id, String name, String userId) {
        FoodItem food = new FoodItem();
        food.setId(id);
        food.setName(name);
        food.setUserId(userId);
        food.setBaseAmount(new BigDecimal("100"));
        food.setBaseUnit("g");
        food.setUnit("g");
        food.setCalories(new BigDecimal("165"));
        food.setProtein(new BigDecimal("31"));
        food.setFat(new BigDecimal("3.6"));
        food.setCarbs(BigDecimal.ZERO);
        return food;
    }
}
