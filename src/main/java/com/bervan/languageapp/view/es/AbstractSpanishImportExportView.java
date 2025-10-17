package com.bervan.languageapp.view.es;

import com.bervan.common.config.BervanViewConfig;
import com.bervan.core.model.BervanLogger;
import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.view.AbstractImportExportView;

public abstract class AbstractSpanishImportExportView extends AbstractImportExportView {
    public static final String ROUTE_NAME = "learning-language-app/es/import-export";

    public AbstractSpanishImportExportView(BervanLogger logger, TranslationRecordService translationRecordService, BervanViewConfig bervanViewConfig) {
        super(logger, translationRecordService, new LearningSpanishLayout(ROUTE_NAME), "ES", bervanViewConfig);
    }
}
