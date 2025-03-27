package com.bervan.languageapp.view;

import com.bervan.languageapp.Cell;
import com.bervan.languageapp.Crossword;
import com.bervan.languageapp.CrosswordWord;
import com.bervan.languageapp.service.CrosswordService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;

@PageTitle("Crossword Puzzle")
public abstract class AbstractCrosswordView extends VerticalLayout {
    public static final String ROUTE_NAME = "learning-english-app/crossword";
    private final CrosswordService crosswordService;
    private Crossword crossword;
    private Div crosswordGrid;
    private TextField[][] cellFields;
    private VerticalLayout cluesLayout;
    private Button generateButton;

    public AbstractCrosswordView(CrosswordService crosswordService) {
        this.crosswordService = crosswordService;
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setPadding(false);
        setSpacing(false);

        generateButton = new Button("Generate New Puzzle", e -> generateCrossword(15, 15));
        add(generateButton);

        generateCrossword(15, 15);
    }

    private void generateCrossword(int width, int height) {
        crossword = crosswordService.generateCrossword(width, height);
        createCrosswordUI();
    }

    private void createCrosswordUI() {
        removeAll();
        add(generateButton);

        // Create main layout
        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setWidth("90%");
        mainLayout.setAlignItems(Alignment.START);
        mainLayout.setSpacing(true);

        // Create crossword grid using CSS Grid
        crosswordGrid = new Div();
        crosswordGrid.setWidth("600px");
        crosswordGrid.setHeight("600px");
        crosswordGrid.getStyle()
                .set("display", "grid")
                .set("grid-template-columns", "repeat(" + crossword.getWidth() + ", 1fr)")
                .set("grid-template-rows", "repeat(" + crossword.getHeight() + ", 1fr)")
                .set("gap", "1px")
                .set("background", "#000")
                .set("border", "2px solid #000");

        cellFields = new TextField[crossword.getWidth()][crossword.getHeight()];

        for (int y = 0; y < crossword.getHeight(); y++) {
            for (int x = 0; x < crossword.getWidth(); x++) {
                Cell cell = crossword.getGrid()[x][y];
                TextField field = new TextField();
                field.getStyle()
                        .set("width", "100%")
                        .set("height", "100%")
                        .set("margin", "0")
                        .set("padding", "0")
                        .set("font-size", "1.2em")
                        .set("text-align", "center")
                        .set("font-weight", "bold");

                Div cellContainer = new Div();
                cellContainer.getStyle()
                        .set("display", "flex")
                        .set("justify-content", "center")
                        .set("align-items", "center")
                        .set("position", "relative")
                        .set("background", "#000");

                if (cell.isEmpty()) {
                    field.getStyle().set("background", "#000");
                    field.setReadOnly(true);
                    field.setValue("");
                } else {
                    field.setMaxLength(1);
                    field.setPattern("[A-Za-z]");
                    field.setValue(String.valueOf(cell.getLetter()));
                    field.setReadOnly(true);
                    field.getStyle().set("background", "#fff");

                    // Add number indicator if this is the start of a word
                    boolean isAcrossStart = cell.getAcrossWord() != null &&
                            (x == 0 || crossword.getGrid()[x - 1][y].getAcrossWord() != cell.getAcrossWord());
                    boolean isDownStart = cell.getDownWord() != null &&
                            (y == 0 || crossword.getGrid()[x][y - 1].getDownWord() != cell.getDownWord());

                    if (isAcrossStart || isDownStart) {
                        Span numberLabel = new Span();
                        numberLabel.getStyle()
                                .set("position", "absolute")
                                .set("top", "2px")
                                .set("left", "2px")
                                .set("font-size", "0.7em")
                                .set("pointer-events", "none");

                        // Find the word number
                        int wordNumber = 1;
                        for (CrosswordWord cw : crossword.getWords()) {
                            if ((cw.isAcross() && cw.getX() == x && cw.getY() == y) ||
                                    (!cw.isAcross() && cw.getX() == x && cw.getY() == y)) {
                                break;
                            }
                            wordNumber++;
                        }

                        numberLabel.setText(String.valueOf(wordNumber));
                        cellContainer.add(numberLabel);
                    }
                }

                cellContainer.add(field);
                crosswordGrid.add(cellContainer);
                cellFields[x][y] = field;
            }
        }

        // Create clues section
        cluesLayout = new VerticalLayout();
        cluesLayout.setWidth("300px");
        cluesLayout.setHeight("600px");
        cluesLayout.setSpacing(false);
        cluesLayout.setPadding(false);

        Div acrossClues = new Div();
        acrossClues.add(new H3("Across"));
        Div downClues = new Div();
        downClues.add(new H3("Down"));

        int wordNumber = 1;
        for (CrosswordWord cw : crossword.getWords()) {
            Div clueDiv = new Div();
            clueDiv.getStyle()
                    .set("margin-bottom", "8px")
                    .set("font-size", "0.9em");
            clueDiv.add(new Span(wordNumber + ". " + cw.getWord().getTextTranslation()));

            if (cw.isAcross()) {
                acrossClues.add(clueDiv);
            } else {
                downClues.add(clueDiv);
            }

            wordNumber++;
        }

        cluesLayout.add(acrossClues, downClues);
        mainLayout.add(crosswordGrid, cluesLayout);
        add(mainLayout);
    }
}