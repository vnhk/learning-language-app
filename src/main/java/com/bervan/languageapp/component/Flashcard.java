package com.bervan.languageapp.component;

import com.bervan.common.component.BervanImageViewer;
import com.bervan.languageapp.TranslationRecord;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class Flashcard extends VerticalLayout {
    private AudioPlayer cardTopPlayer;
    private AudioPlayer cardBottomPlayer;
    private Boolean isAnswerVisible = Boolean.FALSE;
    private Div buttonsLayout;
    private Div flashcardDiv;
    private Div additionalDetailsDiv;
    private Div flashcardContainer;

    public Flashcard(TranslationRecord translationRecord, Div buttonsLayout, boolean isReversed) {
        this.cardTopPlayer = new AudioPlayer();
        this.cardBottomPlayer = new AudioPlayer();
        this.buttonsLayout = buttonsLayout;

        this.addClassName("flashcard-layout");

        // Main flashcard container with flip animation support
        flashcardContainer = new Div();
        flashcardContainer.addClassName("flashcard-container");

        flashcardDiv = new Div();
        additionalDetailsDiv = getAdditionalDetailsDiv(translationRecord);
        additionalDetailsDiv.setVisible(false);
        flashcardDiv.addClassName("flashcard");
        additionalDetailsDiv.addClassName("flashcard-additional-details");

        if (translationRecord.getLevel() != null && !translationRecord.getLevel().isBlank()) {
            flashcardDiv.addClassName(translationRecord.getLevel().toLowerCase() + "-level");
        }

        if (isReversed) {
            reversedCard(translationRecord);
        } else {
            normalCard(translationRecord);
        }

        // Layout: card on top, images below
        VerticalLayout cardWithImages = new VerticalLayout();
        cardWithImages.setPadding(false);
        cardWithImages.setSpacing(true);
        cardWithImages.addClassName("flashcard-card-with-images");

        flashcardContainer.add(flashcardDiv);
        cardWithImages.add(flashcardContainer, additionalDetailsDiv);

        add(buttonsLayout, cardWithImages);

        // Add keyboard shortcuts info
        Div shortcutsInfo = new Div();
        shortcutsInfo.addClassName("flashcard-shortcuts-info");
        shortcutsInfo.getElement().setProperty("innerHTML",
            "<span class='shortcut-key'>Space</span> flip &nbsp; " +
            "<span class='shortcut-key'>Q</span> again &nbsp; " +
            "<span class='shortcut-key'>W</span> hard &nbsp; " +
            "<span class='shortcut-key'>E</span> good &nbsp; " +
            "<span class='shortcut-key'>R</span> easy &nbsp; " +
            "<span class='shortcut-key'>P</span> play");
        add(shortcutsInfo);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        // Register keyboard listener
        getElement().executeJs(
                "window.flashcardListener = function(event) {" +
                        "  if (event.target.tagName === 'INPUT' || event.target.tagName === 'TEXTAREA') return;" +
                        "  if (event.key === ' ' || event.key === 'Spacebar') { event.preventDefault(); $0.$server.flashcardClick(); }" +
                        "  else if (event.key === 'q' || event.key === 'Q') { $0.$server.againButtonClick(); }" +
                        "  else if (event.key === 'w' || event.key === 'W') { $0.$server.hardButtonClick(); }" +
                        "  else if (event.key === 'e' || event.key === 'E') { $0.$server.goodButtonClick(); }" +
                        "  else if (event.key === 'r' || event.key === 'R') { $0.$server.easyButtonClick(); }" +
                        "  else if (event.key === 'p' || event.key === 'P') { $0.$server.playSound(); }" +
                        "};" +
                        "document.addEventListener('keydown', window.flashcardListener);",
                getElement()
        );
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        // Cleanup keyboard listener
        getElement().executeJs(
                "if (window.flashcardListener) {" +
                        "  document.removeEventListener('keydown', window.flashcardListener);" +
                        "  window.flashcardListener = null;" +
                        "}"
        );
    }

    private Div getAdditionalDetailsDiv(TranslationRecord translationRecord) {
        Div div = new Div();
        if (!translationRecord.getImages().isEmpty()) {
            BervanImageViewer bervanImageViewer = new BervanImageViewer(translationRecord.getImages());
            bervanImageViewer.setImageViewerSize("350px", "400px");
            div.add(bervanImageViewer);
        }
        return div;
    }

    @ClientCallable
    public void flashcardClick() {
        if (flashcardDiv.isVisible()) {
            flashcardDiv.getElement().executeJs("this.click();");
        }
    }

    @ClientCallable
    public void playSound() {
        if (flashcardDiv.isVisible() && cardTopPlayer != null) {
            cardTopPlayer.executeAutoPlay();
        }
    }

    @ClientCallable
    public void easyButtonClick() {
        if (buttonsLayout.isVisible()) {
            buttonsLayout.getChildren().filter(e -> e instanceof Button)
                    .filter(e -> ((Button) e).getText().toUpperCase().contains("EASY"))
                    .forEach(e -> ((Button) e).click());
        }
    }

    @ClientCallable
    public void goodButtonClick() {
        if (buttonsLayout.isVisible()) {
            buttonsLayout.getChildren().filter(e -> e instanceof Button)
                    .filter(e -> ((Button) e).getText().toUpperCase().contains("GOOD"))
                    .forEach(e -> ((Button) e).click());
        }
    }

    @ClientCallable
    public void hardButtonClick() {
        if (buttonsLayout.isVisible()) {
            buttonsLayout.getChildren().filter(e -> e instanceof Button)
                    .filter(e -> ((Button) e).getText().toUpperCase().contains("HARD"))
                    .forEach(e -> ((Button) e).click());
        }
    }

    @ClientCallable
    public void againButtonClick() {
        if (buttonsLayout.isVisible()) {
            buttonsLayout.getChildren().filter(e -> e instanceof Button)
                    .filter(e -> ((Button) e).getText().toUpperCase().contains("AGAIN"))
                    .forEach(e -> ((Button) e).click());
        }
    }

    private void reversedCard(TranslationRecord translationRecord) {
        Span textTop = new Span(translationRecord.getTextTranslation());
        Span textBottom = new Span(translationRecord.getInSentenceTranslation());

        Div questionDiv = new Div();

        if (translationRecord.getTextSound() != null) {
            cardTopPlayer.setSource(translationRecord.getTextSound());
            cardTopPlayer.executeAutoPlay();
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
                flashcardContainer.addClassName("flipped");
                textTop.setText(translationRecord.getSourceText());
                textBottom.setText(translationRecord.getInSentence());
                cardTopPlayer.setVisible(true);
                cardBottomPlayer.setVisible(true);

                buttonsLayout.setVisible(true);
                additionalDetailsDiv.setVisible(true);
                isAnswerVisible = true;
            } else {
                flashcardContainer.removeClassName("flipped");
                textTop.setText(translationRecord.getTextTranslation());
                textBottom.setText(translationRecord.getInSentenceTranslation());
                cardTopPlayer.setVisible(false);
                cardBottomPlayer.setVisible(false);

                buttonsLayout.setVisible(false);
                additionalDetailsDiv.setVisible(false);
                isAnswerVisible = false;
            }
        });
    }

    private void normalCard(TranslationRecord translationRecord) {
        Span textTop = new Span(translationRecord.getSourceText());
        Span textBottom = new Span(translationRecord.getInSentence());

        Div questionDiv = new Div();

        if (translationRecord.getTextSound() != null) {
            cardTopPlayer.setSource(translationRecord.getTextSound());
            questionDiv.add(textTop, cardTopPlayer);
            cardTopPlayer.executeAutoPlay();
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
                flashcardContainer.addClassName("flipped");
                textTop.setText(translationRecord.getTextTranslation());
                textBottom.setText(translationRecord.getInSentenceTranslation());
                cardTopPlayer.setVisible(false);
                cardBottomPlayer.setVisible(false);

                buttonsLayout.setVisible(true);
                isAnswerVisible = true;
                additionalDetailsDiv.setVisible(true);
            } else {
                flashcardContainer.removeClassName("flipped");
                textTop.setText(translationRecord.getSourceText());
                textBottom.setText(translationRecord.getInSentence());
                cardTopPlayer.setVisible(true);
                cardBottomPlayer.setVisible(true);

                buttonsLayout.setVisible(false);
                isAnswerVisible = false;
                additionalDetailsDiv.setVisible(false);
            }
        });
    }
}
