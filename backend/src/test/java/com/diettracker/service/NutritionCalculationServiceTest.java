package com.diettracker.service;

import com.diettracker.api.FieldValidationException;
import com.diettracker.dto.NutritionCalculationResponse;
import com.diettracker.entity.FoodItem;
import com.diettracker.repository.FoodItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NutritionCalculationServiceTest {
    private final FoodItemRepository foods = mock(FoodItemRepository.class);
    private final NutritionCalculationService service = new NutritionCalculationService(foods);
    private FoodItem chicken;

    @BeforeEach
    void setUp() {
        chicken = new FoodItem();
        chicken.setId(1L);
        chicken.setName("鸡胸肉");
        chicken.setBaseAmount(new BigDecimal("100"));
        chicken.setBaseUnit("g");
        chicken.setCalories(new BigDecimal("165"));
        chicken.setProtein(new BigDecimal("31"));
        chicken.setFat(new BigDecimal("3.6"));
        chicken.setCarbs(BigDecimal.ZERO);
        when(foods.findById(1L)).thenReturn(Optional.of(chicken));
    }

    @Test
    void calculatesAndRoundsNutritionUsingBigDecimal() {
        NutritionCalculationResponse result = service.calculate(1L, new BigDecimal("150"), "user-1");

        assertThat(result.calories()).isEqualByComparingTo("248");
        assertThat(result.protein()).isEqualByComparingTo("46.5");
        assertThat(result.fat()).isEqualByComparingTo("5.4");
        assertThat(result.carbs()).isEqualByComparingTo("0.0");
    }

    @Test
    void supportsOneDecimalAtBothBoundaries() {
        assertThat(service.calculate(1L, new BigDecimal("1.0"), "user-1").amount())
                .isEqualByComparingTo("1");
        assertThat(service.calculate(1L, new BigDecimal("10000.0"), "user-1").amount())
                .isEqualByComparingTo("10000");
    }

    @Test
    void rejectsEmptyZeroNegativeOversizedAndOverPreciseAmounts() {
        assertAmountError(null, "请输入食用重量");
        assertAmountError(BigDecimal.ZERO, "食用重量不能小于 1g");
        assertAmountError(new BigDecimal("-1"), "食用重量不能小于 1g");
        assertAmountError(new BigDecimal("10000.1"), "食用重量不能超过 10000g");
        assertAmountError(new BigDecimal("1.11"), "食用重量最多保留 1 位小数");
    }

    @Test
    void rejectsMissingNegativeAndNonGramFoodBasisWithFieldErrors() {
        chicken.setBaseUnit("份");
        chicken.setProtein(new BigDecimal("-1"));
        chicken.setFat(null);

        assertThatThrownBy(() -> service.calculate(1L, new BigDecimal("150"), "user-1"))
                .isInstanceOfSatisfying(FieldValidationException.class, error -> {
                    assertThat(error.getFieldErrors()).containsKeys("baseUnit", "protein", "fat");
                });
    }

    @Test
    void doesNotExposeAnotherUsersCustomFood() {
        chicken.setUserId("owner");

        assertThatThrownBy(() -> service.calculate(1L, new BigDecimal("150"), "another-user"))
                .hasMessage("食品不存在");
    }

    private void assertAmountError(BigDecimal amount, String message) {
        assertThatThrownBy(() -> service.calculate(1L, amount, "user-1"))
                .isInstanceOfSatisfying(FieldValidationException.class,
                        error -> assertThat(error.getFieldErrors()).containsEntry("amount", message));
    }
}
