package com.example.kidsreading.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkUploadResultDto {
    private Integer successCount;
    private Integer errorCount;
    private List<String> errors;
    private String message;
}