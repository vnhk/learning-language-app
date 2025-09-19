package com.bervan.languageapp.view.en;

import com.bervan.common.MenuNavigationComponent;
import com.bervan.languageapp.view.*;
import com.vaadin.flow.component.icon.VaadinIcon;

public final class LearningEnglishLayout extends MenuNavigationComponent {
    public LearningEnglishLayout(String routeName) {
        super(routeName);

        addButtonIfVisible(menuButtonsRow, AbstractEnglishLearningAppHomeView.ROUTE_NAME, "Home", VaadinIcon.HOME.create());
        addButtonIfVisible(menuButtonsRow, AbstractEnglishLearningView.ROUTE_NAME, "Flashcards", VaadinIcon.BOOKMARK.create());
        addButtonIfVisible(menuButtonsRow, AbstractEnglishQuizView.ROUTE_NAME, "Quiz", VaadinIcon.QUESTION.create());
        addButtonIfVisible(menuButtonsRow, AbstractEnglishCrosswordView.ROUTE_NAME, "Crossword", VaadinIcon.PUZZLE_PIECE.create());
        addButtonIfVisible(menuButtonsRow, AbstractEnglishImportExportView.ROUTE_NAME, "Data IE", VaadinIcon.EXCHANGE.create());

        add(menuButtonsRow);
    }
}
