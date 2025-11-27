package com.bervan.languageapp.view.es;

import com.bervan.common.config.BervanViewConfig;
import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.view.AbstractImportExportView;

public abstract class AbstractSpanishImportExportView extends AbstractImportExportView {
    public static final String ROUTE_NAME = "learning-language-app/es/import-export";

    public AbstractSpanishImportExportView( TranslationRecordService translationRecordService, BervanViewConfig bervanViewConfig) {
        super( translationRecordService, new LearningSpanishLayout(ROUTE_NAME), "ES", bervanViewConfig);
    }
}
