package com.bervan.languageapp.view;

import com.bervan.common.view.AbstractDataIEView;
import com.bervan.core.model.BervanLogger;
import com.bervan.languageapp.TranslationRecord;
import com.bervan.languageapp.service.TranslationRecordService;

import java.util.UUID;

public abstract class AbstractImportExportView extends AbstractDataIEView<UUID, TranslationRecord> {
    public static final String ROUTE_NAME = "learning-english-app/import-export";

    public AbstractImportExportView(BervanLogger logger, TranslationRecordService translationRecordService) {
        super(translationRecordService,
                new LearningEnglishLayout(ROUTE_NAME),
                logger,
                TranslationRecord.class);
    }
}
