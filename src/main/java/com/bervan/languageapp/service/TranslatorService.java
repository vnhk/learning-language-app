package com.bervan.languageapp.service;

import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TranslatorService {
    @Value("${translation.api.key}")
    private String API_KEY;
    private final Translate translate;

    public TranslatorService() {
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
}
