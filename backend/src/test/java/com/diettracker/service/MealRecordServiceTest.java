package com.diettracker.service;

import com.diettracker.api.ApiException;
import com.diettracker.entity.MealRecord;
import com.diettracker.repository.FoodCategoryRepository;
import com.diettracker.repository.FoodItemRepository;
import com.diettracker.repository.MealRecordRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MealRecordServiceTest {
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
}
