package com.bervan.languageapp.component;

import com.bervan.languageapp.TranslationRecord;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class Flashcard extends VerticalLayout {
    private AudioPlayer sourcePlayer;
    private AudioPlayer inSentencePlayer;
    private Boolean isAnswerVisible = Boolean.FALSE;

    public Flashcard(TranslationRecord translationRecord, Div buttonsLayout) {
        sourcePlayer = new AudioPlayer();
        inSentencePlayer = new AudioPlayer();

        this.addClassName("flashcard-layout");
        Div flashcardDiv = new Div();
        flashcardDiv.addClassName("flashcard");

        Span sourceTextLabel = new Span(translationRecord.getSourceText());
        Span inSentenceLabel = new Span(translationRecord.getInSentence());

        Div questionDiv = new Div();

        if (translationRecord.getTextSound() != null) {
            sourcePlayer.setSource(translationRecord.getTextSound());
            questionDiv.add(sourceTextLabel, sourcePlayer);
            sourcePlayer.getElement().executeJs(
                    "this.addEventListener('click', function(event) { event.stopPropagation(); });"
            );
        } else {
            questionDiv.add(sourceTextLabel);
        }

        Div inSentenceDiv = new Div();

        if (translationRecord.getInSentence() != null) {
            questionDiv.add(new Hr());
            if (translationRecord.getInSentenceSound() != null) {
                inSentencePlayer.setSource(translationRecord.getInSentenceSound());
                inSentenceDiv.add(inSentenceLabel, inSentencePlayer);
                inSentencePlayer.getElement().executeJs(
                        "this.addEventListener('click', function(event) { event.stopPropagation(); });"
                );
            } else {
                inSentenceDiv.add(inSentenceLabel);
            }
        }

        flashcardDiv.add(questionDiv, inSentenceDiv);

        flashcardDiv.addClickListener(event -> {
            if (!isAnswerVisible) {
                sourceTextLabel.setText(translationRecord.getTextTranslation());
                inSentenceLabel.setText(translationRecord.getInSentenceTranslation());
                buttonsLayout.setVisible(true);
                sourcePlayer.setVisible(false);
                inSentencePlayer.setVisible(false);
                isAnswerVisible = true;
            } else {
                sourceTextLabel.setText(translationRecord.getSourceText());
                inSentenceLabel.setText(translationRecord.getInSentence());
                buttonsLayout.setVisible(false);
                sourcePlayer.setVisible(true);
                inSentencePlayer.setVisible(true);
                isAnswerVisible = false;
            }
        });

        add(flashcardDiv, buttonsLayout);
    }
}
