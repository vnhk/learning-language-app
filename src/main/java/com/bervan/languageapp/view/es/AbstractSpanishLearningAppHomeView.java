package com.bervan.languageapp.view.es;

import com.bervan.languageapp.view.AbstractLearningAppHomeView;

public abstract class AbstractSpanishLearningAppHomeView extends AbstractLearningAppHomeView {
    public static final String ROUTE_NAME = "learning-language-app/es/home";

    public AbstractSpanishLearningAppHomeView() {
        super(new LearningSpanishLayout(ROUTE_NAME));
    }
}
