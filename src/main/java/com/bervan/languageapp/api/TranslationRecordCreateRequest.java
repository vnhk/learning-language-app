package com.bervan.languageapp.api;

import com.bervan.core.model.BaseDTO;
import com.bervan.core.model.BaseModel;
import com.bervan.languageapp.TranslationRecord;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class TranslationRecordCreateRequest implements BaseDTO<UUID> {
    private UUID id;
    private String sourceText;
    private String level;
    private String textTranslation;
    private String inSentence;
    private String inSentenceTranslation;
    private String language;
    private boolean markedForLearning = true;
    private Integer factor = 1;

    @Override
    public Class<? extends BaseModel<UUID>> dtoTarget() {
        return TranslationRecord.class;
    }
}
