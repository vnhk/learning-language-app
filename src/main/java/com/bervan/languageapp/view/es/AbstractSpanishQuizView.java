package com.bervan.languageapp.view.es;

import com.bervan.languageapp.service.ExampleOfUsageService;
import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.view.AbstractQuizView;

public abstract class AbstractSpanishQuizView extends AbstractQuizView {
    public static final String ROUTE_NAME = "learning-language-app/es/quiz-view";

    public AbstractSpanishQuizView(TranslationRecordService translationRecordService, ExampleOfUsageService exampleOfUsageService) {
        super(translationRecordService, exampleOfUsageService, "ES", new LearningSpanishLayout(ROUTE_NAME));
    }

}
