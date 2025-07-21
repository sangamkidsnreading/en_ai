package com.example.kidsreading.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DayLevelProgressDto {
    private Integer day;
    private Integer level;
    private Long wordsLearned;
    private Long sentencesLearned;
    
    public Long getTotalLearned() {
        return (wordsLearned != null ? wordsLearned : 0L) + 
               (sentencesLearned != null ? sentencesLearned : 0L);
    }
} 