package com.bervan.languageapp.view;

import com.bervan.common.MenuNavigationComponent;
import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.model.SearchOperation;
import com.bervan.common.view.AbstractBervanTableView;
import com.bervan.languageapp.TranslationRecord;
import com.bervan.languageapp.component.LearningLanguageTableToolbar;
import com.bervan.languageapp.service.ExampleOfUsageService;
import com.bervan.languageapp.service.TextToSpeechService;
import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.service.TranslatorService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import io.micrometer.common.util.StringUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.bervan.languageapp.component.ComponentCommonUtils.optimizedAddAudioIfExist;

public abstract class AbstractLearningTableView extends AbstractBervanTableView<UUID, TranslationRecord> {
    protected final String language;
    private final ExampleOfUsageService exampleOfUsageService;
    private final TextToSpeechService textToSpeechService;
    private final TranslatorService translationService;
    private Checkbox saveSpeech;
    private Checkbox getImages;

    public AbstractLearningTableView(TranslationRecordService translatorRecordService,
                                     ExampleOfUsageService exampleOfUsageService,
                                     TextToSpeechService textToSpeechService,
                                     TranslatorService translationService,
                                     String language,
                                     MenuNavigationComponent pageNavigationComponent, BervanViewConfig bervanViewConfig) {
        super(pageNavigationComponent, translatorRecordService, bervanViewConfig, TranslationRecord.class);
        this.exampleOfUsageService = exampleOfUsageService;
        this.textToSpeechService = textToSpeechService;
        this.translationService = translationService;
        this.language = language;
        renderCommonComponents();
    }

    @Override
    protected void buildToolbarActionBar() {
        LearningLanguageTableToolbar toolbar = new LearningLanguageTableToolbar(
                checkboxes, data, selectAllCheckbox, buttonsForCheckboxesForVisibilityChange, service, bervanViewConfig,
                (v) -> {
                    refreshData();
                    return v;
                });

        // Pass floating toolbar for custom actions (if enabled)
        if (floatingToolbar != null) {
            toolbar.withFloatingToolbar(floatingToolbar);
        }

        tableToolbarActions = toolbar
                .withMarkToLearn()
                .withMarkNotToLearn()
                .withEditButton(service)
                .withDeleteButton()
                .withExportButton(isExportable(), service, pathToFileStorage, globalTmpDir)
                .build();
    }

    @Override
    protected void customizePreLoad(SearchRequest request) {
        request.addCriterion("LANGUAGE_CRITERIA", TranslationRecord.class,
                "language", SearchOperation.EQUALS_OPERATION, language);
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
    protected void customFieldInCreateItemLayout(Map<Field, AutoConfigurableField> fieldsHolder, Map<Field, VerticalLayout> fieldsLayoutHolder, VerticalLayout formLayout) {
        Map.Entry<Field, AutoConfigurableField> sourceTextField = fieldsHolder.entrySet().stream().filter(e -> e.getKey().getName().equals(TranslationRecord.TranslationRecord_sourceText_columnName))
                .findFirst().get();

        for (Map.Entry<Field, AutoConfigurableField> fieldMap : fieldsHolder.entrySet()) {
            HorizontalLayout horizontalLayout = new HorizontalLayout(JustifyContentMode.BETWEEN);
            horizontalLayout.setWidthFull();
            horizontalLayout.getThemeList().remove("spacing");
            horizontalLayout.getThemeList().remove("padding");
            Field field = fieldMap.getKey();
            AutoConfigurableField formField = fieldMap.getValue();
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
                    List<String> examplesOfUsage = this.exampleOfUsageService.createExampleOfUsage(String.valueOf(sourceTextField.getValue().getValue()), language);
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

                Map.Entry<Field, AutoConfigurableField> examplesTextField = fieldsHolder.entrySet().stream().filter(e -> e.getKey().getName().equals(TranslationRecord.TranslationRecord_inSentence_columnName))
                        .findFirst().get();

                examplesTextAutoTranslateButton.addClickListener(click -> {
                    formField.setValue(translate((TextArea) examplesTextField.getValue()));
                });
                horizontalLayout.add(examplesTextAutoTranslateButton);
                verticalFieldLayout.add(horizontalLayout);
            }
        }

        saveSpeech = getSaveSpeech();
        getImages = getImages();

        formLayout.add(saveSpeech, getImages);
    }

