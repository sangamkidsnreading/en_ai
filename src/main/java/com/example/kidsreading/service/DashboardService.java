package com.example.kidsreading.service;

import com.example.kidsreading.dto.DashboardStatsDto;
import com.example.kidsreading.dto.DashboardCalendarDayDto;
import com.example.kidsreading.repository.UserWordProgressRepository;
import com.example.kidsreading.repository.UserSentenceProgressRepository;
import com.example.kidsreading.repository.UserCoinRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class DashboardService {
    @Autowired
    private UserWordProgressRepository wordRepo;
    @Autowired
    private UserSentenceProgressRepository sentenceRepo;
    @Autowired
    private UserCoinRepository coinRepo;

    public DashboardStatsDto getDashboardStats(String userId) {
        DashboardStatsDto dto = new DashboardStatsDto();
        dto.setWordCount(wordRepo.countTotalCompletedWords(Long.valueOf(userId)));
        dto.setSentenceCount(sentenceRepo.countTotalCompletedSentences(Long.valueOf(userId)));
        Integer coins = coinRepo.getTotalCoinsByUserId(userId);
        dto.setCoinCount(coins != null ? coins : 0);
        dto.setStreak(1); // TODO: streak 계산 로직 추가
        return dto;
    }

    public List<DashboardCalendarDayDto> getMonthlyCalendar(String userId, int year, int month) {
        List<DashboardCalendarDayDto> result = new ArrayList<>();
        YearMonth ym = YearMonth.of(year, month);
        for (int day = 1; day <= ym.lengthOfMonth(); day++) {
            LocalDate date = ym.atDay(day);
            DashboardCalendarDayDto dto = new DashboardCalendarDayDto();
            dto.setDate(date);
            // 예시: 코인 데이터로 학습 여부 판단 (실제 로직은 필요시 수정)
            Integer coins = coinRepo.getTotalCoinsEarnedByDate(date);
            dto.setCoinCount(coins != null ? coins : 0);
            dto.setCompleted(dto.getCoinCount() > 0);
            // TODO: wordCount, sentenceCount도 필요시 추가 쿼리
            result.add(dto);
        }
        return result;
    }
} 