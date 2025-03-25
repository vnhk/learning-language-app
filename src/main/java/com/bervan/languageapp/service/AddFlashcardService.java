package com.bervan.languageapp.service;

import com.bervan.common.model.PersistableTableData;
import com.bervan.languageapp.TranslationRecord;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddFlashcardService {
    private final TranslationRecordService translationRecordService;
    private final TranslatorService translatorService;
    private final ExampleOfUsageService exampleOfUsageService;
    private final TextToSpeechService textToSpeechService;

    public AddFlashcardService(TranslationRecordService translationRecordService, TranslatorService translatorService, ExampleOfUsageService exampleOfUsageService, TextToSpeechService textToSpeechService) {
        this.translationRecordService = translationRecordService;
        this.translatorService = translatorService;
        this.exampleOfUsageService = exampleOfUsageService;
        this.textToSpeechService = textToSpeechService;
    }

    public void addAsFlashcardAsync(PersistableTableData item) {
        SecurityContext context = SecurityContextHolder.getContext();
        new Thread(() -> {
            SecurityContextHolder.setContext(context);
            String name = item.getTableFilterableColumnValue();
            String translated = translatorService.translate(name);
            List<String> exampleOfUsage = exampleOfUsageService.createExampleOfUsage(name);
            String examples = exampleOfUsage.toString().replace("[", "").replace("]", "");

            TranslationRecord record = new TranslationRecord();
            record.setSourceText(name);
            record.setTextTranslation(translated);
            record.setFactor(1);
            if (!examples.isBlank()) {
                if (examples.length() > 500) {
                    StringBuilder builder = new StringBuilder();
                    for (String s : exampleOfUsage) {
                        if (builder.length() + s.length() + 1 > 500) {
                            break;
                        }
                        builder.append(s);
                        builder.append(",");
                    }
                    examples = builder.substring(0, builder.length() - 2);
                }

                record.setInSentence(examples);
                String examplesTranslated = translatorService.translate(examples);
                record.setInSentenceTranslation(examplesTranslated);

                if (record.getSourceText() != null && !record.getSourceText().isBlank()) {
                    record.setTextSound(textToSpeechService.getTextSpeech(record.getSourceText()));
                }

                if (record.getInSentence() != null && !record.getInSentence().isBlank()) {
                    record.setInSentenceSound(textToSpeechService.getTextSpeech(record.getInSentence()));
                }

                translationRecordService.setNewAndReplaceImages(record);
            }
            translationRecordService.save(record);
        }).start();
    }
}
