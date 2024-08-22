package com.bervan.languageapp;

import com.bervan.history.model.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TranslationRecordRepository extends BaseRepository<TranslationRecord, UUID> {
    List<TranslationRecord> findAllByDeletedIsFalseOrDeletedIsNull();
}
