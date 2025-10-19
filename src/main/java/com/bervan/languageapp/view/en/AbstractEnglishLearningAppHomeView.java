package com.bervan.languageapp.view.en;

import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.view.AbstractLearningAppHomeView;

public abstract class AbstractEnglishLearningAppHomeView extends AbstractLearningAppHomeView {
    public static final String ROUTE_NAME = "learning-language-app/en/home";

    public AbstractEnglishLearningAppHomeView(TranslationRecordService translationRecordsService) {
        super(translationRecordsService,
                AbstractEnglishLearningTableView.ROUTE_NAME,
                AbstractEnglishLearningView.ROUTE_NAME,
                AbstractEnglishQuizView.ROUTE_NAME,
                AbstractEnglishFastImportView.ROUTE_NAME,
                AbstractEnglishImportExportView.ROUTE_NAME, "EN");
    }
}
