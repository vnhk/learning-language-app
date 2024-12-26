package com.bervan.languageapp.component;

import com.bervan.languageapp.TranslationRecord;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class Flashcard extends VerticalLayout {
    private AudioPlayer cardTopPlayer;
    private AudioPlayer cardBottomPlayer;
    private Boolean isAnswerVisible = Boolean.FALSE;

    public Flashcard(TranslationRecord translationRecord, Div buttonsLayout, boolean isReversed) {
        cardTopPlayer = new AudioPlayer();
        cardBottomPlayer = new AudioPlayer();

        this.addClassName("flashcard-layout");
        Div flashcardDiv = new Div();
        flashcardDiv.addClassName("flashcard");

        if (translationRecord.getLevel() != null && !translationRecord.getLevel().isBlank()) {
            flashcardDiv.addClassName(translationRecord.getLevel().toLowerCase() + "-level");
        }

        if (isReversed) {
            reversedCard(translationRecord, buttonsLayout, flashcardDiv);
        } else {
            normalCard(translationRecord, buttonsLayout, flashcardDiv);
        }

        add(flashcardDiv, buttonsLayout);
    }

    private void reversedCard(TranslationRecord translationRecord, Div buttonsLayout, Div flashcardDiv) {
        Span textTop = new Span(translationRecord.getTextTranslation());
        Span textBottom = new Span(translationRecord.getInSentenceTranslation());

        Div questionDiv = new Div();

        if (translationRecord.getTextSound() != null) {
            cardTopPlayer.setSource(translationRecord.getTextSound());
            questionDiv.add(textTop, cardTopPlayer);
            cardTopPlayer.getElement().executeJs("this.addEventListener('click', function(event) { event.stopPropagation(); });");
        } else {
            questionDiv.add(textTop);
        }

        cardTopPlayer.setVisible(false);
        cardBottomPlayer.setVisible(false);

        Div inSentenceDiv = new Div();

        if (translationRecord.getInSentence() != null) {
            questionDiv.add(new Hr());
            if (translationRecord.getInSentenceSound() != null) {
                cardBottomPlayer.setSource(translationRecord.getInSentenceSound());
                inSentenceDiv.add(textBottom, cardBottomPlayer);
                cardBottomPlayer.getElement().executeJs("this.addEventListener('click', function(event) { event.stopPropagation(); });");
            } else {
                inSentenceDiv.add(textBottom);
            }
        }

        flashcardDiv.add(questionDiv, inSentenceDiv);

        flashcardDiv.addClickListener(event -> {
            if (!isAnswerVisible) {
                textTop.setText(translationRecord.getSourceText());
                textBottom.setText(translationRecord.getInSentence());
                cardTopPlayer.setVisible(true);
                cardBottomPlayer.setVisible(true);

                buttonsLayout.setVisible(true);
                isAnswerVisible = true;
            } else {
                textTop.setText(translationRecord.getTextTranslation());
                textBottom.setText(translationRecord.getInSentenceTranslation());
                cardTopPlayer.setVisible(false);
                cardBottomPlayer.setVisible(false);

                buttonsLayout.setVisible(false);
                isAnswerVisible = false;
            }
        });
    }

    private void normalCard(TranslationRecord translationRecord, Div buttonsLayout, Div flashcardDiv) {
        Span textTop = new Span(translationRecord.getSourceText());
        Span textBottom = new Span(translationRecord.getInSentence());

        Div questionDiv = new Div();

        if (translationRecord.getTextSound() != null) {
            cardTopPlayer.setSource(translationRecord.getTextSound());
            questionDiv.add(textTop, cardTopPlayer);
            cardTopPlayer.getElement().executeJs("this.addEventListener('click', function(event) { event.stopPropagation(); });");
        } else {
            questionDiv.add(textTop);
        }

        Div inSentenceDiv = new Div();

        if (translationRecord.getInSentence() != null) {
            questionDiv.add(new Hr());
            if (translationRecord.getInSentenceSound() != null) {
                cardBottomPlayer.setSource(translationRecord.getInSentenceSound());
                inSentenceDiv.add(textBottom, cardBottomPlayer);
                cardBottomPlayer.getElement().executeJs("this.addEventListener('click', function(event) { event.stopPropagation(); });");
            } else {
                inSentenceDiv.add(textBottom);
            }
        }

        flashcardDiv.add(questionDiv, inSentenceDiv);

        flashcardDiv.addClickListener(event -> {
            if (!isAnswerVisible) {
                textTop.setText(translationRecord.getTextTranslation());
                textBottom.setText(translationRecord.getInSentenceTranslation());
                cardTopPlayer.setVisible(false);
                cardBottomPlayer.setVisible(false);

                buttonsLayout.setVisible(true);
                isAnswerVisible = true;
            } else {
                textTop.setText(translationRecord.getSourceText());
                textBottom.setText(translationRecord.getInSentence());
                cardTopPlayer.setVisible(true);
                cardBottomPlayer.setVisible(true);

                buttonsLayout.setVisible(false);
                isAnswerVisible = false;
            }
        });
    }
}
