package com.bervan.languageapp.service;

import com.bervan.languageapp.Crossword;
import com.bervan.languageapp.TranslationRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class CrosswordService {
    private static final int MAX_ATTEMPTS = 100;

    @Autowired
    private TranslationRecordService wordRepository;

    public Crossword generateCrossword(int width, int height) {
        List<TranslationRecord> words = new ArrayList<>(wordRepository.load(Pageable.ofSize(10)));
        Collections.shuffle(words);

        Crossword crossword = new Crossword(width, height);
        int attempts = 0;

        for (TranslationRecord word : words) {
            String text = word.getSourceText().toUpperCase().replaceAll("[^A-Z]", "");
            if (text.isEmpty()) continue;

            boolean added = false;
            int placementAttempts = 0;

            while (!added && placementAttempts < MAX_ATTEMPTS) {
                placementAttempts++;

                boolean across = Math.random() > 0.5;
                int x = (int) (Math.random() * (across ? width - text.length() : width));
                int y = (int) (Math.random() * (across ? height : height - text.length()));

                added = crossword.tryAddWord(word, x, y, across);
            }

            if (!added) {
                attempts++;
                if (attempts >= MAX_ATTEMPTS) break;
            }
        }

        return crossword;
    }
}