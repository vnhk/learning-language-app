package com.bervan.languageapp.service;

import com.bervan.languageapp.TranslationRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class CrosswordService {

    @Autowired
    private TranslationRecordService wordRepository;

    public List<TranslationRecord> getCrosswordWords(int width, int height) {
        List<TranslationRecord> words = new ArrayList<>(wordRepository.load(Pageable.ofSize(1500)));
        Collections.shuffle(words);
        List<TranslationRecord> result = new ArrayList<>();
        for (TranslationRecord word : words) {
            word.setSourceText(word.getSourceText().replaceAll("\\?", "")
                    .replaceAll("\\.", "")
                    .replaceAll(",", "")
                    .replaceAll("\\(", "")
                    .replaceAll("\\)", "")
                    .replaceAll("!", "")
                    .toUpperCase()
            );
            if (word.getSourceText().length() < width && word.getSourceText().length() < height) {
                result.add(word);
            }
        }

        //todo filter words

        return result;
    }
}