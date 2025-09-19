package com.bervan.languageapp.view.en;

import com.bervan.languageapp.service.ExampleOfUsageService;
import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.view.AbstractQuizView;

public abstract class AbstractEnglishQuizView extends AbstractQuizView {
    public static final String ROUTE_NAME = "learning-language-app/en/quiz-view";

    public AbstractEnglishQuizView(TranslationRecordService translationRecordService, ExampleOfUsageService exampleOfUsageService) {
        super(translationRecordService, exampleOfUsageService, "EN", new LearningEnglishLayout(ROUTE_NAME));
    }

}
