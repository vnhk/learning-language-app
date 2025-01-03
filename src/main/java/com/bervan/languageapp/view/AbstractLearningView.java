package com.bervan.languageapp.view;

import com.bervan.common.AbstractPageView;
import com.bervan.languageapp.TranslationRecord;
import com.bervan.languageapp.component.Flashcard;
import com.bervan.languageapp.service.TranslationRecordService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Abstract class for displaying learning flashcards.
 * It shows flashcards from the database and allows marking them as "again",
 * "hard", "good" or "easy". Next repetition dates are updated accordingly.
 * <p>
 * Six checkboxes (A1, A2, B1, B2, C1, C2) at the top of this view
 * to filter the data by level. By default, A1, A2, B1, and B2 are selected.
 * Any change to the checkbox selection triggers a data reload.
 */
public abstract class AbstractLearningView extends AbstractPageView {
    public static final String ROUTE_NAME = "learning-english-app/learning-view";

    // Service for accessing and updating TranslationRecord entities
    private final TranslationRecordService translationRecordService;

    // Flashcard knowledge buttons
    private final Button againButton = new Button("Again");
    private final Button hardButton = new Button("Hard");
    private final Button goodButton = new Button("Good");
    private final Button easyButton = new Button("Easy");

    // Div container for the flashcard knowledge buttons
    private final Div buttonsLayout = new Div();

    // Checkboxes to filter levels (A1, A2, B1, B2, C1, C2)
    private final Checkbox levelNotClass = new Checkbox("N/A");
    private final Checkbox levelA1 = new Checkbox("A1");
    private final Checkbox levelA2 = new Checkbox("A2");
    private final Checkbox levelB1 = new Checkbox("B1");
    private final Checkbox levelB2 = new Checkbox("B2");
    private final Checkbox levelC1 = new Checkbox("C1");
    private final Checkbox levelC2 = new Checkbox("C2");

    // Checkbox for reversed flashcards
    private final Checkbox reversedSwitch = new Checkbox("Reversed flashcards?");

    // Flashcard currently being viewed
    private Flashcard currentFlashCard = null;

    // Unique identifier of the current card
    private UUID currentCardId = UUID.randomUUID();

    // Label to show how many flashcards left
    private H2 flashcardLeftCounter;

    private final List<TranslationRecord> all = new ArrayList<>();

    // Constructor
    public AbstractLearningView(TranslationRecordService translationRecordService) {
        super();

        // Initialize service
        this.translationRecordService = translationRecordService;

        // Setup reversedSwitch as a "switch" style
        reversedSwitch.getElement().setAttribute("theme", "switch");

        // Add a custom layout for this view (e.g. a header, etc.)
        add(new LearningEnglishLayout(ROUTE_NAME));

        // Style classes for the buttons
        againButton.addClassName("option-button");
        hardButton.addClassName("option-button");
        goodButton.addClassName("option-button");
        easyButton.addClassName("option-button");
        againButton.addClassName("flashcard-knowledge");
        hardButton.addClassName("flashcard-knowledge");
        goodButton.addClassName("flashcard-knowledge");
        easyButton.addClassName("flashcard-knowledge");

        // Add all four buttons to the button layout
        buttonsLayout.add(againButton, hardButton, goodButton, easyButton);
        buttonsLayout.setVisible(false);

        // Hidden field (UUID) - sometimes used for passing internal ID
        TextField uuid = new TextField("");
        uuid.setId("uuid");
        uuid.setVisible(false);

        // =========================
        // 1. SETUP LEVEL CHECKBOXES
        // =========================
        // By default, A1, A2, B1, B2 are selected
        levelNotClass.setValue(true);
        levelA1.setValue(true);
        levelA2.setValue(true);
        levelB1.setValue(true);
        levelB2.setValue(true);
        // C1, C2 remain unchecked by default

        loadLearningRecords();

        // Put all level checkboxes in a single container
        Div levelCheckBoxesLayout = new Div(levelNotClass, levelA1, levelA2, levelB1, levelB2, levelC1, levelC2);

        // Add the level checkboxes to the top of the page
        add(levelCheckBoxesLayout);

        // ============================
        // 2. SETUP CHECKBOX LISTENERS
        // ============================
        // Each checkbox triggers data reload on change
        levelNotClass.addValueChangeListener(event -> reloadData());
        levelA1.addValueChangeListener(event -> reloadData());
        levelA2.addValueChangeListener(event -> reloadData());
        levelB1.addValueChangeListener(event -> reloadData());
        levelB2.addValueChangeListener(event -> reloadData());
        levelC1.addValueChangeListener(event -> reloadData());
        levelC2.addValueChangeListener(event -> reloadData());

        // ==============
        // 3. BUTTON LOGIC
        // ==============
        againButton.addClickListener(buttonClickEvent ->
                postButtonClickActions(translationRecordService, "AGAIN"));
        hardButton.addClickListener(buttonClickEvent ->
                postButtonClickActions(translationRecordService, "HARD"));
        goodButton.addClickListener(buttonClickEvent ->
                postButtonClickActions(translationRecordService, "GOOD"));
        easyButton.addClickListener(buttonClickEvent ->
                postButtonClickActions(translationRecordService, "EASY"));

        setNextToLearn();
    }

