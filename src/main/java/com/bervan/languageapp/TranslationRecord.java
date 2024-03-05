package com.bervan.languageapp;

import io.micrometer.common.util.StringUtils;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
@EqualsAndHashCode
public class TranslationRecord {
    private UUID uuid = UUID.randomUUID();
    private String sourceText;
    private String textTranslation;
    private String type;
    private String inSentence;
    private String inSentenceTranslation;
    private String textPronunciationPath;
    private String inSentencePronunciationPath;

    public TranslationRecord(TranslationRecord old) {
        this.sourceText = old.getSourceText();
        this.textTranslation = old.getTextTranslation();
        this.type = old.getType();
        this.inSentence = old.getInSentence();
        this.inSentenceTranslation = old.getInSentenceTranslation();
        this.textPronunciationPath = old.getTextPronunciationPath();
        this.uuid = getUuid(old);
        this.inSentencePronunciationPath = old.getInSentencePronunciationPath();
    }

    private static UUID getUuid(TranslationRecord old) {
        if (old.getUuid() != null && StringUtils.isNotBlank(old.getUuid().toString())) {
            return old.getUuid();
        }

        return UUID.randomUUID();
    }
}
