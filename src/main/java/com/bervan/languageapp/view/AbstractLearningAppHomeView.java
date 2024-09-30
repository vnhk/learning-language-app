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
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
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
    private final ExampleOfUsageService exampleOfUsageService;
    private final TextToSpeechService textToSpeechService;
    private final TranslatorService translationService;
    private Checkbox saveSpeech;
    private Map<String, String> helpfulLinks = ImmutableMap.of("https://youglish.com", "The page that finds words in youtube videos");

    public AbstractLearningAppHomeView(TranslationRecordService translatorRecordService,
                                       ExampleOfUsageService exampleOfUsageService,
                                       TextToSpeechService textToSpeechService,
                                       TranslatorService translationService, BervanLogger log) {
        super(new LearningEnglishLayout(ROUTE_NAME), translatorRecordService, "Learning Home", log, TranslationRecord.class);
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
    protected void customFieldInCreateLayout(Map<Field, AbstractField> fieldsHolder, Map<Field, VerticalLayout> fieldsLayoutHolder, VerticalLayout formLayout) {
        Map.Entry<Field, AbstractField> sourceTextField = fieldsHolder.entrySet().stream().filter(e -> e.getKey().getName().equals(TranslationRecord.TranslationRecord_sourceText_columnName))
                .findFirst().get();

        for (Map.Entry<Field, AbstractField> fieldMap : fieldsHolder.entrySet()) {
            HorizontalLayout horizontalLayout = new HorizontalLayout(JustifyContentMode.BETWEEN);
            horizontalLayout.setWidthFull();
            horizontalLayout.getThemeList().remove("spacing");
            horizontalLayout.getThemeList().remove("padding");
            Field field = fieldMap.getKey();
            AbstractField formField = fieldMap.getValue();
            if (field.getName().equals(TranslationRecord.TranslationRecord_textTranslation_columnName)) {
                VerticalLayout verticalFieldLayout = fieldsLayoutHolder.get(field);
                Button sourceTextAutoTranslateButton = getFormButton("Auto translate");
                sourceTextAutoTranslateButton.addClassName("option-button");

                sourceTextAutoTranslateButton.addClickListener(click -> {
                    formField.setValue(translate(((TextArea) sourceTextField.getValue())));
                });

                horizontalLayout.add(sourceTextAutoTranslateButton);
                verticalFieldLayout.add(horizontalLayout);
            } else if (field.getName().equals(TranslationRecord.TranslationRecord_inSentence_columnName)) {
                Button findExamplesButton = getFormButton("Generate example sentence");
                findExamplesButton.addClassName("option-button");
                VerticalLayout verticalFieldLayout = fieldsLayoutHolder.get(field);

                findExamplesButton.addClickListener(click -> {
                    List<String> examplesOfUsage = this.exampleOfUsageService.createExampleOfUsage(String.valueOf(sourceTextField.getValue().getValue()));
                    formField.setValue(
                            examplesOfUsage.toString().replace("[", "").replace("]", "")
                    );
                });
                horizontalLayout.add(findExamplesButton);
                verticalFieldLayout.add(horizontalLayout);
            } else if (field.getName().equals(TranslationRecord.TranslationRecord_inSentenceTranslation_columnName)) {
                Button examplesTextAutoTranslateButton = getFormButton("Auto translate");
                examplesTextAutoTranslateButton.addClassName("option-button");
                VerticalLayout verticalFieldLayout = fieldsLayoutHolder.get(field);

                Map.Entry<Field, AbstractField> examplesTextField = fieldsHolder.entrySet().stream().filter(e -> e.getKey().getName().equals(TranslationRecord.TranslationRecord_inSentence_columnName))
                        .findFirst().get();

                examplesTextAutoTranslateButton.addClickListener(click -> {
                    formField.setValue(translate((TextArea) examplesTextField.getValue()));
                });
                horizontalLayout.add(examplesTextAutoTranslateButton);
                verticalFieldLayout.add(horizontalLayout);
            }
        }

        Button navigateToUsageInSentenceOnYoutube = new Button("Open in youglish.com");
        navigateToUsageInSentenceOnYoutube.addClassName("option-button");
        navigateToUsageInSentenceOnYoutube.addClickListener(click -> {
            String hrefFormat = "https://youglish.com/pronounce/%s/english?";
            String href = String.format(hrefFormat, sourceTextField.getValue());
            getUI().get().getPage().open(href);
        });

        CheckboxGroup<Checkbox> saveOptions = new CheckboxGroup<>();
        saveSpeech = getSaveSpeech();
        saveOptions.add(saveSpeech);

        formLayout.add(saveOptions);
    }

    @Override
    protected TranslationRecord customizeSavingInCreateForm(TranslationRecord newTranslationRecord) {
        if (saveSpeech.getValue()) {
            newTranslationRecord.setTextSound(this.textToSpeechService.getTextSpeech(newTranslationRecord.getSourceText()));
            if (StringUtils.isNotBlank(newTranslationRecord.getInSentence())) {
                newTranslationRecord.setInSentenceSound(this.textToSpeechService.getTextSpeech(newTranslationRecord.getInSentence()));
            }
        }
        return newTranslationRecord;
    }

    private Checkbox getSaveSpeech() {
        Checkbox saveSpeech = new Checkbox("Save sound as file", false);
        saveSpeech.setWidth("200px");
        saveSpeech.setId("saveSpeechCheckbox");
        return saveSpeech;
    }

    private String translate(TextArea textArea) {
        try {
            return this.translationService.translate(textArea.getValue());
        } catch (Exception e) {
            showErrorNotification(e.getMessage());
        }
        return "";
    }

    private Button getFormButton(String label) {
        Button button = new Button(label);
        button.setClassName("creating-flashcard-from-buttons");
        return button;
    }

    private void buildHelpfulPagesLinks() {
        for (Map.Entry<String, String> stringStringEntry : helpfulLinks.entrySet()) {
            Anchor a = new Anchor(stringStringEntry.getKey(), "-" + stringStringEntry.getValue() + " (" + stringStringEntry.getKey() + ")");
            add(a);
        }
    }

    @Override
    protected void customPreUpdate(String clickedColumn, VerticalLayout layoutForField, TranslationRecord item, Field finalField, AbstractField finalComponentWithValue) {
        super.customPreUpdate(clickedColumn, layoutForField, item, finalField, finalComponentWithValue);

        int componentCount = layoutForField.getComponentCount();
        for (int i = 0; i < componentCount; i++) {
            Component component = layoutForField.getComponentAt(i);
            if (component.getId().isPresent()) {
                if (component.getId().get().equals("saveSpeechCheckbox")) {
                    Boolean checked = ((Checkbox) component).getValue();
                    if (clickedColumn.equals(TranslationRecord.TranslationRecord_sourceText_columnName)) {
                        String sourceText = item.getSourceText();
                        item.setTextSound(checked ? textToSpeechService.getTextSpeech(sourceText) : null);
                    } else if (clickedColumn.equals(TranslationRecord.TranslationRecord_inSentence_columnName)) {
                        String inSentence = item.getInSentence();
                        item.setInSentenceSound(checked ? textToSpeechService.getTextSpeech(inSentence) : null);
                    }
                }
            }
        }
    }

    @Override
    protected void customFieldInEditLayout(VerticalLayout layoutForField, AbstractField componentWithValue, String clickedColumn, TranslationRecord item) {
        super.customFieldInEditLayout(layoutForField, componentWithValue, clickedColumn, item);

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