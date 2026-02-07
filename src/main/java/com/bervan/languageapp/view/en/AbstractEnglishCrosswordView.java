package com.bervan.languageapp.view.en;

import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.view.AbstractCrosswordView;

public abstract class AbstractEnglishCrosswordView extends AbstractCrosswordView {
    public static final String ROUTE_NAME = "learning-language-app/en/crossword";

    public AbstractEnglishCrosswordView(TranslationRecordService translationRecordService) {
        super(translationRecordService, "EN", new LearningEnglishLayout(ROUTE_NAME));
    }
}
