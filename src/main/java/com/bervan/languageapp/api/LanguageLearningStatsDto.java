package com.bervan.languageapp.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LanguageLearningStatsDto {
    private long total;
    private long mastered;
    private long dueNow;
}
