package com.bervan.languageapp.view.es;

import com.bervan.common.search.SearchService;
import com.bervan.languageapp.service.ExampleOfUsageService;
import com.bervan.languageapp.service.TextToSpeechService;
import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.service.TranslatorService;
import com.bervan.languageapp.view.AbstractFastImportView;

public class AbstractSpanishFastImportView extends AbstractFastImportView {
    public static final String ROUTE_NAME = "learning-language-app/es/fast-import";

    public AbstractSpanishFastImportView(TranslationRecordService translationRecordService, TextToSpeechService textToSpeechService, SearchService searchService, ExampleOfUsageService exampleOfUsageService, TranslatorService translatorService) {
        super(translationRecordService, new LearningSpanishLayout(ROUTE_NAME), "ES", textToSpeechService, searchService, translatorService, exampleOfUsageService);
    }
}
