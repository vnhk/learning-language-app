package com.bervan.languageapp.view;

import com.bervan.common.MenuNavigationComponent;

public final class LearningEnglishLayout extends MenuNavigationComponent {
    public LearningEnglishLayout(String routeName) {
        super(routeName);

        addButtonIfVisible(menuButtonsRow, AbstractLearningAppHomeView.ROUTE_NAME, "Home");
        addButtonIfVisible(menuButtonsRow, AbstractLearningView.ROUTE_NAME, "Flashcards");
        addButtonIfVisible(menuButtonsRow, AbstractQuizView.ROUTE_NAME, "Quiz");

        add(menuButtonsRow);
    }
}
