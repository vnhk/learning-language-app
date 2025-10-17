package com.bervan.languageapp.view.en;

import com.bervan.common.config.BervanViewConfig;
import com.bervan.core.model.BervanLogger;
import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.view.AbstractImportExportView;

public abstract class AbstractEnglishImportExportView extends AbstractImportExportView {
    public static final String ROUTE_NAME = "learning-language-app/en/import-export";

    public AbstractEnglishImportExportView(BervanLogger logger, TranslationRecordService translationRecordService, BervanViewConfig bervanViewConfig) {
        super(logger, translationRecordService, new LearningEnglishLayout(ROUTE_NAME), "EN", bervanViewConfig);
    }
}
