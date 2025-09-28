package com.bervan.languageapp.view;

import com.bervan.asynctask.AsyncTask;
import com.bervan.asynctask.AsyncTaskService;
import com.bervan.common.MenuNavigationComponent;
import com.bervan.common.component.BervanButton;
import com.bervan.common.search.SearchQueryOption;
import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.SearchService;
import com.bervan.common.search.model.SearchOperation;
import com.bervan.common.search.model.SearchResponse;
import com.bervan.common.view.AbstractPageView;
import com.bervan.languageapp.TranslationRecord;
import com.bervan.languageapp.service.ExampleOfUsageService;
import com.bervan.languageapp.service.TextToSpeechService;
import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.service.TranslatorService;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractFastImportView extends AbstractPageView {
    private final String language;
    private final Checkbox generateSound = new Checkbox("Generate sound", true);
    private final Checkbox generateExamples = new Checkbox("Generate Examples", true);
    private final Checkbox generateExamplesWithAI = new Checkbox("Generate Examples using AI", false);
    private final Checkbox generateImages = new Checkbox("Generate Images", true);
    private final Checkbox markAllAsToLearn = new Checkbox("Activate and Mark for Learning", true);
    private final TextArea text = new TextArea("';' Separated Words/Sentences");
    private final TranslationRecordService translationRecordService;
    private final TextToSpeechService textToSpeechService;
    private final SearchService searchService;
    private final AsyncTaskService asyncTaskService;
    private final TranslatorService translatorService;
    private final ExampleOfUsageService exampleOfUsageService;
    private final BervanButton loadButton = new BervanButton("Load", buttonClickEvent -> createRecords(text.getValue()));

    public AbstractFastImportView(TranslationRecordService translationRecordService, MenuNavigationComponent menuNavigation, String language, TextToSpeechService textToSpeechService, SearchService searchService, AsyncTaskService asyncTaskService, TranslatorService translatorService, ExampleOfUsageService exampleOfUsageService) {
        this.language = language;
        this.textToSpeechService = textToSpeechService;
        this.translationRecordService = translationRecordService;
        this.searchService = searchService;
        this.asyncTaskService = asyncTaskService;
        this.translatorService = translatorService;
        this.exampleOfUsageService = exampleOfUsageService;
        text.setHeight("200px");
        text.setWidth("600px");
        this.add(menuNavigation);
        this.add(new HorizontalLayout(new VerticalLayout(generateSound), new VerticalLayout(generateExamples, generateExamplesWithAI), new VerticalLayout(generateImages), new VerticalLayout(markAllAsToLearn)));
        this.add(new VerticalLayout(text));
        this.add(new VerticalLayout(loadButton));
    }

    protected void createRecords(String text) {
        //process in another thread to not block the app
        SecurityContext context = SecurityContextHolder.getContext();

        AsyncTask newAsyncTask = asyncTaskService.createAndStoreAsyncTask();
        new Thread(() -> {
            AsyncTask asyncTask = asyncTaskService.setInProgress(newAsyncTask);
            try {
                SecurityContextHolder.setContext(context);
                Set<String> alreadyExisting = getAlreadyPresentWords();
                List<String> wordsToAdd = Arrays.stream(text.split(";")).map(String::trim).filter(word -> !alreadyExisting.contains(word)).toList();

                log.info("Importing {} words in Fast Import", wordsToAdd.size());

                List<TranslationRecord> toSave = new ArrayList<>();
                for (String word : wordsToAdd) {
                    TranslationRecord translationRecord = new TranslationRecord();
                    translationRecord.setSourceText(word);
                    translationRecord.setMarkedForLearning(markAllAsToLearn.getValue());
                    translationRecord.setLanguage(language);
                    translationRecord.setTextTranslation(translatorService.translate(word, language));

                    if (generateExamples.getValue()) {
                        generateExamples(word, translationRecord);
                    }

                    if (generateSound.getValue()) {
                        generateSound(translationRecord);
                    }

                    if (generateImages.getValue()) {
                        generateImages(translationRecord);
                    }

                    toSave.add(translationRecord);
                }

                log.info("Saving {} records in Fast Import", toSave.size());
                translationRecordService.save(toSave);
                asyncTaskService.setFinished(asyncTask, "Import successful: " + wordsToAdd + " imported!");
            } catch (Exception e) {
                log.error("Could not import words!", e);
                asyncTaskService.setFailed(asyncTask, "Could not import words: " + e.getMessage());
            }

        }).start();

        showPrimaryNotification("Importing started. It might take a while...");
    }

    private void generateImages(TranslationRecord translationRecord) {
        try {
            translationRecordService.setNewAndReplaceImages(translationRecord);
        } catch (Exception e) {
            log.error("Could not create images!", e);
        }
    }

    private void generateSound(TranslationRecord translationRecord) {
        try {
            translationRecord.setTextSound(this.textToSpeechService.getTextSpeech(translationRecord.getSourceText(), language));
            if (StringUtils.isNotBlank(translationRecord.getInSentence())) {
                translationRecord.setInSentenceSound(this.textToSpeechService.getTextSpeech(translationRecord.getInSentence(), language));
            }
        } catch (Exception e) {
            log.error("Could not create sound!", e);
        }
    }

    private void generateExamples(String word, TranslationRecord translationRecord) {
        try {
            Map<String, List<String>> exampleOfUsage = exampleOfUsageService.createExampleOfUsage(word, language, 3, generateExamplesWithAI.getValue());
            if (!exampleOfUsage.isEmpty()) {
                String examples = exampleOfUsage.values().stream().flatMap(Collection::stream).collect(Collectors.joining("; "));
                translationRecord.setInSentence(examples);
                translationRecord.setInSentenceTranslation(translatorService.translate(examples, language));
            }
        } catch (Exception e) {
            log.error("Could not create example of usage!", e);
        }
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
}
