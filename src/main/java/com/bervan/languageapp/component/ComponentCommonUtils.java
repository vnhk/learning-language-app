package com.bervan.languageapp.component;

import com.bervan.languageapp.AudioPlayer;
import com.bervan.languageapp.TextToSpeechService;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import io.micrometer.common.util.StringUtils;

import java.nio.file.Path;

public class ComponentCommonUtils {
    public static void addAudio(Span span, String path) {
        AudioPlayer textAudioPlayer = new AudioPlayer();
        textAudioPlayer.setVisible(true);
        textAudioPlayer.setSource(Path.of(TextToSpeechService.path, path).toString());
        span.add(new Div());
        span.add(textAudioPlayer);
    }

    public static void addAudioIfExist(Span span, String path) {
        boolean isAvailable = StringUtils.isNotBlank(path);
        if (isAvailable) {
            ComponentCommonUtils.addAudio(span, path);
        }
    }
}
