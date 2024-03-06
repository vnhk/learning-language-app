package com.bervan.languageapp;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.server.StreamResource;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.util.Base64;

@Tag("audio")
@Slf4j
public class AudioPlayer extends Component {

    public AudioPlayer() {
        this.getStyle().setWidth("15px");
        getElement().setAttribute("controls", true);
    }

    public void setSource(String audioInBase64) {
        StreamResource audioResource = new StreamResource("audio.mp3",
                () -> new ByteArrayInputStream(Base64.getDecoder().decode(audioInBase64)));

        getElement().setAttribute("src", audioResource);
    }
}

