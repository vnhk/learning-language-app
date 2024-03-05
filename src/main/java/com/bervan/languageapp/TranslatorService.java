package com.bervan.languageapp;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TranslatorService {
    @Value("${translation.api.key}")
    private String API_KEY;
    private final Translate translate;
    private final TranslationRecordService translationRecordService;

    public TranslatorService(TranslationRecordService translationRecordService) {
        this.translationRecordService = translationRecordService;
        translate = TranslateOptions.newBuilder()
                .setApiKey(API_KEY)
                .build().getService();
    }

    public String translate(String value, String sourceLanguage, String targetLanguage) {
        Translation translation = translate.translate(value,
                Translate.TranslateOption.sourceLanguage(sourceLanguage),
                Translate.TranslateOption.targetLanguage(targetLanguage));

        return translation.getTranslatedText();
    }

    public List<TranslationRecord> getAll() {
        return translationRecordService.getAll();
    }

    public TranslationRecord add(TranslationRecord newTranslationRecord) {
        return translationRecordService.add(newTranslationRecord);
    }

    public void loadAll() {
        translationRecordService.loadAll();
    }

    public void delete(TranslationRecord record) {
        translationRecordService.delete(record);
    }

    public void delete(String uuid) {
        TranslationRecord translationRecord = getAll().stream().filter(e -> e.getUuid().equals(UUID.fromString(uuid))).findFirst().get();
        translationRecordService.delete(translationRecord);
    }
}