    @Override
    protected TranslationRecord preSaveActions(TranslationRecord newTranslationRecord) {
        if (saveSpeech.getValue()) {
            newTranslationRecord.setTextSound(this.textToSpeechService.getTextSpeech(newTranslationRecord.getSourceText(), language));
            if (StringUtils.isNotBlank(newTranslationRecord.getInSentence())) {
                newTranslationRecord.setInSentenceSound(this.textToSpeechService.getTextSpeech(newTranslationRecord.getInSentence(), language));
            }
        }

        if (getImages.getValue()) {
            ((TranslationRecordService) service).setNewAndReplaceImages(newTranslationRecord);
        }

        newTranslationRecord.setLanguage(language);

        return newTranslationRecord;
    }

    @Override
    protected void postSearchUpdate(List<TranslationRecord> collect) {
//        super.postSearchUpdate(collect);

//        ((TranslationRecordService) service).loadImages(collect);
    }

    private Checkbox getSaveSpeech() {
        Checkbox saveSpeech = new Checkbox("Save sound as file", false);
        saveSpeech.setWidth("200px");
        saveSpeech.setId("saveSpeechCheckbox");
        return saveSpeech;
    }

    private Checkbox getImages() {
        Checkbox checkbox = new Checkbox("Load new images", false);
        checkbox.setWidth("200px");
        checkbox.setId("loadImagesCheckbox");
        return checkbox;
    }

    private Checkbox getReloadNextLearningDate() {
        Checkbox checkbox = new Checkbox("Recalculate next learning date", true);
        checkbox.setWidth("200px");
        checkbox.setId("reloadNextLearningDate");
        return checkbox;
    }

    private String translate(TextArea textArea) {
        try {
            return this.translationService.translate(textArea.getValue(), language);
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

    @Override
    protected TranslationRecord customPreUpdate(String clickedColumn, VerticalLayout layoutForField, TranslationRecord item, Field finalField, AutoConfigurableField finalComponentWithValue) {
        item = super.customPreUpdate(clickedColumn, layoutForField, item, finalField, finalComponentWithValue);

        int componentCount = layoutForField.getComponentCount();
        for (int i = 0; i < componentCount; i++) {
            Component component = layoutForField.getComponentAt(i);
            if (component.getId().isPresent()) {
                if (component.getId().get().equals("saveSpeechCheckbox")) {
                    Boolean checked = ((Checkbox) component).getValue();
                    if (clickedColumn.equals(TranslationRecord.TranslationRecord_sourceText_columnName)) {
                        String sourceText = item.getSourceText();
                        item.setTextSound(checked ? textToSpeechService.getTextSpeech(sourceText, language) : null);
                    } else if (clickedColumn.equals(TranslationRecord.TranslationRecord_inSentence_columnName)) {
                        String inSentence = item.getInSentence();
                        item.setInSentenceSound(checked ? textToSpeechService.getTextSpeech(inSentence, language) : null);
                    }
                } else if (component.getId().get().equals("reloadNextLearningDate")) {
                    Boolean checked = ((Checkbox) component).getValue();
                    if (checked && clickedColumn.equals(TranslationRecord.TranslationRecord_factor_columnName)) {
                        Integer newFactor = item.getFactor();
                        item.setNextRepeatTime(TranslationRecordService.getNextRepeatTime(newFactor, ""));
                    }
                } else if (component.getId().get().equals("loadImagesCheckbox")) {
                    Boolean checked = ((Checkbox) component).getValue();
                    if (checked && clickedColumn.equals(TranslationRecord.TranslationRecord_images_columnName)) {
                        ((TranslationRecordService) service).setNewAndReplaceImages(item);
                    }
                }
            }
        }

        item.setLanguage(language);

        return item;
    }

    @Override
    protected void customFieldInEditLayout(VerticalLayout layoutForField, AutoConfigurableField componentWithValue, String clickedColumn, TranslationRecord item) {
        super.customFieldInEditLayout(layoutForField, componentWithValue, clickedColumn, item);

        if (clickedColumn.equals(TranslationRecord.TranslationRecord_sourceText_columnName)) {
            Checkbox saveSpeech = getSaveSpeech();
            saveSpeech.setValue(item.getTextSound() != null);
            layoutForField.add(saveSpeech);
        } else if (clickedColumn.equals(TranslationRecord.TranslationRecord_inSentence_columnName)) {
            Checkbox saveSpeech = getSaveSpeech();
            saveSpeech.setValue(item.getInSentenceSound() != null);
            layoutForField.add(saveSpeech);
        } else if (clickedColumn.equals(TranslationRecord.TranslationRecord_factor_columnName)) {
            Checkbox reloadNextLearningDate = getReloadNextLearningDate();
            layoutForField.add(reloadNextLearningDate);
        } else if (clickedColumn.equals(TranslationRecord.TranslationRecord_images_columnName)) {
            Checkbox imagesCheckbox = getImages();
            layoutForField.add(imagesCheckbox);
        }
    }
}