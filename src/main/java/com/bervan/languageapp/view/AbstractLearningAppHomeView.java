package com.bervan.languageapp.view;

import com.bervan.languageapp.TranslationRecord;
import com.bervan.languageapp.component.Form;
import com.bervan.languageapp.component.TranslationTable;
import com.bervan.languageapp.service.ExampleOfUsageService;
import com.bervan.languageapp.service.TextToSpeechService;
import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.service.TranslatorService;
import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractLearningAppHomeView extends VerticalLayout {
    public static final String ROUTE_NAME = "learning-english-app/home";
    private final TranslationRecordService translatorRecordService;
    private List<TranslationRecord> translations;
    private Map<String, String> helpfulLinks = ImmutableMap.of("https://youglish.com", "The page that fins words in youtube videos");

    public AbstractLearningAppHomeView(TranslationRecordService translatorRecordService,
                                       ExampleOfUsageService exampleOfUsageService,
                                       TextToSpeechService textToSpeechService,
                                       TranslatorService translationService) {
        this.translatorRecordService = translatorRecordService;
        translations = translatorRecordService.getAll();
        TranslationTable translationTable = new TranslationTable(translations);
        Form form = new Form(exampleOfUsageService, textToSpeechService, translationService, translationTable, translatorRecordService, translations);

        Button learnButtonPage = new Button("Learning");
        learnButtonPage.addClickListener(buttonClickEvent -> {
            UI.getCurrent().navigate(AbstractLearningView.ROUTE_NAME);
        });

        buildHelpfulPagesLinks();

        add(learnButtonPage, new H1("Translations"), form, translationTable);

        for (Button deleteButton : translationTable.getDeleteButtons()) {
            deleteButton.addClickListener(e -> {
                translatorRecordService.delete(UUID.fromString(deleteButton.getElement().getAttribute("uuid")));
                translations = translatorRecordService.getAll();
                translationTable.refresh(translations);
            });
        }
    }

    private void buildHelpfulPagesLinks() {
        for (Map.Entry<String, String> stringStringEntry : helpfulLinks.entrySet()) {
            Anchor a = new Anchor(stringStringEntry.getKey(), "-" + stringStringEntry.getValue() + " (" + stringStringEntry.getKey() + ")");
            add(a);
        }
    }
}