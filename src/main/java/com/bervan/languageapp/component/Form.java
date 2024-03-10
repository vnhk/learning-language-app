package com.bervan.languageapp.component;


import com.bervan.languageapp.*;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Form extends VerticalLayout {
    private final ExampleOfUsageService exampleOfUsageService;
    private final TextToSpeechService textToSpeechService;
    private final TranslatorService translatorService;
    private final TranslationRecordService translationRecordService;
    private final int minHeight = 200;
    private final int width = 500;
    private String sourceLanguage = "ES";
    private String targetLanguage = "PL";
    private TranslationRecord newTranslationRecord = new TranslationRecord();
    private List<TranslationRecord> translations;

    public Form(ExampleOfUsageService exampleOfUsageService, TextToSpeechService textToSpeechService, TranslatorService translatorService, TranslationTable translationTable, TranslationRecordService translationRecordService, List<TranslationRecord> translations) {
        this.exampleOfUsageService = exampleOfUsageService;
        this.textToSpeechService = textToSpeechService;
        this.translatorService = translatorService;
        this.translationRecordService = translationRecordService;
        this.translations = translations;
        CheckboxGroup<Checkbox> saveOptions = new CheckboxGroup<>();
        Checkbox saveSpeech = new Checkbox("Save sound as file", true);
        saveSpeech.setWidth("200px");
        saveOptions.add(saveSpeech);

        TextArea sourceLanguageInput = new TextArea();
        TextArea targetLanguageInput = new TextArea();
        sourceLanguageInput.setWidth(width, Unit.PIXELS);
        sourceLanguageInput.setMinHeight(minHeight, Unit.PIXELS);
        sourceLanguageInput.setMaxHeight(minHeight, Unit.PIXELS);
        targetLanguageInput.setWidth(width, Unit.PIXELS);
        targetLanguageInput.setMinHeight(minHeight, Unit.PIXELS);
        targetLanguageInput.setMaxHeight(minHeight, Unit.PIXELS);

        Button textAutoTranslateButton = new Button("Auto translate");
        textAutoTranslateButton.addClickListener(click -> {
            targetLanguageInput.setValue(this.translatorService.translate(sourceLanguageInput.getValue(), sourceLanguage, targetLanguage));
        });


        TextArea inSentenceSourceLanguageInput = new TextArea();
        TextArea inSentenceTargetLanguageInput = new TextArea();
        inSentenceSourceLanguageInput.setWidth(width, Unit.PIXELS);
        inSentenceTargetLanguageInput.setWidth(width, Unit.PIXELS);
        inSentenceSourceLanguageInput.setMinHeight(minHeight, Unit.PIXELS);
        inSentenceTargetLanguageInput.setMinHeight(minHeight, Unit.PIXELS);
        inSentenceSourceLanguageInput.setMaxHeight(minHeight, Unit.PIXELS);
        inSentenceTargetLanguageInput.setMaxHeight(minHeight, Unit.PIXELS);

        Button inSentenceAutoTranslateButton = new Button("Auto translate");
        inSentenceAutoTranslateButton.addClickListener(click -> {
            inSentenceTargetLanguageInput.setValue(this.translatorService.translate(inSentenceSourceLanguageInput.getValue(), sourceLanguage, targetLanguage));
        });

        Button findUsageInSentence = new Button("Generate example sentence");
        findUsageInSentence.addClickListener(click -> {
            List<String> examplesOfUsage = this.exampleOfUsageService.createExampleOfUsage(sourceLanguageInput.getValue());
            inSentenceSourceLanguageInput.setValue(
                    examplesOfUsage.toString().replace("[", "").replace("]", "")
            );
        });

        Button navigateToUsageInSentenceOnYoutube = new Button("Open in youglish.com");
        navigateToUsageInSentenceOnYoutube.addClickListener(click -> {
            String hrefFormat = "https://youglish.com/pronounce/%s/english?";
            String href = String.format(hrefFormat, sourceLanguageInput.getValue());
            getUI().get().getPage().open(href);
        });


        Button addButton = new Button("Add");
        addButton.addClickListener(click -> {
            if (StringUtils.isNotBlank(sourceLanguageInput.getValue()) && StringUtils.isNotBlank(targetLanguageInput.getValue())) {
                newTranslationRecord.setSourceText(sourceLanguageInput.getValue());
                newTranslationRecord.setTextTranslation(targetLanguageInput.getValue());
                newTranslationRecord.setInSentence(inSentenceSourceLanguageInput.getValue());
                newTranslationRecord.setInSentenceTranslation(inSentenceTargetLanguageInput.getValue());

                if (saveSpeech.getValue()) {
                    newTranslationRecord.setTextSound(this.textToSpeechService.getTextSpeech(newTranslationRecord.getSourceText()));
                    if (StringUtils.isNotBlank(newTranslationRecord.getInSentence())) {
                        newTranslationRecord.setInSentenceSound(this.textToSpeechService.getTextSpeech(newTranslationRecord.getInSentence()));
                    }
                }

                TranslationRecord newOne = this.translationRecordService.add(newTranslationRecord);

                this.translations.add(newOne);
                newTranslationRecord = new TranslationRecord();
                translationTable.refresh(this.translations);

                sourceLanguageInput.setValue("");
                targetLanguageInput.setValue("");
                inSentenceSourceLanguageInput.setValue("");
                inSentenceTargetLanguageInput.setValue("");
            }
        });
        addButton.addClickShortcut(Key.ENTER);

        VerticalLayout sourceLanguageLayout = getSourceLanguageLayout(sourceLanguageInput, "Text");
        VerticalLayout inSentenceSourceLanguageLayout = getSourceLanguageLayout(inSentenceSourceLanguageInput, "In sentence");
        inSentenceSourceLanguageLayout.add(findUsageInSentence);
        inSentenceSourceLanguageLayout.add(navigateToUsageInSentenceOnYoutube);
        VerticalLayout targetLanguageLayout = getTargetLanguageLayout(targetLanguageInput, textAutoTranslateButton, "Translation");
        VerticalLayout inSentenceTargetLanguageLayout = getTargetLanguageLayout(inSentenceTargetLanguageInput, inSentenceAutoTranslateButton, "In sentence translation");
        VerticalLayout saveOptionsLayout = getSaveOptionsLayout(saveOptions);
        VerticalLayout addButtonLayout = getSaveButtonLayout(addButton);


        HorizontalLayout container = new HorizontalLayout();
        VerticalLayout inputsContainer = new VerticalLayout();

        HorizontalLayout d1 = new HorizontalLayout();
        d1.add(sourceLanguageLayout);
        d1.add(targetLanguageLayout);

        HorizontalLayout d2 = new HorizontalLayout();
        d2.add(inSentenceSourceLanguageLayout);
        d2.add(inSentenceTargetLanguageLayout);

        VerticalLayout d3 = new VerticalLayout();
        Div div = new Div("Actions:");
        div.setMinHeight(minHeight, Unit.PIXELS);
        div.setMaxHeight(minHeight, Unit.PIXELS);
        d3.add(div);
        d3.add(saveOptionsLayout);
        d3.add(addButtonLayout);

        inputsContainer.add(d1, d2);

        container.add(inputsContainer, d3);

        add(container);
    }

    private static VerticalLayout getSaveButtonLayout(Button addButton) {
        VerticalLayout addButtonLayout = new VerticalLayout();
        addButtonLayout.add(addButton);
        return addButtonLayout;
    }

    private static VerticalLayout getSaveOptionsLayout(CheckboxGroup<Checkbox> saveOptions) {
        VerticalLayout saveOptionsLayout = new VerticalLayout();
        saveOptionsLayout.add(saveOptions);
        return saveOptionsLayout;
    }

    private static VerticalLayout getTargetLanguageLayout(TextArea targetLanguageInput, Button textAutoTranslateButton, String textV) {
        VerticalLayout targetLanguageLayout = new VerticalLayout();
        Text text = new Text(textV);
        targetLanguageLayout.add(text);
        targetLanguageLayout.add(targetLanguageInput);
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.add(textAutoTranslateButton);
        targetLanguageLayout.add(horizontalLayout);
        return targetLanguageLayout;
    }

    private static VerticalLayout getSourceLanguageLayout(TextArea sourceLanguageInput, String textV) {
        VerticalLayout sourceLanguageLayout = new VerticalLayout();
        Text text = new Text(textV);
        sourceLanguageLayout.add(text);
        sourceLanguageLayout.add(sourceLanguageInput);
        HorizontalLayout languageToLearnActions = new HorizontalLayout();
        languageToLearnActions.add(new Div());
        sourceLanguageLayout.add(languageToLearnActions);
        return sourceLanguageLayout;
    }
}
