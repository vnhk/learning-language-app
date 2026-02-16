package com.bervan.languageapp.view;

import com.bervan.asynctask.AsyncTask;
import com.bervan.asynctask.AsyncTaskService;
import com.bervan.common.MenuNavigationComponent;
import com.bervan.common.component.AutoConfigurableField;
import com.bervan.common.component.BervanButton;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.search.SearchQueryOption;
import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.SearchService;
import com.bervan.common.search.model.SearchOperation;
import com.bervan.common.search.model.SearchResponse;
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
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import io.micrometer.common.util.StringUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static com.bervan.languageapp.component.ComponentCommonUtils.optimizedAddAudioIfExist;

public abstract class AbstractLearningTableView extends AbstractBervanTableView<UUID, TranslationRecord> {
    protected final String language;
    private final ExampleOfUsageService exampleOfUsageService;
    private final TextToSpeechService textToSpeechService;
    private final TranslatorService translationService;
    private final SearchService searchService;
    private final AsyncTaskService asyncTaskService;
    private Checkbox saveSpeech;
    private Checkbox getImages;

    public AbstractLearningTableView(TranslationRecordService translatorRecordService,
                                     ExampleOfUsageService exampleOfUsageService,
                                     TextToSpeechService textToSpeechService,
                                     TranslatorService translationService,
                                     SearchService searchService,
                                     AsyncTaskService asyncTaskService,
                                     String language,
                                     MenuNavigationComponent pageNavigationComponent, BervanViewConfig bervanViewConfig) {
        super(pageNavigationComponent, translatorRecordService, bervanViewConfig, TranslationRecord.class);
        this.exampleOfUsageService = exampleOfUsageService;
        this.textToSpeechService = textToSpeechService;
        this.translationService = translationService;
        this.searchService = searchService;
        this.asyncTaskService = asyncTaskService;
        this.language = language;
        renderCommonComponents();
    }

    @Override
    protected void customizeTopTableActions(HorizontalLayout topTableActions) {
        super.customizeTopTableActions(topTableActions);

        // Add Fast Import button next to the Add button
        Button fastImportButton = new BervanButton(new Icon(VaadinIcon.FILE_TABLE), e -> openFastImportDialog());
        fastImportButton.addClassName("bervan-icon-btn");
        fastImportButton.addClassName("accent");
        fastImportButton.getElement().setAttribute("title", "Fast Import");
        topTableActions.addComponentAtIndex(1, fastImportButton);
    }

    private void openFastImportDialog() {
        Dialog dialog = new Dialog();
        dialog.setWidth("600px");
        dialog.setCloseOnOutsideClick(false);

        H3 title = new H3("Fast Import");
        title.getStyle().set("margin", "0");

        Button closeButton = new BervanButton(new Icon(VaadinIcon.CLOSE), e -> dialog.close());
        closeButton.addClassName("bervan-icon-btn");

        HorizontalLayout header = new HorizontalLayout(title, closeButton);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        TextArea textArea = new TextArea("';' Separated Words/Sentences");
        textArea.setWidth("100%");
        textArea.setHeight("200px");
        textArea.setPlaceholder("word1; word2; sentence with spaces; ...");

        Checkbox generateSound = new Checkbox("Generate sound", true);
        Checkbox generateExamples = new Checkbox("Generate Examples", true);
        Checkbox generateExamplesWithAI = new Checkbox("Generate Examples using AI", false);
        Checkbox generateImages = new Checkbox("Generate Images", true);
        Checkbox markAllAsToLearn = new Checkbox("Activate and Mark for Learning", true);

        HorizontalLayout optionsRow1 = new HorizontalLayout(generateSound, generateExamples, generateExamplesWithAI);
        optionsRow1.setSpacing(true);

        HorizontalLayout optionsRow2 = new HorizontalLayout(generateImages, markAllAsToLearn);
        optionsRow2.setSpacing(true);

        Button importButton = new BervanButton("Import", e -> {
            String text = textArea.getValue();
            if (text != null && !text.isBlank()) {
                performFastImport(text, generateSound.getValue(), generateExamples.getValue(),
                        generateExamplesWithAI.getValue(), generateImages.getValue(), markAllAsToLearn.getValue());
                dialog.close();
            }
        });
        importButton.addClassName("bervan-icon-btn");
        importButton.addClassName("primary");

        VerticalLayout content = new VerticalLayout(header, textArea, optionsRow1, optionsRow2, importButton);
        content.setSpacing(true);
        content.setPadding(true);
        content.setAlignItems(FlexComponent.Alignment.STRETCH);

        dialog.add(content);
        dialog.open();
    }

