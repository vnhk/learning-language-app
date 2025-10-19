package com.bervan.languageapp.view.es;

import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.view.AbstractLearningAppHomeView;

public abstract class AbstractSpanishLearningAppHomeView extends AbstractLearningAppHomeView {
    public static final String ROUTE_NAME = "learning-language-app/es/home";

    public AbstractSpanishLearningAppHomeView(TranslationRecordService translationRecordsService) {
        super(translationRecordsService,
                AbstractSpanishLearningTableView.ROUTE_NAME,
                AbstractSpanishLearningView.ROUTE_NAME,
                AbstractSpanishQuizView.ROUTE_NAME,
                AbstractSpanishFastImportView.ROUTE_NAME,
                AbstractSpanishImportExportView.ROUTE_NAME, "ES");
    }
}
