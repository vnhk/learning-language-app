package com.bervan.languageapp.component;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.shared.Registration;

import java.io.ByteArrayInputStream;
import java.util.Base64;

@Tag("audio")
public class AudioPlayer extends Component {

    public AudioPlayer() {
        this.getStyle().setWidth("15px");
        getElement().setAttribute("controls", true);
    }

    public Registration addClickListener(
            ComponentEventListener listener) {
        return this.addListener(ClickEvent.class, listener);
    }

    public void setSource(String audioInBase64) {
        StreamResource audioResource = new StreamResource("audio.mp3",
                () -> new ByteArrayInputStream(Base64.getDecoder().decode(audioInBase64)));

        getElement().setAttribute("src", audioResource);
    }
}

