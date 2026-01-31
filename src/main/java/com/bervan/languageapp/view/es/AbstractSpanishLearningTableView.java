package com.bervan.languageapp.view.es;

import com.bervan.asynctask.AsyncTaskService;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.search.SearchService;
import com.bervan.languageapp.service.ExampleOfUsageService;
import com.bervan.languageapp.service.TextToSpeechService;
import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.service.TranslatorService;
import com.bervan.languageapp.view.AbstractLearningTableView;

public abstract class AbstractSpanishLearningTableView extends AbstractLearningTableView {
    public static final String ROUTE_NAME = "learning-language-app/es/list";

    public AbstractSpanishLearningTableView(TranslationRecordService translatorRecordService,
                                            ExampleOfUsageService exampleOfUsageService,
                                            TextToSpeechService textToSpeechService,
                                            TranslatorService translationService,
                                            SearchService searchService,
                                            AsyncTaskService asyncTaskService,
                                            BervanViewConfig bervanViewConfig) {
        super(translatorRecordService, exampleOfUsageService, textToSpeechService, translationService,
                searchService, asyncTaskService, "ES", new LearningSpanishLayout(ROUTE_NAME), bervanViewConfig);
    }
}
