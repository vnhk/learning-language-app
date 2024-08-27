package com.bervan.languageapp;

import com.bervan.history.model.BaseRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TranslationRecordRepository extends BaseRepository<TranslationRecord, UUID> {
    Set<TranslationRecord> findAllByDeletedIsFalseOrDeletedIsNull();

    Set<TranslationRecord> findAllByDeletedIsFalseOrDeletedIsNullAndNextRepeatTimeNullOrNextRepeatTimeBefore(LocalDateTime dateTime);
}
