package com.bervan.languageapp.view;

import com.bervan.common.MenuNavigationComponent;
import com.vaadin.flow.component.html.Hr;

public final class LearningEnglishLayout extends MenuNavigationComponent {
    public LearningEnglishLayout(String routeName) {
        super(routeName);

        addButton(menuButtonsRow, AbstractLearningAppHomeView.ROUTE_NAME, "Home");
        addButton(menuButtonsRow, AbstractLearningView.ROUTE_NAME, "Flashcards");
        addButton(menuButtonsRow, AbstractQuizView.ROUTE_NAME, "Quiz");

        add(menuButtonsRow);
    }
}
