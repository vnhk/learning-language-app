package com.bervan.languageapp.component;

import com.bervan.languageapp.AudioPlayer;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

public class ComponentCommonUtils {
    private static void addAudio(Span span, String audioInBase64) {
        AudioPlayer textAudioPlayer = new AudioPlayer();
        textAudioPlayer.setVisible(true);
        textAudioPlayer.setSource(audioInBase64);
        span.add(new Div());
        span.add(textAudioPlayer);
    }

    private static void optimizedAddAudio(Span span, String audioInBase64) {
        AudioPlayer textAudioPlayer = new AudioPlayer();
        textAudioPlayer.setVisible(true);
        textAudioPlayer.addClickListener(componentEvent -> {
            if (textAudioPlayer.getElement().getAttribute("src") == null) {
                textAudioPlayer.setSource(audioInBase64);
            }
        });
        span.add(new Div());
        span.add(textAudioPlayer);
    }

    public static void addAudioIfExist(Span span, String audio) {
        boolean isAvailable = audio != null && audio.length() > 0;
        if (isAvailable) {
            ComponentCommonUtils.addAudio(span, audio);
        }
    }

    public static void optimizedAddAudioIfExist(Span span, String audio) {
        boolean isAvailable = audio != null && audio.length() > 0;
        if (isAvailable) {
            ComponentCommonUtils.optimizedAddAudio(span, audio);
        }
    }
}
