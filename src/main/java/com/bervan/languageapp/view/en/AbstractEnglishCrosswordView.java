package com.bervan.languageapp.view.en;

import com.bervan.languageapp.service.CrosswordService;
import com.bervan.languageapp.view.AbstractCrosswordView;

public abstract class AbstractEnglishCrosswordView extends AbstractCrosswordView {
    public static final String ROUTE_NAME = "learning-language-app/en/crossword";

    public AbstractEnglishCrosswordView(CrosswordService crosswordService) {
        super(crosswordService, "EN");
    }
}