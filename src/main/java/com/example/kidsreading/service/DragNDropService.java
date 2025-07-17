package com.example.kidsreading.service;

import com.example.kidsreading.controller.DragNDropController.OrderDto;
import com.example.kidsreading.repository.WordRepository;
import com.example.kidsreading.repository.SentenceRepository;
import com.example.kidsreading.entity.Word;
import com.example.kidsreading.entity.Sentence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DragNDropService {
    private final WordRepository wordRepository;
    private final SentenceRepository sentenceRepository;

    @Transactional
    public void updateWordsOrder(List<OrderDto> orderList) {
        for (OrderDto dto : orderList) {
            Word word = wordRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("단어를 찾을 수 없습니다: " + dto.getId()));
            word.setDisplayOrder(dto.getOrder());
            wordRepository.save(word);
        }
    }

    @Transactional
    public void updateSentencesOrder(List<OrderDto> orderList) {
        for (OrderDto dto : orderList) {
            Sentence sentence = sentenceRepository.findById(dto.getId())
                .orElseThrow(() -> new IllegalArgumentException("문장을 찾을 수 없습니다: " + dto.getId()));
            sentence.setDisplayOrder(dto.getOrder());
            sentenceRepository.save(sentence);
        }
    }
} 