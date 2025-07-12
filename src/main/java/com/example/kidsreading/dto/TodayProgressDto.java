package com.example.kidsreading.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodayProgressDto {
    private List<WordInfo> words;
    private List<SentenceInfo> sentences;
    private int coin;

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class WordInfo {
        private Long id;
        private String english;
        private String korean;
        private String learnedAt;
        private boolean isFavorite;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SentenceInfo {
        private Long id;
        private String english;
        private String korean;
        private String learnedAt;
    }
}