    private void performFastImport(String text, boolean generateSound, boolean generateExamples,
                                   boolean generateExamplesWithAI, boolean generateImages, boolean markAllAsToLearn) {
        SecurityContext context = SecurityContextHolder.getContext();

        AsyncTask newAsyncTask = asyncTaskService.createAndStoreAsyncTask();
        new Thread(() -> {
            SecurityContextHolder.setContext(context);
            AsyncTask asyncTask = asyncTaskService.setInProgress(newAsyncTask, "Importing words...");
            try {
                Set<String> alreadyExisting = getAlreadyPresentWords();
                List<String> wordsToAdd = Arrays.stream(text.split(";"))
                        .map(String::trim)
                        .filter(word -> !word.isEmpty() && !alreadyExisting.contains(word))
                        .toList();

                List<TranslationRecord> toSave = new ArrayList<>();
                for (String word : wordsToAdd) {
                    TranslationRecord translationRecord = new TranslationRecord();
                    translationRecord.setSourceText(word);
                    translationRecord.setMarkedForLearning(markAllAsToLearn);
                    translationRecord.setLanguage(language);
                    translationRecord.setTextTranslation(translationService.translate(word, language));

                    if (generateExamples) {
                        try {
                            Map<String, List<String>> exampleOfUsage = exampleOfUsageService.createExampleOfUsage(word, language, 3, generateExamplesWithAI);
                            if (!exampleOfUsage.isEmpty()) {
                                String examples = exampleOfUsage.values().stream().flatMap(Collection::stream).collect(Collectors.joining("; "));
                                translationRecord.setInSentence(examples);
                                translationRecord.setInSentenceTranslation(translationService.translate(examples, language));
                            }
                        } catch (Exception e) {
                            // Continue without examples
                        }
                    }

                    if (generateSound) {
                        try {
                            translationRecord.setTextSound(textToSpeechService.getTextSpeech(translationRecord.getSourceText(), language));
                            if (StringUtils.isNotBlank(translationRecord.getInSentence())) {
                                translationRecord.setInSentenceSound(textToSpeechService.getTextSpeech(translationRecord.getInSentence(), language));
                            }
                        } catch (Exception e) {
                            // Continue without sound
                        }
                    }

                    if (generateImages) {
                        try {
                            ((TranslationRecordService) service).setNewAndReplaceImages(translationRecord);
                        } catch (Exception e) {
                            // Continue without images
                        }
                    }

                    toSave.add(translationRecord);
                }

                ((TranslationRecordService) service).save(toSave);
                asyncTaskService.setFinished(asyncTask, "Import successful: " + wordsToAdd.size() + " items imported!");
            } catch (Exception e) {
                asyncTaskService.setFailed(asyncTask, "Could not import words: " + e.getMessage());
            }
        }).start();

        showPrimaryNotification("Importing started. It might take a while...");
    }

    private Set<String> getAlreadyPresentWords() {
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.addDeletedFalseCriteria(TranslationRecord.class);
        searchRequest.addCriterion("LANGUAGE_CRITERIA", TranslationRecord.class, "language", SearchOperation.EQUALS_OPERATION, language);
        SearchQueryOption options = new SearchQueryOption(TranslationRecord.class);
        options.setPageSize(10000000);
        options.setColumnsToFetch(List.of("sourceText"));
        SearchResponse<TranslationRecord> searchResponse = searchService.search(searchRequest, options);
        return searchResponse.getResultList().stream().map(TranslationRecord::getSourceText).collect(Collectors.toSet());
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
                .withExportButton(isExportable(), service, () -> pathToFileStorage, () -> globalTmpDir)
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
