package com.bervan.languageapp;

import com.bervan.history.model.AbstractBaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
public class TranslationRecord implements AbstractBaseEntity<UUID> {
    @Id
    @GeneratedValue
    private UUID id;
    private Boolean deleted;
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
    private Integer factor = 1;
    private LocalDateTime nextRepeatTime;

    public LocalDateTime getNextRepeatTime() {
        return nextRepeatTime;
    }

    public void setNextRepeatTime(LocalDateTime nextRepeatTime) {
        this.nextRepeatTime = nextRepeatTime;
    }

    public TranslationRecord() {
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public String getSourceText() {
        return sourceText;
    }

    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
    }

    public String getTextTranslation() {
        return textTranslation;
    }

    public void setTextTranslation(String textTranslation) {
        this.textTranslation = textTranslation;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInSentence() {
        return inSentence;
    }

    public void setInSentence(String inSentence) {
        this.inSentence = inSentence;
    }

    public String getInSentenceTranslation() {
        return inSentenceTranslation;
    }

    public void setInSentenceTranslation(String inSentenceTranslation) {
        this.inSentenceTranslation = inSentenceTranslation;
    }

    public String getTextSound() {
        return textSound;
    }

    public void setTextSound(String textSound) {
        this.textSound = textSound;
    }

    public String getInSentenceSound() {
        return inSentenceSound;
    }

    public void setInSentenceSound(String inSentenceSound) {
        this.inSentenceSound = inSentenceSound;
    }

    public TranslationRecord(TranslationRecord old) {
        this.sourceText = old.getSourceText();
        this.textTranslation = old.getTextTranslation();
        this.type = old.getType();
        this.inSentence = old.getInSentence();
        this.inSentenceTranslation = old.getInSentenceTranslation();
        this.id = old.getId();
        this.deleted = old.getDeleted();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TranslationRecord that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(deleted, that.deleted) && Objects.equals(sourceText, that.sourceText) && Objects.equals(textTranslation, that.textTranslation) && Objects.equals(type, that.type) && Objects.equals(inSentence, that.inSentence) && Objects.equals(inSentenceTranslation, that.inSentenceTranslation) && Objects.equals(textSound, that.textSound) && Objects.equals(inSentenceSound, that.inSentenceSound);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deleted, sourceText, textTranslation, type, inSentence, inSentenceTranslation, textSound, inSentenceSound);
    }

    @Override
    public LocalDateTime getModificationDate() {
        return null;
    }

    @Override
    public void setModificationDate(LocalDateTime modificationDate) {

    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getFactor() {
        return factor;
    }

    public void setFactor(Integer factor) {
        this.factor = factor;
    }


    public static final class TranslationRecordBuilder {
        private UUID id;
        private Boolean deleted;
        private @Size(max = 2000) String sourceText;
        private @Size(max = 2000) String textTranslation;
        private String type;
        private @Size(max = 2000) String inSentence;
        private @Size(max = 2000) String inSentenceTranslation;
        private @Size(max = 1000000000) String textSound;
        private @Size(max = 1000000000) String inSentenceSound;
        private LocalDateTime nextRepeatTime;

        public TranslationRecordBuilder() {
        }

        public TranslationRecordBuilder(TranslationRecord other) {
            this.id = other.id;
            this.deleted = other.deleted;
            this.sourceText = other.sourceText;
            this.textTranslation = other.textTranslation;
            this.type = other.type;
            this.inSentence = other.inSentence;
            this.inSentenceTranslation = other.inSentenceTranslation;
            this.textSound = other.textSound;
            this.inSentenceSound = other.inSentenceSound;
            this.nextRepeatTime = other.nextRepeatTime;
        }

        public static TranslationRecordBuilder aTranslationRecord() {
            return new TranslationRecordBuilder();
        }

        public TranslationRecordBuilder withId(UUID id) {
            this.id = id;
            return this;
        }

        public TranslationRecordBuilder withDeleted(Boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public TranslationRecordBuilder withSourceText(String sourceText) {
            this.sourceText = sourceText;
            return this;
        }

        public TranslationRecordBuilder withTextTranslation(String textTranslation) {
            this.textTranslation = textTranslation;
            return this;
        }

        public TranslationRecordBuilder withType(String type) {
            this.type = type;
            return this;
        }

        public TranslationRecordBuilder withInSentence(String inSentence) {
            this.inSentence = inSentence;
            return this;
        }

        public TranslationRecordBuilder withInSentenceTranslation(String inSentenceTranslation) {
            this.inSentenceTranslation = inSentenceTranslation;
            return this;
        }

        public TranslationRecordBuilder withTextSound(String textSound) {
            this.textSound = textSound;
            return this;
        }

        public TranslationRecordBuilder withInSentenceSound(String inSentenceSound) {
            this.inSentenceSound = inSentenceSound;
            return this;
        }

        public TranslationRecordBuilder withNextRepeatTime(LocalDateTime nextRepeatTime) {
            this.nextRepeatTime = nextRepeatTime;
            return this;
        }

        public TranslationRecord build() {
            TranslationRecord translationRecord = new TranslationRecord();
            translationRecord.setId(id);
            translationRecord.setDeleted(deleted);
            translationRecord.setSourceText(sourceText);
            translationRecord.setTextTranslation(textTranslation);
            translationRecord.setType(type);
            translationRecord.setInSentence(inSentence);
            translationRecord.setInSentenceTranslation(inSentenceTranslation);
            translationRecord.setTextSound(textSound);
            translationRecord.setInSentenceSound(inSentenceSound);
            translationRecord.nextRepeatTime = this.nextRepeatTime;
            return translationRecord;
        }
    }
}
