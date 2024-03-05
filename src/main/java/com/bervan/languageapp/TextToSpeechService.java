package com.bervan.languageapp;

import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class TextToSpeechService {
    public static String path = "src/main/resources/db";

    public void deleteNotUsedAudio(List<TranslationRecord> all) {
        try (Stream<Path> paths = Files.walk(Paths.get(path))) {
            Set<Path> allFiles = paths.collect(Collectors.toSet());
            Set<Path> mp3s = allFiles.stream()
                    .filter(e -> e.toAbsolutePath().toString().endsWith(".mp3"))
                    .collect(Collectors.toSet());

            Set<String> mp3FileNames = mp3s.stream().map(Path::toString).map(e -> e.replace(path, "").replace("/", "")).collect(Collectors.toSet());

            Set<Path> toNOTBeDeleted = new HashSet<>();
            for (TranslationRecord translationRecord : all) {
                if (mp3FileNames.contains(translationRecord.getTextPronunciationPath())) {
                    toNOTBeDeleted.add(Path.of(path, translationRecord.getTextPronunciationPath()));
                }

                if (mp3FileNames.contains(translationRecord.getInSentencePronunciationPath())) {
                    toNOTBeDeleted.add(Path.of(path, translationRecord.getInSentencePronunciationPath()));
                }
            }

            mp3s.removeAll(toNOTBeDeleted);
            for (Path mp3 : mp3s) {
//                mp3.toFile().delete();
            }

        } catch (Exception e) {
            log.error("Failed to remove audios!", e);
            throw new RuntimeException("Failed to remove audios!");
        }
    }

    public Path getTextSpeech(String text) {
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
            // Set the text input to be synthesized
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

            // Build the voice request, select the language code ("en-US") and the ssml voice gender
            // ("neutral")
            VoiceSelectionParams voice =
                    VoiceSelectionParams.newBuilder()
                            .setLanguageCode("en-US")
                            .setSsmlGender(SsmlVoiceGender.FEMALE)
                            .build();

            // Select the type of audio file you want returned
            AudioConfig audioConfig =
                    AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).build();

            // Perform the text-to-speech request on the text input with the selected voice parameters and
            // audio file type
            SynthesizeSpeechResponse response =
                    textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            // Get the audio contents from the response
            ByteString audioContents = response.getAudioContent();

            // Write the response to the output file.
            String fileName = UUID.randomUUID() + ".mp3";
            Path path = Path.of(this.path, fileName);
            try (OutputStream out = new FileOutputStream(path.toFile())) {
                out.write(audioContents.toByteArray());
                log.info("Audio content written to file: " + path);

                return Path.of(fileName);
            } catch (Exception e) {
                log.error("Failed to save audio!", e);
                throw new RuntimeException("Failed to save audio!");
            }
        } catch (IOException e) {
            log.error("Failed to load and save audio!", e);
            throw new RuntimeException("Failed to load and save audio!");
        }
    }
}
