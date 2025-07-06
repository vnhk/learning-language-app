package com.bervan.languageapp.view;

import com.bervan.common.AbstractBervanTableView;
import com.bervan.common.AutoConfigurableField;
import com.bervan.common.BervanButton;
import com.bervan.common.BervanButtonStyle;
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
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import io.micrometer.common.util.StringUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.bervan.languageapp.component.ComponentCommonUtils.optimizedAddAudioIfExist;

public abstract class AbstractLearningAppHomeView extends AbstractBervanTableView<UUID, TranslationRecord> {
    public static final String ROUTE_NAME = "learning-english-app/home";
    private final ExampleOfUsageService exampleOfUsageService;
    private final TextToSpeechService textToSpeechService;
    private final TranslatorService translationService;
    private Checkbox saveSpeech;
    private Checkbox getImages;
    private BervanButton markToLearnButton;
    private BervanButton markNotToLearnButton;
    private Map<String, String> helpfulLinks = ImmutableMap.of("https://youglish.com", "The page that finds words in youtube videos");

    public AbstractLearningAppHomeView(TranslationRecordService translatorRecordService,
                                       ExampleOfUsageService exampleOfUsageService,
                                       TextToSpeechService textToSpeechService,
                                       TranslatorService translationService, BervanLogger log) {
        super(new LearningEnglishLayout(ROUTE_NAME), translatorRecordService, log, TranslationRecord.class);
        this.exampleOfUsageService = exampleOfUsageService;
        this.textToSpeechService = textToSpeechService;
        this.translationService = translationService;
        renderCommonComponents();
        buildHelpfulPagesLinks();

        markToLearnButton = new BervanButton("Activate", setToLearnEvent -> {
            ConfirmDialog confirmDialog = new ConfirmDialog();
            confirmDialog.setHeader("Confirm activation");
            confirmDialog.setText("Are you sure you want to activate selected item(s)?");

            confirmDialog.setConfirmText("Yes");
            confirmDialog.setConfirmButtonTheme("primary");
            confirmDialog.addConfirmListener(event -> {
                Set<String> itemsId = getSelectedItemsByCheckbox();

                List<TranslationRecord> toSet = data.stream()
                        .filter(e -> e.getId() != null)
                        .filter(e -> itemsId.contains(e.getId().toString()))
                        .filter(e -> !e.isMarkedForLearning())
                        .toList();

                for (TranslationRecord translationRecord : toSet) {
                    translationRecord.setMarkedForLearning(true);
                    TranslationRecord translationRecordInDB = service.loadById(translationRecord.getId()).get();
                    translationRecordInDB.setMarkedForLearning(true);
                    service.save(translationRecordInDB);
                }

                checkboxes.stream().filter(AbstractField::getValue).forEach(e -> e.setValue(false));
                selectAllCheckbox.setValue(false);

                refreshData();
                showSuccessNotification("Changed state of " + toSet.size() + " items");
            });

            confirmDialog.setCancelText("Cancel");
            confirmDialog.setCancelable(true);
            confirmDialog.addCancelListener(event -> {
            });

            confirmDialog.open();
        }, BervanButtonStyle.WARNING);

        markNotToLearnButton = new BervanButton("Deactivate", setNotToLearnEvent -> {
            ConfirmDialog confirmDialog = new ConfirmDialog();
            confirmDialog.setHeader("Confirm deactivation");
            confirmDialog.setText("Are you sure you want to deactivate selected item(s)?");

            confirmDialog.setConfirmText("Yes");
            confirmDialog.setConfirmButtonTheme("primary");
            confirmDialog.addConfirmListener(event -> {
                Set<String> itemsId = getSelectedItemsByCheckbox();

                List<TranslationRecord> toSet = data.stream()
                        .filter(e -> e.getId() != null)
                        .filter(e -> itemsId.contains(e.getId().toString()))
                        .filter(TranslationRecord::isMarkedForLearning)
                        .toList();

                for (TranslationRecord translationRecord : toSet) {
                    translationRecord.setMarkedForLearning(false);
                    TranslationRecord translationRecordInDB = service.loadById(translationRecord.getId()).get();
                    translationRecordInDB.setMarkedForLearning(false);
                    service.save(translationRecordInDB);
                }

                checkboxes.stream().filter(AbstractField::getValue).forEach(e -> e.setValue(false));
                selectAllCheckbox.setValue(false);

                refreshData();
                showSuccessNotification("Changed state of " + toSet.size() + " items");
            });

            confirmDialog.setCancelText("Cancel");
            confirmDialog.setCancelable(true);
            confirmDialog.addCancelListener(event -> {
            });

            confirmDialog.open();
        }, BervanButtonStyle.WARNING);

        buttonsForCheckboxesForVisibilityChange.add(markNotToLearnButton);
        buttonsForCheckboxesForVisibilityChange.add(markToLearnButton);
        for (Button button : buttonsForCheckboxesForVisibilityChange) {
            button.setEnabled(false);
        }

        checkboxActions.remove(checkboxDeleteButton);
        checkboxActions.add(markToLearnButton);
        checkboxActions.add(markNotToLearnButton);
        checkboxActions.add(checkboxDeleteButton);
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

                Map.Entry<Field, AutoConfigurableField> examplesTextField = fieldsHolder.entrySet().stream().filter(e -> e.getKey().getName().equals(TranslationRecord.TranslationRecord_inSentence_columnName))
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

        saveSpeech = getSaveSpeech();
        getImages = getImages();

        formLayout.add(saveSpeech, getImages);
    }

    @Override
    protected TranslationRecord customizeSavingInCreateForm(TranslationRecord newTranslationRecord) {
        if (saveSpeech.getValue()) {
            newTranslationRecord.setTextSound(this.textToSpeechService.getTextSpeech(newTranslationRecord.getSourceText()));
            if (StringUtils.isNotBlank(newTranslationRecord.getInSentence())) {
                newTranslationRecord.setInSentenceSound(this.textToSpeechService.getTextSpeech(newTranslationRecord.getInSentence()));
            }
        }

        if (getImages.getValue()) {
            ((TranslationRecordService) service).setNewAndReplaceImages(newTranslationRecord);
        }

        return newTranslationRecord;
    }

    @Override
    protected void postSearchUpdate(List<TranslationRecord> collect) {
        super.postSearchUpdate(collect);

        ((TranslationRecordService) service).loadImages(collect);
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
                        item.setTextSound(checked ? textToSpeechService.getTextSpeech(sourceText) : null);
                    } else if (clickedColumn.equals(TranslationRecord.TranslationRecord_inSentence_columnName)) {
                        String inSentence = item.getInSentence();
                        item.setInSentenceSound(checked ? textToSpeechService.getTextSpeech(inSentence) : null);
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