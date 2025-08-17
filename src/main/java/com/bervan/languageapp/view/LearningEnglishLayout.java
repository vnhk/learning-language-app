package com.bervan.languageapp.view;

import com.bervan.common.MenuNavigationComponent;
import com.vaadin.flow.component.icon.VaadinIcon;

public final class LearningEnglishLayout extends MenuNavigationComponent {
    public LearningEnglishLayout(String routeName) {
        super(routeName);

        addButtonIfVisible(menuButtonsRow, AbstractLearningAppHomeView.ROUTE_NAME, "Home", VaadinIcon.HOME.create());
        addButtonIfVisible(menuButtonsRow, AbstractLearningView.ROUTE_NAME, "Flashcards", VaadinIcon.HOME.create());
        addButtonIfVisible(menuButtonsRow, AbstractQuizView.ROUTE_NAME, "Quiz", VaadinIcon.HOME.create());
        addButtonIfVisible(menuButtonsRow, AbstractCrosswordView.ROUTE_NAME, "Crossword", VaadinIcon.HOME.create());
        addButtonIfVisible(menuButtonsRow, AbstractImportExportView.ROUTE_NAME, "Data IE", VaadinIcon.HOME.create());

        add(menuButtonsRow);
    }
}
