package com.bervan.languageapp.view.es;

import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.view.AbstractCrosswordView;

public abstract class AbstractSpanishCrosswordView extends AbstractCrosswordView {
    public static final String ROUTE_NAME = "learning-language-app/es/crossword";

    public AbstractSpanishCrosswordView(TranslationRecordService translationRecordService) {
        super(translationRecordService, "ES", new LearningSpanishLayout(ROUTE_NAME));
    }
}
