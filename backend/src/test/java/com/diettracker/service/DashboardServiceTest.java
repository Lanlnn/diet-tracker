package com.diettracker.service;

import com.diettracker.dto.DashboardTodayResponse;
import com.diettracker.entity.MealRecord;
import com.diettracker.entity.User;
import com.diettracker.repository.MealRecordRepository;
import com.diettracker.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DashboardServiceTest {
    @Test
    void readsOneRecordSnapshotAndDoesNotReturnExerciseCaloriesToRemaining() {
        MealRecordRepository records = mock(MealRecordRepository.class);
        UserRepository users = mock(UserRepository.class);
        LocalDate date = LocalDate.of(2026, 7, 15);
        User user = new User();
        user.setOpenid("user-a");
        user.setDailyCalorieGoal(1000);
        MealRecord meal = meal(new BigDecimal("700"));
        when(users.findById("user-a")).thenReturn(Optional.of(user));
        when(records.findByUserIdAndMealDateOrderByRecordTimeAsc("user-a", date)).thenReturn(List.of(meal));

        DashboardTodayResponse response = new DashboardService(records, users).getToday(date, "user-a");

        assertThat(response.intakeCalories()).isEqualByComparingTo("1155");
        assertThat(response.remainingCalories()).isEqualByComparingTo("0");
        assertThat(response.exceededCalories()).isEqualByComparingTo("155");
        assertThat(response.exerciseCalories()).isEqualByComparingTo("0");
        assertThat(response.netCalories()).isEqualByComparingTo(response.intakeCalories());
        assertThat(response.meals()).hasSize(4);
        verify(users).findById("user-a");
        verify(records).findByUserIdAndMealDateOrderByRecordTimeAsc("user-a", date);
    }

    private MealRecord meal(BigDecimal quantity) {
        MealRecord meal = new MealRecord();
        meal.setMealType(MealRecord.MealType.lunch);
        meal.setQuantity(quantity);
        meal.setFoodNameSnapshot("鸡胸肉");
        meal.setBaseAmountSnapshot(new BigDecimal("100"));
        meal.setBaseUnitSnapshot("g");
        meal.setCaloriesSnapshot(new BigDecimal("165"));
        meal.setProteinSnapshot(new BigDecimal("31"));
        meal.setFatSnapshot(new BigDecimal("3.6"));
        meal.setCarbsSnapshot(BigDecimal.ZERO);
        return meal;
    }
}
