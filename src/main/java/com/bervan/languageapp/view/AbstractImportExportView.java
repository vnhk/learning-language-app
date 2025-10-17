package com.bervan.languageapp.view;

import com.bervan.common.MenuNavigationComponent;
import com.bervan.common.config.BervanViewConfig;
import com.bervan.common.search.SearchRequest;
import com.bervan.common.search.model.SearchOperation;
import com.bervan.common.view.AbstractDataIEView;
import com.bervan.core.model.BervanLogger;
import com.bervan.ieentities.ExcelIEEntity;
import com.bervan.languageapp.TranslationRecord;
import com.bervan.languageapp.service.TranslationRecordService;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public abstract class AbstractImportExportView extends AbstractDataIEView<UUID, TranslationRecord> {
    private final String language;

    public AbstractImportExportView(BervanLogger logger, TranslationRecordService translationRecordService, MenuNavigationComponent menuNavigation, String language, BervanViewConfig bervanViewConfig) {
        super(translationRecordService,
                menuNavigation,
                bervanViewConfig,
                logger,
                TranslationRecord.class);
        this.language = language;
    }

    @Override
    protected SearchRequest getRequestForDataExport() {
        SearchRequest requestForDataExport = super.getRequestForDataExport();
        requestForDataExport.addCriterion("LANGUAGE_CRITERIA", TranslationRecord.class,
                "language", SearchOperation.EQUALS_OPERATION, language);
        return requestForDataExport;
    }

    @Override
    protected void postDataLoad(List<? extends ExcelIEEntity<UUID>> objects) {
        Iterator<? extends ExcelIEEntity<UUID>> iterator = objects.iterator();
        while (iterator.hasNext()) {
            ExcelIEEntity<UUID> object = iterator.next();
            if (object instanceof TranslationRecord translationRecord) {
                if (translationRecord.getLanguage() == null) {
                    translationRecord.setLanguage(this.language);
                } else if (!translationRecord.getLanguage().equals(this.language)) {
                    logger.warn("Language of record " + translationRecord.getId() + " does not match the language of the view (" + this.language + "). It will not be imported");
                    iterator.remove();
                }
            } else {
                throw new IllegalArgumentException("Object is not of type TranslationRecord");
            }
        }
    }
}
