package com.bervan.languageapp.view.en;

import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.view.AbstractLearningView;


public abstract class AbstractEnglishLearningView extends AbstractLearningView {
    public static final String ROUTE_NAME = "learning-language-app/en/learning-view";

    public AbstractEnglishLearningView(TranslationRecordService translationRecordService) {
        super(translationRecordService, new LearningEnglishLayout(ROUTE_NAME), "EN");
    }
}