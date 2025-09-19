package com.bervan.languageapp.view.en;

import com.bervan.core.model.BervanLogger;
import com.bervan.languageapp.TranslationRecord;
import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.view.AbstractImportExportView;

public abstract class AbstractEnglishImportExportView extends AbstractImportExportView {
    public static final String ROUTE_NAME = "learning-language-app/en/import-export";

    public AbstractEnglishImportExportView(BervanLogger logger, TranslationRecordService translationRecordService) {
        super(logger, translationRecordService, "EN");
    }
}
