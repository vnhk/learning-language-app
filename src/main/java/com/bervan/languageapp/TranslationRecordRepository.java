package com.bervan.languageapp;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TranslationRecordRepository extends JpaRepository<TranslationRecord, UUID> {
    List<TranslationRecord> findAllByDeletedIsFalseOrDeletedIsNull();
}
