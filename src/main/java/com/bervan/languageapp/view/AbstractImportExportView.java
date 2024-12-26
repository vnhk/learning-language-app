package com.bervan.languageapp.view;

import com.bervan.common.AbstractDataIEView;
import com.bervan.core.model.BervanLogger;
import com.bervan.languageapp.TranslationRecord;
import com.bervan.languageapp.service.ExampleOfUsageService;
import com.bervan.languageapp.service.TranslationRecordService;

import java.util.Collections;

public abstract class AbstractImportExportView extends AbstractDataIEView {
    public static final String ROUTE_NAME = "learning-english-app/ie";

    public AbstractImportExportView(BervanLogger logger, TranslationRecordService translationRecordService, ExampleOfUsageService exampleOfUsageService) {
        super(Collections.singletonList(translationRecordService),
                new LearningEnglishLayout(ROUTE_NAME),
                logger,
                Collections.singletonList(TranslationRecord.class));
    }
}
