package com.example.kidsreading.controller;

import com.example.kidsreading.entity.LevelSettings;
import com.example.kidsreading.service.LevelSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/level-settings")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class LevelSettingsController {
    private final LevelSettingsService service;

    @GetMapping
    public List<LevelSettings> getAll() { return service.getAll(); }

    @PostMapping
    public LevelSettings save(@RequestBody LevelSettings s, Principal principal) {
        String updatedBy = principal != null ? principal.getName() : "unknown";
        return service.save(s, updatedBy);
    }

    @PutMapping("/{id}")
    public LevelSettings update(@PathVariable Long id, @RequestBody Map<String, Integer> body, Principal principal) {
        String updatedBy = principal != null ? principal.getName() : "unknown";
        return service.update(
            id,
            body.get("wordsToNextLevel"),
            body.get("sentencesToNextLevel"),
            updatedBy
        );
    }
}
