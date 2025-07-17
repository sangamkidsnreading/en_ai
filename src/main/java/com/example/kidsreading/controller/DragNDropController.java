package com.example.kidsreading.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dragndrop")
@RequiredArgsConstructor
public class DragNDropController {

    private final com.example.kidsreading.service.DragNDropService dragNDropService;

    @PostMapping("/words-order")
    public ResponseEntity<?> updateWordsOrder(@RequestBody List<OrderDto> orderList) {
        try {
            dragNDropService.updateWordsOrder(orderList);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/sentences-order")
    public ResponseEntity<?> updateSentencesOrder(@RequestBody List<OrderDto> orderList) {
        try {
            dragNDropService.updateSentencesOrder(orderList);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @Data
    public static class OrderDto {
        private Long id;
        private Integer order;
    }
}
