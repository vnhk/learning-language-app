package com.bervan.languageapp.view.es;

import com.bervan.common.MenuNavigationComponent;
import com.vaadin.flow.component.icon.VaadinIcon;

public final class LearningSpanishLayout extends MenuNavigationComponent {
    public LearningSpanishLayout(String routeName) {
        super(routeName);

        addButtonIfVisible(menuButtonsRow, AbstractSpanishLearningAppHomeView.ROUTE_NAME, "Home", VaadinIcon.HOME.create());
        addButtonIfVisible(menuButtonsRow, AbstractSpanishLearningView.ROUTE_NAME, "Flashcards", VaadinIcon.BOOKMARK.create());
        addButtonIfVisible(menuButtonsRow, AbstractSpanishQuizView.ROUTE_NAME, "Quiz", VaadinIcon.QUESTION.create());
        addButtonIfVisible(menuButtonsRow, AbstractSpanishCrosswordView.ROUTE_NAME, "Crossword", VaadinIcon.PUZZLE_PIECE.create());
        addButtonIfVisible(menuButtonsRow, AbstractSpanishImportExportView.ROUTE_NAME, "Data IE", VaadinIcon.EXCHANGE.create());
        addButtonIfVisible(menuButtonsRow, AbstractSpanishFastImportView.ROUTE_NAME, "Fast Import", VaadinIcon.INPUT.create());

        add(menuButtonsRow);
    }
}
