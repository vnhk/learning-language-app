package com.bervan.languageapp;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.server.AbstractStreamResource;
import com.vaadin.flow.server.StreamResource;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

@Tag("audio")
@Slf4j
public class AudioPlayer extends Component {

    public AudioPlayer() {
        this.getStyle().setWidth("15px");
        getElement().setAttribute("controls", true);
    }

    public void setSource(String path) {
        try {
            AbstractStreamResource resource = new StreamResource(path.replace("/", "_"), () -> {
                try {
                    return new FileInputStream(path);
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                }
                return null;
            });
            getElement().setAttribute("src", resource);


        } catch (Exception e) {
            log.error("Failed to load audio tag!");
        }
    }
}

