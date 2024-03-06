package com.bervan.languageapp;

import com.bervan.languageapp.component.Form;
import com.bervan.languageapp.component.TranslationTable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.util.List;
import java.util.UUID;


@Route("")
public class MainView extends VerticalLayout {
    private final TranslationRecordService translatorRecordService;
    private List<TranslationRecord> translations;

    public MainView(TranslationRecordService translatorRecordService,
                    ExampleOfUsageService exampleOfUsageService,
                    TextToSpeechService textToSpeechService,
                    TranslatorService translationService) {
        this.translatorRecordService = translatorRecordService;
        translations = translatorRecordService.getAll();
        TranslationTable translationTable = new TranslationTable(translations);
        Form form = new Form(exampleOfUsageService, textToSpeechService, translationService, translationTable, translatorRecordService, translations);

        Button learnButtonPage = new Button("Learning");
        learnButtonPage.addClickListener(buttonClickEvent -> {
            UI.getCurrent().navigate("learning");
        });

        add(learnButtonPage, new H1("Translations"), form, translationTable);

        for (Button deleteButton : translationTable.getDeleteButtons()) {
            deleteButton.addClickListener(e -> {
                translatorRecordService.delete(UUID.fromString(deleteButton.getElement().getAttribute("uuid")));
                translations = translatorRecordService.getAll();
                translationTable.refresh(translations);
            });
        }
    }
}