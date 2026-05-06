package com.bervan.languageapp.api;

import com.bervan.common.service.ApiKeyService;
import com.bervan.languageapp.TranslationRecord;
import com.bervan.languageapp.service.ExampleOfUsageService;
import com.bervan.languageapp.service.TextToSpeechService;
import com.bervan.languageapp.service.TranslationRecordService;
import com.bervan.languageapp.service.TranslatorService;
import com.bervan.logging.JsonLogger;
import com.google.common.base.Strings;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@PermitAll
public class LanguageLearningExternalController {
    private final JsonLogger log = JsonLogger.getLogger(getClass(), "my-tools-app");
    private final TranslationRecordService translationRecordService;
    private final ExampleOfUsageService exampleOfUsageService;
    private final TextToSpeechService textToSpeechService;
    private final TranslatorService translationService;
    private final ApiKeyService apiKeyService;

    @Value("${api.keys}")
    private List<String> API_KEYS = new ArrayList<>();

    public LanguageLearningExternalController(TranslationRecordService translationRecordService,
                                              ExampleOfUsageService exampleOfUsageService,
                                              TextToSpeechService textToSpeechService,
                                              TranslatorService translationService,
                                              ApiKeyService apiKeyService) {
        this.translationRecordService = translationRecordService;
        this.exampleOfUsageService = exampleOfUsageService;
        this.textToSpeechService = textToSpeechService;
        this.translationService = translationService;
        this.apiKeyService = apiKeyService;
    }

    @PostMapping(path = "/language-learning/translation")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> addTranslation(@RequestBody ExternalTranslationRequest request) {
        if (!this.API_KEYS.contains(request.getApiKey())) {
            throw new RuntimeException("INVALID ACCESS");
        }

        if (request.getLevel() == null) {
            request.setLevel("N/A");
        }

        switch (request.getLevel()) {
            case "A1", "A2", "B1", "B2", "C1", "C2":
                break;
            default:
                request.setLevel("N/A");
        }

        try {
            TranslationRecord record = new TranslationRecord();
            record.setDeleted(false);
            record.setFactor(1);
            record.setLanguage("EN");
            record.setSourceText(request.getEnglishText());
            record.setLevel(request.getLevel());
            record.setTextTranslation(request.getPolishText());
            record.addOwner(apiKeyService.getUserByAPIKey(request.getApiKey()));

            if (Boolean.TRUE.equals(request.getGenerateExample())) {
                List<String> exampleOfUsage = exampleOfUsageService.createExampleOfUsage(request.getEnglishText(), "EN");
                String examples = exampleOfUsage.toString();
                if (!examples.isBlank() && !exampleOfUsage.isEmpty()) {
                    if (examples.length() > 500) {
                        StringBuilder builder = new StringBuilder();
                        for (String s : exampleOfUsage) {
                            if (builder.length() + s.length() + 1 > 500) break;
                            builder.append(s);
                            builder.append(",");
                        }
                        examples = builder.substring(0, builder.length() - 2);
                    }
                    record.setInSentence(examples);
                    record.setInSentenceTranslation(translationService.translate(examples, "EN"));
                }
            }

            if (Boolean.TRUE.equals(request.getSaveWithSound())) {
                record.setTextSound(textToSpeechService.getTextSpeech(request.getEnglishText(), "EN"));
                if (!Strings.isNullOrEmpty(record.getInSentence())) {
                    record.setInSentenceSound(textToSpeechService.getTextSpeech(record.getInSentence(), "EN"));
                }
            }

            if (Boolean.TRUE.equals(request.getLoadNewImages())) {
                translationRecordService.setNewAndReplaceImages(record);
            }

            TranslationRecord saved = translationRecordService.save(record);
            return ResponseEntity.ok(saved.getId() + " saved.");
        } catch (Exception e) {
            log.error("Unable to save new translation item!", e);
            return ResponseEntity.badRequest().body("Unable to save new translation item!");
        }
    }

    @PostMapping(path = "/language-learning/translate")
    @CrossOrigin(origins = "*")
    public ResponseEntity<String> translate(@RequestBody ExternalTranslationRequest request) {
        if (!this.API_KEYS.contains(request.getApiKey())) {
            throw new RuntimeException("INVALID ACCESS");
        }
        return new ResponseEntity<>(translationService.translate(request.getEnglishText(), "EN"), HttpStatus.OK);
    }
}
