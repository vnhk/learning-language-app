package com.bervan.languageapp.view;

import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.model.SearchOperation;
import com.bervan.common.view.AbstractPageView;
import com.bervan.languageapp.TranslationRecord;
import com.bervan.languageapp.service.TranslationRecordService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public abstract class AbstractLearningAppHomeView extends AbstractPageView {

    private final String WORD_LIST_ROUTE;
    private final String FLASHCARD_ROUTE;
    private final String QUIZ_ROUTE;
    private final String FAST_IMPORT_ROUTE;
    private final String IMPORT_EXPORT_ROUTE;
    private final String language;
    private final TranslationRecordService translationRecordService;

    public AbstractLearningAppHomeView(TranslationRecordService translationRecordService,
                                       String wordListRoute, String flashcardRoute,
                                       String quizRoute, String fastImportRoute,
                                       String importExportRoute, String language) {
        this.translationRecordService = translationRecordService;
        this.language = language;
        this.WORD_LIST_ROUTE = wordListRoute;
        this.FLASHCARD_ROUTE = flashcardRoute;
        this.QUIZ_ROUTE = quizRoute;
        this.FAST_IMPORT_ROUTE = fastImportRoute;
        this.IMPORT_EXPORT_ROUTE = importExportRoute;
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
                "Browse and edit your word lists.",
                WORD_LIST_ROUTE
        ));

        quickAccessLayout.add(createCardButton(
                VaadinIcon.BOOKMARK.create(), // Flashcards
                "Flashcards",
                "Time for a review!",
                FLASHCARD_ROUTE
        ));

        quickAccessLayout.add(createCardButton(
                VaadinIcon.QUESTION_CIRCLE.create(), // Quiz
                "Quiz",
                "Test your knowledge.",
                QUIZ_ROUTE
        ));

        quickAccessLayout.add(createCardButton(
                VaadinIcon.INPUT.create(), // Fast Import
                "Fast Import",
                "Quickly import new words.",
                FAST_IMPORT_ROUTE
        ));

        quickAccessLayout.add(createCardButton(
                VaadinIcon.FILE_SEARCH.create(), // Data Import/Export
                "Import/Export",
                "Manage your data.",
                IMPORT_EXPORT_ROUTE
        ));

        return quickAccessLayout;
    }

    private Component createProgressSection() {
        Div statsPanel = new Div();
        statsPanel.addClassName("stats-panel");

        H3 progressTitle = new H3("Your Progress");
        progressTitle.addClassName("stats-title");

        long masteredWords = getMasteredWords();
        long allWords = getAllWords();

        statsPanel.add(progressTitle);
        statsPanel.add(new Span("Mastered words: " + masteredWords + " / " + allWords));

        return statsPanel;
    }

    private long getAllWords() {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.addDeletedFalseCriteria(TranslationRecord.class);
        searchRequest.addCriterion("LANGUAGE_CRITERIA", TranslationRecord.class, "language", SearchOperation.EQUALS_OPERATION, language);
        return translationRecordService.loadCount(searchRequest);
    }

    private long getMasteredWords() {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.addDeletedFalseCriteria(TranslationRecord.class);
        searchRequest.addCriterion("LANGUAGE_CRITERIA", TranslationRecord.class, "language", SearchOperation.EQUALS_OPERATION, language);
        searchRequest.addCriterion("FACTOR_CRITERIA", TranslationRecord.class, "factor", SearchOperation.GREATER_EQUAL_OPERATION, 512);
        return translationRecordService.loadCount(searchRequest);
    }

    // Helper method for creating clickable card buttons
    private Div createCardButton(Icon icon, String title, String description, String route) {
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

        cardButtonDiv.addClickListener(e -> {
            getUI().ifPresent(ui -> ui.navigateToClient(route));
        });

        return cardButtonDiv;
    }
}