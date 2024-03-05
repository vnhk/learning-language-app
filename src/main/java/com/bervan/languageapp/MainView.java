package com.bervan.languageapp;

import com.bervan.languageapp.component.Form;
import com.bervan.languageapp.component.TranslationTable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.util.List;


@Route("")
public class MainView extends VerticalLayout {
    private final TranslatorService translatorService;
    private List<TranslationRecord> translations;

    public MainView(TranslatorService translatorService,
                    ExampleOfUsageService exampleOfUsageService,
                    TextToSpeechService textToSpeechService) {
        this.translatorService = translatorService;
        this.translatorService.loadAll();
        translations = translatorService.getAll();
        TranslationTable translationTable = new TranslationTable(translations);
        Form form = new Form(exampleOfUsageService, textToSpeechService, translatorService, translationTable, translations);

        Button learnButtonPage = new Button("Learning");
        learnButtonPage.addClickListener(buttonClickEvent -> {
            UI.getCurrent().navigate("learning");
        });

        add(learnButtonPage, new H1("Translations"), form, translationTable);

        for (Button deleteButton : translationTable.getDeleteButtons()) {
            deleteButton.addClickListener(e -> {
                translatorService.delete(deleteButton.getElement().getAttribute("uuid"));
                translations = translatorService.getAll();
                translationTable.refresh(translations);
            });
        }
    }
}