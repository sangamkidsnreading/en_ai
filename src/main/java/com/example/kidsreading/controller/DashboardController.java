package com.example.kidsreading.controller;

import com.example.kidsreading.dto.DashboardStatsDto;
import com.example.kidsreading.service.DashboardService;
import com.example.kidsreading.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/dashboard/api")
public class DashboardController {
    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public DashboardStatsDto getStats(@AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user) {
        return dashboardService.getDashboardStats(user.getId().toString());
    }

    @GetMapping("/calendar")
    public List<com.example.kidsreading.dto.DashboardCalendarDayDto> getCalendar(
        @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal user,
        @RequestParam int year,
        @RequestParam int month
    ) {
        return dashboardService.getMonthlyCalendar(user.getId().toString(), year, month);
    }
}
