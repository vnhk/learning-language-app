package com.bervan.languageapp.view.es;

import com.bervan.asynctask.AsyncTaskService;
import com.bervan.common.search.SearchService;
import com.bervan.common.view.EmptyLayout;
import com.bervan.languageapp.service.ExampleOfUsageService;
import com.bervan.languageapp.service.TextToSpeechService;
import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.service.TranslatorService;
import com.bervan.languageapp.view.AbstractFastImportView;

public class AbstractSpanishFastImportView extends AbstractFastImportView {

    public AbstractSpanishFastImportView(TranslationRecordService translationRecordService, TextToSpeechService textToSpeechService, SearchService searchService, ExampleOfUsageService exampleOfUsageService, TranslatorService translatorService, AsyncTaskService asyncTaskService) {
        super(translationRecordService, new EmptyLayout(), "ES", textToSpeechService, searchService, asyncTaskService, translatorService, exampleOfUsageService);
        generateImages.setValue(false);
    }
}
