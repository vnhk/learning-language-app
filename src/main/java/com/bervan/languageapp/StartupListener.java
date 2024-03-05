package com.bervan.languageapp;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private TranslationRecordFileRepository translationRecordFileRepository;

    @Autowired
    private TextToSpeechService textToSpeechService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        translationRecordFileRepository.loadAll();
        textToSpeechService.deleteNotUsedAudio(translationRecordFileRepository.getAll());
    }
}