package com.bervan.languageapp.view;

import com.bervan.common.AbstractPageLayout;
import com.vaadin.flow.component.html.Hr;

public final class LearningEnglishLayout extends AbstractPageLayout {
    public LearningEnglishLayout(String routeName) {
        super(routeName);

        addButton(menuButtonsRow, AbstractLearningAppHomeView.ROUTE_NAME, "Home");
        addButton(menuButtonsRow, AbstractLearningView.ROUTE_NAME, "Flashcards");

        add(menuButtonsRow);
        add(new Hr());
    }
}
