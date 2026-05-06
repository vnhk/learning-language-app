package com.bervan.languageapp.api;

import com.bervan.core.model.BaseDTO;
import com.bervan.core.model.BaseModel;
import com.bervan.languageapp.TranslationRecord;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TranslationRecordDto implements BaseDTO<UUID> {
    private UUID id;
    private String sourceText;
    private String level;
    private String textTranslation;
    private String type;
    private String inSentence;
    private String inSentenceTranslation;
    private Integer factor;
    private LocalDateTime nextRepeatTime;
    private boolean markedForLearning;
    private String language;

    @Override
    public Class<? extends BaseModel<UUID>> dtoTarget() {
        return TranslationRecord.class;
    }
}
