package com.bervan.languageapp.view.es;

import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.view.AbstractLearningView;


public abstract class AbstractSpanishLearningView extends AbstractLearningView {
    public static final String ROUTE_NAME = "learning-language-app/es/learning-view";

    public AbstractSpanishLearningView(TranslationRecordService translationRecordService) {
        super(translationRecordService, new LearningSpanishLayout(ROUTE_NAME), "ES");
    }
}