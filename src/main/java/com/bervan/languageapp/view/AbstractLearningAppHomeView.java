package com.bervan.languageapp.view;

import com.bervan.common.AbstractTableView;
import com.bervan.core.model.BervanLogger;
import com.bervan.languageapp.TranslationRecord;
import com.bervan.languageapp.service.ExampleOfUsageService;
import com.bervan.languageapp.service.TextToSpeechService;
import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.service.TranslatorService;
import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import io.micrometer.common.util.StringUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static com.bervan.languageapp.component.ComponentCommonUtils.optimizedAddAudioIfExist;

public abstract class AbstractLearningAppHomeView extends AbstractTableView<TranslationRecord> {
    public static final String ROUTE_NAME = "learning-english-app/home";
    private final TranslationRecordService translatorRecordService;
    private final ExampleOfUsageService exampleOfUsageService;
    private final TextToSpeechService textToSpeechService;
    private final TranslatorService translationService;
    private Map<String, String> helpfulLinks = ImmutableMap.of("https://youglish.com", "The page that finds words in youtube videos");

    public AbstractLearningAppHomeView(TranslationRecordService translatorRecordService,
                                       ExampleOfUsageService exampleOfUsageService,
                                       TextToSpeechService textToSpeechService,
                                       TranslatorService translationService, BervanLogger log) {
        super(new LearningEnglishLayout(ROUTE_NAME), translatorRecordService, "Learning Home", log);
        this.translatorRecordService = translatorRecordService;
        this.exampleOfUsageService = exampleOfUsageService;
        this.textToSpeechService = textToSpeechService;
        this.translationService = translationService;
        renderCommonComponents();
        buildHelpfulPagesLinks();
    }

    @Override
    protected Grid<TranslationRecord> getGrid() {
        Grid<TranslationRecord> grid = new Grid<>(TranslationRecord.class, false);
        buildGridAutomatically(grid);
        return grid;
    }

    @Override
    protected void customizeTextColumnUpdater(Span span, TranslationRecord record, Field f) {
        super.customizeTextColumnUpdater(span, record, f);
        if (f.getName().equals(TranslationRecord.TranslationRecord_sourceText_columnName)) {
            optimizedAddAudioIfExist(span, record.getTextSound());
        } else if (f.getName().equals(TranslationRecord.TranslationRecord_inSentence_columnName)) {
            optimizedAddAudioIfExist(span, record.getInSentenceSound());
        }
    }

    @Override
    protected void buildNewItemDialogContent(Dialog dialog, VerticalLayout dialogLayout, HorizontalLayout headerLayout) {
        TextArea sourceTextField = getTextFormComponent("Source Text");
        HorizontalLayout row1Layout = new HorizontalLayout(sourceTextField);

        TextArea sourceTranslationField = getTextFormComponent("Translation");
        Button sourceTextAutoTranslateButton = getFormButton("Auto translate");
        sourceTextAutoTranslateButton.addClassName("option-button");
        sourceTextAutoTranslateButton.addClickListener(click -> {
            sourceTranslationField.setValue(translate(sourceTextField));
        });
        HorizontalLayout row2Layout = new HorizontalLayout(sourceTranslationField, sourceTextAutoTranslateButton);

        TextArea examplesTextField = getTextFormComponent("Examples");
        Button findExamplesButton = getFormButton("Generate example sentence");
        findExamplesButton.addClassName("option-button");
        findExamplesButton.addClickListener(click -> {
            List<String> examplesOfUsage = this.exampleOfUsageService.createExampleOfUsage(sourceTextField.getValue());
            examplesTextField.setValue(
                    examplesOfUsage.toString().replace("[", "").replace("]", "")
            );
        });
        HorizontalLayout row3Layout = new HorizontalLayout(examplesTextField, findExamplesButton);

        TextArea examplesTranslationField = getTextFormComponent("Translation");
        Button examplesTextAutoTranslateButton = getFormButton("Auto translate");
        examplesTextAutoTranslateButton.addClassName("option-button");
        examplesTextAutoTranslateButton.addClickListener(click -> {
            examplesTranslationField.setValue(translate(examplesTextField));
        });
        HorizontalLayout row4Layout = new HorizontalLayout(examplesTranslationField, examplesTextAutoTranslateButton);

        Button navigateToUsageInSentenceOnYoutube = new Button("Open in youglish.com");
        navigateToUsageInSentenceOnYoutube.addClassName("option-button");
        navigateToUsageInSentenceOnYoutube.addClickListener(click -> {
            String hrefFormat = "https://youglish.com/pronounce/%s/english?";
            String href = String.format(hrefFormat, sourceTextField.getValue());
            getUI().get().getPage().open(href);
        });

        CheckboxGroup<Checkbox> saveOptions = new CheckboxGroup<>();
        Checkbox saveSpeech = getSaveSpeech();
        saveOptions.add(saveSpeech);

        Button saveButton = new Button("Save");
        saveButton.addClassName("option-button");

        saveButton.addClickListener(click -> {
            TranslationRecord newTranslationRecord = new TranslationRecord();
            if (StringUtils.isNotBlank(sourceTextField.getValue()) && StringUtils.isNotBlank(sourceTranslationField.getValue())) {
                newTranslationRecord.setSourceText(sourceTextField.getValue());
                newTranslationRecord.setTextTranslation(sourceTranslationField.getValue());
                newTranslationRecord.setInSentence(examplesTextField.getValue());
                newTranslationRecord.setInSentenceTranslation(examplesTranslationField.getValue());
                newTranslationRecord.setFactor(1);

                if (saveSpeech.getValue()) {
                    newTranslationRecord.setTextSound(this.textToSpeechService.getTextSpeech(newTranslationRecord.getSourceText()));
                    if (StringUtils.isNotBlank(newTranslationRecord.getInSentence())) {
                        newTranslationRecord.setInSentenceSound(this.textToSpeechService.getTextSpeech(newTranslationRecord.getInSentence()));
                    }
                }

                TranslationRecord newOne = this.translatorRecordService.save(newTranslationRecord);

                data.add(newOne);
                grid.getDataProvider().refreshAll();

                sourceTextField.setValue("");
                sourceTranslationField.setValue("");
                examplesTextField.setValue("");
                examplesTranslationField.setValue("");
                saveSpeech.setValue(false);
                dialog.close();
            }
        });

        dialogLayout.add(headerLayout, row1Layout, row2Layout, row3Layout, row4Layout, saveOptions, saveButton);
    }

