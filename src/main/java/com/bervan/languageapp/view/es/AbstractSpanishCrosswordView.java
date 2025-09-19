package com.bervan.languageapp.view.es;

import com.bervan.languageapp.service.CrosswordService;
import com.bervan.languageapp.view.AbstractCrosswordView;

public abstract class AbstractSpanishCrosswordView extends AbstractCrosswordView {
    public static final String ROUTE_NAME = "learning-language-app/es/crossword";

    public AbstractSpanishCrosswordView(CrosswordService crosswordService) {
        super(crosswordService, "ES");
    }
}