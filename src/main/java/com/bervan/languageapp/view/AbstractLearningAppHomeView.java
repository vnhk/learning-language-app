package com.bervan.languageapp.view;

import com.bervan.common.MenuNavigationComponent;
import com.bervan.common.view.AbstractPageView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public abstract class AbstractLearningAppHomeView extends AbstractPageView {

    public AbstractLearningAppHomeView(MenuNavigationComponent menuNavigationLayout) {
//        add(menuNavigationLayout);
        // Set the main background style (as in the image)
        addClassName("home-view");
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        // 1. Header and Welcome Section
        add(createHeader());

        // 2. Quick Access (Shortcut Cards)
        add(createQuickAccessSection());

        // 3. Progress Section
        add(createProgressSection());
    }

    private Component createHeader() {
        H1 welcomeHeader = new H1("Welcome back!");
        welcomeHeader.addClassName("welcome-text");

        Paragraph motivation = new Paragraph("Let’s start learning. What are you doing today?");
        motivation.addClassName("motivation-text");

        Div headerLayout = new Div(welcomeHeader, motivation);
        headerLayout.addClassName("header-section");
        return headerLayout;
    }

    private Component createQuickAccessSection() {
        FlexLayout quickAccessLayout = new FlexLayout();
        quickAccessLayout.addClassName("quick-access-grid");
        quickAccessLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        quickAccessLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        // Navigation buttons (styled similarly to menu icons)
        quickAccessLayout.add(createCardButton(
                VaadinIcon.GRID.create(),
                "Word List",
                "Browse and edit your word lists."
        ));

        quickAccessLayout.add(createCardButton(
                VaadinIcon.BOOKMARK.create(), // Flashcards
                "Flashcards",
                "Time for a review!"
        ));

        quickAccessLayout.add(createCardButton(
                VaadinIcon.QUESTION_CIRCLE.create(), // Quiz
                "Quiz",
                "Test your knowledge."
        ));

        quickAccessLayout.add(createCardButton(
                VaadinIcon.INPUT.create(), // Fast Import
                "Fast Import",
                "Quickly import new words."
        ));

        quickAccessLayout.add(createCardButton(
                VaadinIcon.FILE_SEARCH.create(), // Data Import/Export
                "Import/Export",
                "Manage your data."
        ));

        return quickAccessLayout;
    }

    private Component createProgressSection() {
        Div statsPanel = new Div();
        statsPanel.addClassName("stats-panel");

        H3 progressTitle = new H3("Your Progress");
        progressTitle.addClassName("stats-title");

        // Example statistics (will be loaded from a service in the future)
        statsPanel.add(progressTitle);
        statsPanel.add(new Span("Mastered words: 450 / 1200"));
        statsPanel.add(new Span("Last quiz: 85%"));

        return statsPanel;
    }

    // Helper method for creating clickable card buttons
    private Div createCardButton(Icon icon, String title, String description) {
        Div cardButtonDiv = new Div();
        cardButtonDiv.addClassName("card-button");

        // Set icon and text inside the button
        VerticalLayout content = new VerticalLayout();
        content.setAlignItems(FlexComponent.Alignment.CENTER);
        content.setSpacing(false);

        // Icon styling (e.g., cyan/pink to match the menu theme)
        icon.addClassName("card-icon");

        H4 titleText = new H4(title);
        titleText.addClassName("card-title");

        Paragraph descText = new Paragraph(description);
        descText.addClassName("card-description");

        content.add(icon, titleText, descText);
        cardButtonDiv.add(content);

        // Button layout settings
        cardButtonDiv.setWidth("180px");
        cardButtonDiv.setHeight("180px");
        cardButtonDiv.getStyle().set("padding", "10px");

        return cardButtonDiv;
    }
}