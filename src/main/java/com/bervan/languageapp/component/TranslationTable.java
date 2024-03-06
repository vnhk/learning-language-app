package com.bervan.languageapp.component;

import com.bervan.languageapp.TranslationRecord;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.SerializableBiConsumer;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static com.bervan.languageapp.component.ComponentCommonUtils.addAudioIfExist;

@Component
public class TranslationTable extends VerticalLayout {
    private final Grid<TranslationRecord> grid = new Grid<>();
    @Getter
    private final List<Button> deleteButtons = new ArrayList<>();
    private Map<UUID, String> recordsWithSearchValues;
    private List<TranslationRecord> data;

    public TranslationTable(List<TranslationRecord> data) {
        refresh(data);
        HorizontalLayout searchLayout = new HorizontalLayout();
        Button searchButton = new Button("Search");
        Button resetButton = new Button("Reset");

        TextField searchField = new TextField();
        searchField.addValueChangeListener(event -> search(data, event.getValue()));
        searchButton.addClickListener(event -> search(data, searchField.getValue()));

        resetButton.addClickListener(buttonClickEvent -> {
            grid.setItems(this.data);
            searchField.setValue("");
        });

        searchLayout.add(searchField);
        searchLayout.add(searchButton);
        searchLayout.add(resetButton);
        createButtons(data);

        grid.addColumn(createSourceTextWithSoundComponent()).setHeader("Text");
        grid.addColumn(TranslationRecord::getTextTranslation).setHeader("Translation");
        grid.addColumn(createInSentenceTextWithSoundComponent()).setHeader("In Sentence");
        grid.addColumn(TranslationRecord::getInSentenceTranslation).setHeader("In Sentence Translation");
        grid.addColumn(createActionsComponent()).setHeader("Actions");

        grid.addClassName("my-grid");
        grid.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        grid.getThemeNames().add("no-border");

        grid.getColumns().forEach(column -> column.setHeader(new Span(column.getHeaderText())));

        add(searchLayout);
        add(grid);
    }

    private void search(List<TranslationRecord> data, String value) {
        if (value.length() > 2) {
            Set<UUID> matches = this.recordsWithSearchValues.entrySet().stream()
                    .filter(e -> e.getValue().contains(value.toLowerCase()))
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet());

            Set<TranslationRecord> filteredValue = data.stream().filter(e -> matches.contains(e.getUuid()))
                    .collect(Collectors.toSet());
            grid.setItems(filteredValue);
        }
    }

    private Map<UUID, String> buildRecordsWithSearchValues(List<TranslationRecord> data) {
        Map<UUID, String> recordWithSearchValues = new HashMap<>();
        for (TranslationRecord d : data) {
            String search = Arrays.asList(d.getSourceText(), d.getTextTranslation(), d.getInSentence(), d.getInSentenceTranslation()).toString();
            recordWithSearchValues.put(d.getUuid(), search.toLowerCase());
        }

        return recordWithSearchValues;
    }

    private void createButtons(List<TranslationRecord> data) {
        for (TranslationRecord translationRecord : data) {
            Button button = new Button("Delete");
            button.addClassName("delete-button");
            button.getElement().setAttribute("uuid", String.valueOf(translationRecord.getUuid()));
            deleteButtons.add(button);
        }
    }

    private final SerializableBiConsumer<Span, TranslationRecord> sourceTextUpdater = (
            span, translationRecord) -> {
        span.add(translationRecord.getSourceText());
        addAudioIfExist(span, translationRecord.getTextSound());
    };

    private final SerializableBiConsumer<Span, TranslationRecord> inSentenceTextUpdater = (
            span, translationRecord) -> {
        span.add(translationRecord.getInSentence());
        addAudioIfExist(span, translationRecord.getInSentenceSound());
    };

    private final SerializableBiConsumer<Span, TranslationRecord> actionsComponentUpdater = (
            span, translationRecord) -> {
        for (Button deleteButton : getDeleteButtons()) {
            if (translationRecord.getUuid().equals(UUID.fromString(deleteButton.getElement().getAttribute("uuid")))) {
                span.add(deleteButton);
                break;
            }
        }
    };


    private ComponentRenderer<Span, TranslationRecord> createSourceTextWithSoundComponent() {
        return new ComponentRenderer<>(Span::new, sourceTextUpdater);
    }

    private ComponentRenderer<Span, TranslationRecord> createInSentenceTextWithSoundComponent() {
        return new ComponentRenderer<>(Span::new, inSentenceTextUpdater);
    }

    private ComponentRenderer<Span, TranslationRecord> createActionsComponent() {
        return new ComponentRenderer<>(Span::new, actionsComponentUpdater);
    }

    public void refresh(List<TranslationRecord> data) {
        this.data = data;
        grid.setItems(data);
        this.recordsWithSearchValues = buildRecordsWithSearchValues(data);
    }
}