    /**
     * Reloads the data when level checkboxes are changed.
     * Removes the current card display and fetches fresh data from the DB.
     */
    private void reloadData() {
        // Remove any existing flashcard and counter from the UI
        if (currentFlashCard != null) {
            remove(currentFlashCard);
        }
        if (flashcardLeftCounter != null) {
            remove(flashcardLeftCounter);
        }

        // Load new data based on the selected levels
        loadLearningRecords();

        // Display the next flashcard (or show notification if none found)
        setNextToLearn();
    }

    /**
     * Loads learning records from the database, filtering by:
     * 1) next repeat time is not after `now`,
     * 2) levels that are currently selected via the checkboxes.
     */
    private void loadLearningRecords() {
        List<String> selectedLevels = getSelectedLevels();
        all.removeAll(all);
        all.addAll(translationRecordService
                .getAllForLearning(selectedLevels, Pageable.ofSize(50).first()));
    }

    private List<String> getSelectedLevels() {
        List<String> levels = new ArrayList<>();
        if (levelNotClass.getValue()) levels.add("N/A");
        if (levelA1.getValue()) levels.add("A1");
        if (levelA2.getValue()) levels.add("A2");
        if (levelB1.getValue()) levels.add("B1");
        if (levelB2.getValue()) levels.add("B2");
        if (levelC1.getValue()) levels.add("C1");
        if (levelC2.getValue()) levels.add("C2");
        return levels;
    }

    /**
     * Performs common actions after user clicks a knowledge button.
     * Depending on the button type (AGAIN, HARD, GOOD, EASY), calls the
     * service to update the next learning date in the DB. Then removes
     * the used flashcard from the UI and fetches the next card.
     *
     * @param translationRecordService service for data update
     * @param button                   type of knowledge button pressed
     */
    private void postButtonClickActions(TranslationRecordService translationRecordService,
                                        String button) {

        // Update next learning date for the current card
        translationRecordService.updateNextLearningDate(currentCardId, button);

        // Hide the buttons
        buttonsLayout.setVisible(false);

        // Remove the current flashcard and counter
        if (currentFlashCard != null) {
            remove(currentFlashCard);
        }
        if (flashcardLeftCounter != null) {
            remove(flashcardLeftCounter);
        }

        // If the list wasn't passed in, reload data from DB
        if (all == null) {
            loadLearningRecords();
        } else {
            // If we have a list, remove the current item from it
            all.removeIf(next -> next.getId().equals(currentCardId));
        }

        // Now set the next card to learn
        setNextToLearn();
    }

    /**
     * Displays the next flashcard to learn or shows a notification if no more remain.
     * If there are no flashcards left, tries reloading them one more time.
     */
    private void setNextToLearn() {
        // If no cards left, try reloading from DB
        if (all.isEmpty()) {
            loadLearningRecords();
            // If still no cards, show a notification
            if (all.isEmpty()) {
                showPrimaryNotification("No flashcards for that moment. Come back later!");
                return;
            }
        }

        // Grab the next flashcard
        TranslationRecord translationRecord = all.iterator().next();
        currentCardId = translationRecord.getId();

        // Show how many flashcards are left
        flashcardLeftCounter = new H2("Flashcards left: " + all.size());

        // Create a new Flashcard component
        currentFlashCard = new Flashcard(translationRecord, buttonsLayout, reversedSwitch.getValue());
        add(reversedSwitch, flashcardLeftCounter, currentFlashCard);

        // If the reversed switch is toggled, we remove everything and reload
        reversedSwitch.addValueChangeListener(checkboxBooleanComponentValueChangeEvent -> {
            remove(currentFlashCard);
            remove(flashcardLeftCounter);
            remove(reversedSwitch);
            setNextToLearn();
        });

        // Update tooltip text with dynamic hour calculations
        againButton.setTooltipText("<"
                + TranslationRecordService.getHoursUntilNextRepeatTime(
                TranslationRecordService.getNextFactor("AGAIN",
                        translationRecordService.getFactor(currentCardId)), "AGAIN") + "h");
        hardButton.setTooltipText("<"
                + TranslationRecordService.getHoursUntilNextRepeatTime(
                TranslationRecordService.getNextFactor("HARD",
                        translationRecordService.getFactor(currentCardId)), "HARD") + "h");
        goodButton.setTooltipText("<"
                + TranslationRecordService.getHoursUntilNextRepeatTime(
                TranslationRecordService.getNextFactor("GOOD",
                        translationRecordService.getFactor(currentCardId)), "GOOD") + "h");
        easyButton.setTooltipText("<"
                + TranslationRecordService.getHoursUntilNextRepeatTime(
                TranslationRecordService.getNextFactor("EASY",
                        translationRecordService.getFactor(currentCardId)), "EASY") + "h");
    }
}