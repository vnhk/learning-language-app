package com.bervan.languageapp.component;

import com.bervan.languageapp.TranslationRecord;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class Flashcard extends VerticalLayout {

    public Flashcard(TranslationRecord translationRecord, HorizontalLayout buttonsLayout) {
        this.addClassName("flashcard-layout");
        Div flashcardDiv = new Div();
        flashcardDiv.addClassName("flashcard");

        Span questionLabel = new Span(translationRecord.getSourceText());
        Span answerLabel = new Span(translationRecord.getTextTranslation());
        answerLabel.setVisible(false);

        flashcardDiv.add(questionLabel, answerLabel);

        flashcardDiv.addClickListener(event -> {
            boolean isAnswerVisible = answerLabel.isVisible();
            if (!isAnswerVisible) {
                showAnswerCard(questionLabel, answerLabel);
                buttonsLayout.setVisible(true);
            }
        });

        add(flashcardDiv, buttonsLayout);
    }

    private void showAnswerCard(Span questionLabel, Span answerLabel) {
        answerLabel.setVisible(true);
        questionLabel.setVisible(false);
    }
}
