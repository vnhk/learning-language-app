package com.bervan.languageapp.view.en;

import com.bervan.common.search.SearchService;
import com.bervan.languageapp.service.ExampleOfUsageService;
import com.bervan.languageapp.service.TextToSpeechService;
import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.service.TranslatorService;
import com.bervan.languageapp.view.AbstractFastImportView;

public class AbstractEnglishFastImportView extends AbstractFastImportView {
    public static final String ROUTE_NAME = "learning-language-app/en/fast-import";

    public AbstractEnglishFastImportView(TranslationRecordService translationRecordService, TextToSpeechService textToSpeechService, SearchService searchService, ExampleOfUsageService exampleOfUsageService, TranslatorService translatorService) {
        super(translationRecordService, new LearningEnglishLayout(ROUTE_NAME), "EN", textToSpeechService, searchService, translatorService, exampleOfUsageService);
    }
}
