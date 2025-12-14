package com.bervan.languageapp;

import com.bervan.common.model.BervanOwnedBaseEntity;
import com.bervan.common.model.PersistableTableOwnedData;
import com.bervan.ieentities.ExcelIEEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class TranslationRecord extends BervanOwnedBaseEntity<UUID> implements PersistableTableOwnedData<UUID>, ExcelIEEntity<UUID> {
    public static final String TranslationRecord_sourceText_columnName = "sourceText";
    public static final String TranslationRecord_markedForLearning_columnName = "markedForLearning";
    public static final String TranslationRecord_textTranslation_columnName = "textTranslation";
    public static final String TranslationRecord_inSentence_columnName = "inSentence";
    public static final String TranslationRecord_level_columnName = "level";
    public static final String TranslationRecord_inSentenceTranslation_columnName = "inSentenceTranslation";
    public static final String TranslationRecord_images_columnName = "images";
    public static final String TranslationRecord_factor_columnName = "factor";
    public static final String TranslationRecord_nextRepeatTime_columnName = "nextRepeatTime";

    @Id
    private UUID id;
    private Boolean deleted;
    @Size(max = 2000)
    private String sourceText;
    @Size(min = 2, max = 3)
    private String level;
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
    private Integer factor;
    private LocalDateTime nextRepeatTime;
    private boolean markedForLearning = true;
    private String language = "EN";
    @ElementCollection(targetClass = String.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "images", joinColumns = @JoinColumn(name = "library_id"))
    @Column(name = "image", nullable = false, length = 1000000000)
    private List<String> images = new ArrayList<>();

    public TranslationRecord() {
    }

    public TranslationRecord(TranslationRecord old) {
        this.sourceText = old.getSourceText();
        this.textTranslation = old.getTextTranslation();
        this.type = old.getType();
        this.level = old.getLevel();
        this.inSentence = old.getInSentence();
        this.inSentenceTranslation = old.getInSentenceTranslation();
        this.id = old.getId();
        this.deleted = old.isDeleted();
    }

    public LocalDateTime getNextRepeatTime() {
        return nextRepeatTime;
    }

    public void setNextRepeatTime(LocalDateTime nextRepeatTime) {
        this.nextRepeatTime = nextRepeatTime;
    }

    public Boolean isDeleted() {
        if (deleted == null) {
            return false;
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TranslationRecord that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(deleted, that.deleted) && Objects.equals(sourceText, that.sourceText) && Objects.equals(level, that.level) && Objects.equals(textTranslation, that.textTranslation) && Objects.equals(type, that.type) && Objects.equals(inSentence, that.inSentence) && Objects.equals(inSentenceTranslation, that.inSentenceTranslation) && Objects.equals(textSound, that.textSound) && Objects.equals(inSentenceSound, that.inSentenceSound);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, deleted, sourceText, textTranslation, level, type, inSentence, inSentenceTranslation, textSound, inSentenceSound);
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

    @Override
    public String getTableFilterableColumnValue() {
        return sourceText;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public void addImage(String image) {
        images.add(image);
    }

    public void removeImage(String image) {
        images.remove(image);
    }

    public boolean isMarkedForLearning() {
        return markedForLearning;
    }

    public void setMarkedForLearning(boolean markedForLearning) {
        this.markedForLearning = markedForLearning;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
