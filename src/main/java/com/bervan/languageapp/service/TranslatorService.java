package com.bervan.languageapp.service;

import com.bervan.core.model.BervanLogger;
import com.google.cloud.translate.Translate;
import com.google.cloud.translate.TranslateOptions;
import com.google.cloud.translate.Translation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TranslatorService {
    @Value("${translation.api.key}")
    private String API_KEY;
    private Translate translate;
    private final BervanLogger logger;
    private String sourceLanguage = "ES";
    private String targetLanguage = "PL";

    public TranslatorService(BervanLogger logger) {
        this.logger = logger;

        translate = TranslateOptions.newBuilder()
                .setApiKey(API_KEY)
                .build().getService();
    }

    public String translate(String value) {
        if (value.length() > 250) {
            throw new RuntimeException("Too long value to be translated!");
        }

        Translation translation = translate.translate(value,
                Translate.TranslateOption.sourceLanguage(sourceLanguage),
                Translate.TranslateOption.targetLanguage(targetLanguage));

        return translation.getTranslatedText();
    }
}