    private Checkbox getSaveSpeech() {
        Checkbox saveSpeech = new Checkbox("Save sound as file", false);
        saveSpeech.setWidth("200px");
        return saveSpeech;
    }

    private String translate(TextArea textArea) {
        try {
            return this.translationService.translate(textArea.getValue());
        } catch (Exception e) {
            Notification.show(e.getMessage());
        }
        return "";
    }

    private Button getFormButton(String label) {
        Button button = new Button(label);
        button.setClassName("creating-flashcard-from-buttons");
        return button;
    }

    private TextArea getTextFormComponent(String label) {
        TextArea textField = new TextArea(label);
        textField.setClassName("creating-flashcard-from-inputs");
        return textField;
    }

    private void buildHelpfulPagesLinks() {
        for (Map.Entry<String, String> stringStringEntry : helpfulLinks.entrySet()) {
            Anchor a = new Anchor(stringStringEntry.getKey(), "-" + stringStringEntry.getValue() + " (" + stringStringEntry.getKey() + ")");
            add(a);
        }
    }

    @Override
    protected void customizeSaving(Field field, VerticalLayout layoutForField, String clickedColumn, TranslationRecord item) {
        if (clickedColumn.equals(TranslationRecord.TranslationRecord_sourceText_columnName)) {
            item.setTextSound(null);
            Checkbox checkboxSound = (Checkbox) layoutForField.getComponentAt(1);
            if (checkboxSound.getValue()) {
                item.setTextSound(textToSpeechService.getTextSpeech(item.getSourceText()));
            }
        } else if (clickedColumn.equals(TranslationRecord.TranslationRecord_inSentence_columnName)) {
            item.setInSentenceSound(null);
            Checkbox checkboxSound = (Checkbox) layoutForField.getComponentAt(1);
            if (checkboxSound.getValue()) {
                item.setInSentenceSound(textToSpeechService.getTextSpeech(item.getInSentence()));
            }
        }
    }

    @Override
    protected void customFieldLayout(VerticalLayout layoutForField, AbstractField componentWithValue, String clickedColumn, TranslationRecord item) {
        super.customFieldLayout(layoutForField, componentWithValue, clickedColumn, item);

        if (clickedColumn.equals(TranslationRecord.TranslationRecord_sourceText_columnName)) {
            Checkbox saveSpeech = getSaveSpeech();
            saveSpeech.setValue(item.getTextSound() != null);
            layoutForField.add(saveSpeech);
        } else if (clickedColumn.equals(TranslationRecord.TranslationRecord_inSentence_columnName)) {
            Checkbox saveSpeech = getSaveSpeech();
            saveSpeech.setValue(item.getInSentenceSound() != null);
            layoutForField.add(saveSpeech);
        }
    }
}