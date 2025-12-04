package com.bervan.languageapp.component;

import com.bervan.logging.JsonLogger;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.shared.Registration;

import java.io.ByteArrayInputStream;
import java.util.Base64;

@Tag("Div")
public class AudioPlayer extends Component {
    private static final JsonLogger log = JsonLogger.getLogger(AudioPlayer.class, "learning-language");

    private AudioElement audioElement;

    public AudioPlayer() {
        audioElement = new AudioElement();
        audioElement.getElement().setAttribute("style", "display: none;");
        getElement().appendChild(audioElement.getElement());

        Icon speakerIcon = new Icon(VaadinIcon.VOLUME_UP);
        speakerIcon.getStyle().set("cursor", "pointer");
        speakerIcon.addClickListener(event -> toggleAudio());

        getElement().appendChild(speakerIcon.getElement());
    }

    private static byte[] getDecode(String audioInBase64) {
        try {
            return Base64.getDecoder().decode(audioInBase64);
        } catch (IllegalArgumentException e) {
            log.error("Incorrect base64 audio.");
            return new byte[0];
        }
    }

    public void executeAutoPlay() {
        getElement().executeJs("setTimeout(() => { const audio = this.querySelector('audio'); if (audio) { audio.play(); } }, 200);");
    }

    public void setSource(String audioInBase64) {
        try {
            if (audioInBase64 == null || audioInBase64.isBlank() || audioInBase64.isEmpty()) {
                this.setVisible(false);
                return;
            }

            StreamResource audioResource = new StreamResource(
                    "audio.mp3",
                    () -> new ByteArrayInputStream(getDecode(audioInBase64))
            );

            audioElement.getElement().setAttribute("src", audioResource);
        } catch (Exception ignored) {

        }

    }

    public void toggleAudio() {
        getElement().executeJs("const audio = this.querySelector('audio'); if (audio.paused) { audio.play(); } else { audio.pause(); }");
    }

    public Registration addClickListener(
            ComponentEventListener listener) {
        return this.addListener(ClickEvent.class, listener);
    }

    @Tag("audio")
    private static class AudioElement extends Component {
        public AudioElement() {
        }
    }
}

