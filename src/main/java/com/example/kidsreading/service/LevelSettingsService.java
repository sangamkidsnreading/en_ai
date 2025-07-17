package com.example.kidsreading.service;

import com.example.kidsreading.entity.LevelSettings;
import com.example.kidsreading.repository.LevelSettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class LevelSettingsService {
    @Autowired
    private LevelSettingsRepository repo;

    public void initDefaultLevels() {
        for (int i = 1; i <= 10; i++) {
            if (repo.findByLevel(i).isEmpty()) {
                LevelSettings s = new LevelSettings();
                s.setLevel(i);
                s.setWordsToNextLevel(100);
                s.setSentencesToNextLevel(50);
                s.setUpdatedBy("system");
                s.setUpdatedAt(LocalDateTime.now());
                repo.save(s);
            }
        }
    }

    public List<LevelSettings> getAll() { return repo.findAll(); }
    public LevelSettings save(LevelSettings s, String updatedBy) {
        s.setUpdatedBy(updatedBy);
        s.setUpdatedAt(LocalDateTime.now());
        return repo.save(s);
    }
    public LevelSettings update(Long id, int words, int sentences, String updatedBy) {
        LevelSettings s = repo.findById(id).orElseThrow();
        s.setWordsToNextLevel(words);
        s.setSentencesToNextLevel(sentences);
        s.setUpdatedBy(updatedBy);
        s.setUpdatedAt(LocalDateTime.now());
        return repo.save(s);
    }
}
