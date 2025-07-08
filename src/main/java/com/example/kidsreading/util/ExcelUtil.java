// ========== ExcelUtil.java ==========
package com.example.kidsreading.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ExcelUtil {

    public static byte[] createWordTemplate() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("단어 템플릿");

        // 헤더 생성
        Row headerRow = sheet.createRow(0);
        String[] headers = {"단어", "의미", "발음", "레벨", "날짜"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);

            // 헤더 스타일
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            cell.setCellStyle(headerStyle);
        }

        // 예시 데이터 한 줄 추가
        Row exampleRow = sheet.createRow(1);
        exampleRow.createCell(0).setCellValue("hello");
        exampleRow.createCell(1).setCellValue("안녕");
        exampleRow.createCell(2).setCellValue("/həˈloʊ/");
        exampleRow.createCell(3).setCellValue(1);
        exampleRow.createCell(4).setCellValue(1);

        // 열 너비 자동 조정
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    public static byte[] createSentenceTemplate() throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("문장 템플릿");

        // 헤더 생성
        Row headerRow = sheet.createRow(0);
        String[] headers = {"문장", "번역", "레벨", "날짜"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);

            // 헤더 스타일
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            cell.setCellStyle(headerStyle);
        }

        // 예시 데이터 한 줄 추가
        Row exampleRow = sheet.createRow(1);
        exampleRow.createCell(0).setCellValue("Hello, how are you?");
        exampleRow.createCell(1).setCellValue("안녕하세요, 어떻게 지내세요?");
        exampleRow.createCell(2).setCellValue(1);
        exampleRow.createCell(3).setCellValue(1);

        // 열 너비 자동 조정
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    public static byte[] createUserProgressReport(List<Map<String, Object>> progressData) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("사용자 진도 리포트");

        // 헤더 생성
        Row headerRow = sheet.createRow(0);
        String[] headers = {"사용자 ID", "사용자명", "이름", "학습한 단어", "학습한 문장", "마지막 로그인"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            cell.setCellStyle(headerStyle);
        }

        // 데이터 행 생성
        int rowNum = 1;
        for (Map<String, Object> userData : progressData) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(userData.get("userId").toString());
            row.createCell(1).setCellValue(userData.get("username").toString());
            row.createCell(2).setCellValue(userData.get("name").toString());
            row.createCell(3).setCellValue(userData.get("wordsLearned").toString());
            row.createCell(4).setCellValue(userData.get("sentencesLearned").toString());
            row.createCell(5).setCellValue(userData.get("lastLogin") != null ?
                    userData.get("lastLogin").toString() : "없음");
        }

        // 열 너비 자동 조정
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }
}