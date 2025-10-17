package com.bervan.languageapp.view.en;

import com.bervan.common.config.BervanViewConfig;
import com.bervan.core.model.BervanLogger;
import com.bervan.languageapp.service.ExampleOfUsageService;
import com.bervan.languageapp.service.TextToSpeechService;
import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.service.TranslatorService;
import com.bervan.languageapp.view.AbstractLearningTableView;
import com.google.common.collect.ImmutableMap;
import com.vaadin.flow.component.html.Anchor;

import java.util.Map;

public abstract class AbstractEnglishLearningTableView extends AbstractLearningTableView {
    public static final String ROUTE_NAME = "learning-language-app/en/list";
    private final Map<String, String> helpfulLinks = ImmutableMap.of("https://youglish.com", "The page that finds words in youtube videos");

    public AbstractEnglishLearningTableView(TranslationRecordService translatorRecordService,
                                            ExampleOfUsageService exampleOfUsageService,
                                            TextToSpeechService textToSpeechService,
                                            TranslatorService translationService, BervanLogger log, BervanViewConfig bervanViewConfig) {
        super(translatorRecordService, exampleOfUsageService, textToSpeechService, translationService, log, "EN", new LearningEnglishLayout(ROUTE_NAME), bervanViewConfig);
        buildHelpfulPagesLinks();
    }

    private void buildHelpfulPagesLinks() {
        for (Map.Entry<String, String> stringStringEntry : helpfulLinks.entrySet()) {
            Anchor a = new Anchor(stringStringEntry.getKey(), "-" + stringStringEntry.getValue() + " (" + stringStringEntry.getKey() + ")");
            add(a);
        }
    }
}