package com.bervan.languageapp.service;

import com.bervan.common.model.BervanLogger;
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

    public TranslatorService(BervanLogger logger) {
        this.logger = logger;

        translate = TranslateOptions.newBuilder()
                .setApiKey(API_KEY)
                .build().getService();

        logger.logDebug("TRANSLATE_API_KEY:" + API_KEY);
    }

    public String translate(String value, String sourceLanguage, String targetLanguage) {
        logger.logDebug("TRANSLATE_API_KEY:" + API_KEY);

        Translation translation = translate.translate(value,
                Translate.TranslateOption.sourceLanguage(sourceLanguage),
                Translate.TranslateOption.targetLanguage(targetLanguage));

        return translation.getTranslatedText();
    }
}
