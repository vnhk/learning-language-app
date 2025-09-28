package com.bervan.languageapp.view.en;

import com.bervan.languageapp.view.AbstractLearningAppHomeView;
import com.bervan.languageapp.view.es.LearningSpanishLayout;

public abstract class AbstractEnglishLearningAppHomeView extends AbstractLearningAppHomeView {
    public static final String ROUTE_NAME = "learning-language-app/en/home";

    public AbstractEnglishLearningAppHomeView() {
        super(new LearningEnglishLayout(ROUTE_NAME));
    }
}
