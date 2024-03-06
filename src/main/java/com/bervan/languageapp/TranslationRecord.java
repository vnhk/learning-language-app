package com.bervan.languageapp;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Entity
@EqualsAndHashCode
public class TranslationRecord {
    @Id
    @GeneratedValue
    private UUID uuid;
    @Size(max = 2000)
    private String sourceText;
    @Size(max = 2000)
    private String textTranslation;
    private String type;
    @Size(max = 2000)
    private String inSentence;
    @Size(max = 2000)
    private String inSentenceTranslation;
    @Size(max = 1000000000)
    private String textSound;
    @Size(max = 1000000000)
    private String inSentenceSound;
    private String textPronunciationPath;
    private String inSentencePronunciationPath;

    public TranslationRecord(TranslationRecord old) {
        this.sourceText = old.getSourceText();
        this.textTranslation = old.getTextTranslation();
        this.type = old.getType();
        this.inSentence = old.getInSentence();
        this.inSentenceTranslation = old.getInSentenceTranslation();
        this.textPronunciationPath = old.getTextPronunciationPath();
        this.uuid = old.getUuid();
        this.inSentencePronunciationPath = old.getInSentencePronunciationPath();
    }
}
