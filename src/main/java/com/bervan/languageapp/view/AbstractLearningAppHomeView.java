package com.bervan.languageapp.view;

import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.model.SearchOperation;
import com.bervan.common.view.AbstractHomePageView;
import com.bervan.languageapp.TranslationRecord;
import com.bervan.languageapp.service.TranslationRecordService;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;

import java.util.List;

public abstract class AbstractLearningAppHomeView extends AbstractHomePageView {

    private final String language;
    private final TranslationRecordService translationRecordService;

    public AbstractLearningAppHomeView(TranslationRecordService translationRecordService,
                                       String wordListRoute, String flashcardRoute,
                                       String quizRoute,
                                       String importExportRoute, String language) {
        this.translationRecordService = translationRecordService;
        this.language = language;

        String title = "EN".equals(language) ? "English" : "Spanish";
        add(createHeader(title
                , "Let’s start learning. What are you doing today?"));

        add(createQuickAccessSection(
                List.of("Word List", "Flashcards", "Quiz", "Import/Export"),
                List.of("Browse and edit your word lists.", "Time for a review!", "Test you knowledge!", "Manage data."),
                List.of(VaadinIcon.GRID.create(), VaadinIcon.BOOKMARK.create(), VaadinIcon.QUESTION_CIRCLE.create(), VaadinIcon.FILE_SEARCH.create()),
                List.of(wordListRoute, flashcardRoute, quizRoute, importExportRoute)

        ));

        Div yourProgress = createFooterSection("Your Progress");
        long masteredWords = getMasteredWords();
        long allWords = getAllWords();

        yourProgress.add(new Span("Mastered words: " + masteredWords + " / " + allWords));

        add(yourProgress);
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
}