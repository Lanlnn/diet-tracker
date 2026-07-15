package com.diettracker.admin;

import com.diettracker.admin.dto.AdminMealDiagnosticResponse;
import com.diettracker.admin.dto.AdminSupportUserResponse;
import com.diettracker.dto.DashboardTodayResponse;
import com.diettracker.entity.User;
import com.diettracker.repository.MealRecordRepository;
import com.diettracker.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class AdminSupportDiagnosticService {
    private final AdminUserRefService userRefs;
    private final DashboardService dashboardService;
    private final MealRecordRepository mealRecords;

    public AdminSupportDiagnosticService(AdminUserRefService userRefs, DashboardService dashboardService,
                                         MealRecordRepository mealRecords) {
        this.userRefs = userRefs;
        this.dashboardService = dashboardService;
        this.mealRecords = mealRecords;
    }

    @Transactional(readOnly = true)
    public AdminSupportUserResponse user(String supportRef) {
        return AdminSupportUserResponse.from(userRefs.resolve(supportRef));
    }

    @Transactional(readOnly = true)
    public DashboardTodayResponse today(String supportRef, LocalDate date) {
        User user = userRefs.resolve(supportRef);
        return dashboardService.getToday(date, user.getOpenid());
    }

    @Transactional(readOnly = true)
    public List<AdminMealDiagnosticResponse> meals(String supportRef, LocalDate date) {
        User user = userRefs.resolve(supportRef);
        return mealRecords.findSupportDiagnosticsByDate(user.getOpenid(), date);
    }
}
