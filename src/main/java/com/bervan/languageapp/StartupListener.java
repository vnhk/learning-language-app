package com.bervan.languageapp;


import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {

//    @Autowired
//    private TranslationRecordService translationRecordService;

//    @Autowired
//    private TranslationRecordFileRepository translationRecordFileRepository;

    @Override
    @Transactional
    public void onApplicationEvent(ContextRefreshedEvent event) {
//        translationRecordFileRepository.loadAll();
//        List<TranslationRecord> all =
//                translationRecordFileRepository.getAll();
//
//        for (TranslationRecord translationRecord : all) {
//            if (translationRecord.getTextPronunciationPath() != null && !translationRecord.getTextPronunciationPath().equals("")) {
//                AudioPlayer a1 = new AudioPlayer();
//                FileInputStream s1 = a1.getSource(Path.of(TextToSpeechService.path, translationRecord.getTextPronunciationPath()).toString());
//                try {
//                    byte[] bytes = s1.readAllBytes();
//                    String s = Base64.getEncoder().encodeToString(bytes);
//                    translationRecord.setTextSound(s);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//            if (translationRecord.getInSentencePronunciationPath() != null && !translationRecord.getInSentencePronunciationPath().equals("")) {
//                AudioPlayer a2 = new AudioPlayer();
//                FileInputStream s2 = a2.getSource(Path.of(TextToSpeechService.path, translationRecord.getInSentencePronunciationPath()).toString());
//                try {
//                    byte[] bytes = s2.readAllBytes();
//                    String s = Base64.getEncoder().encodeToString(bytes);
//                    translationRecord.setInSentenceSound(s);
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//            translationRecordService.add(translationRecord);
//        }
    }
}