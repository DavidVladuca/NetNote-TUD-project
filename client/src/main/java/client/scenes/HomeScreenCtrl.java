package client.scenes;

import client.HomeScreen;
import client.utils.Command;
import client.utils.ServerUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.fasterxml.jackson.databind.ObjectMapper;
import commons.Collection;
import commons.Language;
import commons.LanguageOptions;
import commons.Note;
import commons.Server;
import commons.Tag;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Text;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class HomeScreenCtrl {
    /**
     * Logger for all errors.
     */
    private final Logger errorLogger = Logger.getLogger(
            HomeScreenCtrl.class.getName());
    /**
     * controller for the main screen.
     */
    private final ScreenCtrl sc;

    /**
     * connector for the server.
     */
    private final ServerUtils serverUtils;

    /**
     * main stage.
     */
    private Stage primaryStage;

    /**
     * scene for the home screen.
     */
    private javafx.scene.Scene homeScene;
    /**
     * scene for the collection editor.
     */
    private javafx.scene.Scene editCollectionScene;


    /**
     * Initializes the whole scene.
     * @param localPrimaryStage - main stage to be used
     * @param home - home screen controller
     * @param editCollection - edit collections controller
     */

    public void init(
            final Stage localPrimaryStage,
            final Pair<HomeScreenCtrl, Parent> home,
            final Pair<EditCollectionsViewCtrl, Parent> editCollection) {
        this.primaryStage = localPrimaryStage;
        this.homeScene = new javafx.scene.Scene(home.getValue());
        this.editCollectionScene = new javafx.scene.Scene(
                editCollection.getValue());
        showHome();
        localPrimaryStage.show();
    }

    /**
     * Sets the scene as the home screen.
     */
    public void showHome() {
        if (primaryStage == null) {
            throw new IllegalStateException("Primary stage is not initialized");
        }
        primaryStage.setScene(homeScene);
    }

    /**
     * Triggers the edit collection viewer.
     */
    public void showEditCollection() {
        if (primaryStage == null) {
            throw new IllegalStateException("Primary stage is not initialized");
        }
        primaryStage.setScene(editCollectionScene);
    }


    /**
     * Constructor for the home screen controller.
     * @param localScene - scene used
     * @param localServerUtils - server to be used
     */
    @Inject
    public HomeScreenCtrl(
            final ScreenCtrl localScene, final ServerUtils localServerUtils) {
        this.sc = localScene;
        this.serverUtils = localServerUtils;
        availableTags = FXCollections.observableArrayList(
                new Tag("#Tag1"),
                new Tag("#Tag2"),
                new Tag("#Tag3")
        );
    }

    /**
     * HTTP Successful request code number.
     */
    private final int requestSuccessfulCode = 200;
    /**
     * HTTP "Successful creation upon request" code number.
     */
    private final int creationSuccessfulCode = 201;


    /**
     * Button for adding a new note.
     */
    @FXML
    private Button addB;
    /**
     * Button for deleting a note.
     */
    private Button deleteB;
    /**
     * Button for undo-ing last change.
     */
    private Button undoB;
    /**
     * TBH IDK - todo.
     */
    private Button dropDownSearchNoteB;
    /**
     * Go to previous search match in a note.
     */
    private Button prevMatchB;
    /**
     * Go to next search match in a note.
     */
    private Button nextMatchB;
    /**
     * Refresh all notes from server.
     */

    private Button refreshB;

    /**
     * Button for editing collections.
     */
    public Button editCollectionsB;

    /**
     * Collection text item in JavaFX.
     */
    public Text collectionText;

    /**
     * Language text item in JavaFX.
     */

    public Text languageText;

    /**
     * Bundle containing all languages.
     */
    private ResourceBundle bundle;
    /**
     * current locale.
     */
    private Locale locale;
    /**
     * Language selection box in JavaFX.
     */
    public ComboBox<Language> selectLangBox = new ComboBox<>();
    /**
     * Title text field in the scene.
     */
    public TextField noteTitleF;
    /**
     * Body text field in the scene.
     */
    public TextArea noteBodyF;

    /**
     * Collection search text field in the scene.
     */
    public TextField searchCollectionF;
    /**
     * Note search text field in the scene.
     */
    public TextField searchNoteF;

    /**
     * note search index (0 by default).
     */
    private int currentSearchIndex = 0;
    /**
     * Collection text field in the scene.
     */
    private  ArrayList<Long> noteMatchIndices;

    /**
     * IDK TBH - todo.
     */
    private Button searchMore;
    /**
     * Output showing actual note.
     */
    public WebView markDownOutput;
    /**
     * Selection box for the working collection.
     */
    public ChoiceBox<Collection> selectCollectionBox = new ChoiceBox<>();
    /**
     * Invoker for keeping history of commands and executing them.
     */
    private final CommandInvoker invoker = new CommandInvoker();
    /**
     * Keeps track of last note deleted.
     * It is useful for the undo button.
     */
    private Note lastDeletedNote = null;
    /**
     * Button to show tags scene.
     */
    private Button tagsButton;

    /**
     * List of all available tags (replace with actual fetching logic).
     */
    private final ObservableList<Tag> availableTags;

    /**
     * current server being used.
     */
    private final Server currentServer = new Server();

    /**
     * Current collection. If just the program for the first time
     * makes a default collection.
     */
    private Collection currentCollection = new Collection(
            currentServer, "Default");

    /**
     * Current note. If just the program for the first time
     * makes a default collection.
     */
    private Note currentNote = new Note("", "", currentCollection);

    /**
     * List of all the notes in the application. Useful for the Observable list.
     */
    public ListView<Note> notesListView;
    /**
     * Display of all the notes in the application.
     */
    private final ObservableList<Note> notes = FXCollections
            .observableArrayList();

    /**
     * Scheduler for the executor.
     */
    private final ScheduledExecutorService scheduler = Executors
            .newScheduledThreadPool(1);

    /**
     * Main parser.
     */
    public final Parser parser = Parser.builder().build();
    /**
     * Main renderer.
     */
    public final HtmlRenderer renderer = HtmlRenderer.builder().build();

    /**
     * The original title, prior to the change.
     */
    private String originalTitle;
    /**
     * Boolean that determines whether title is in progress or not.
     */
    private boolean isTitleEditInProgress = false;

    /**
     * Title of note in last sync to server.
     */
    private String lastSyncedTitle = "";
    /**
     * Body of note in last sync to server.
     */
    private String lastSyncedBody = "";





    /**
     * This method initializes the controller
     * and sets up the listener for the text area that the user types in.
     * based on other methods it calls
     */
    @FXML
    public void initialize() {
        final int period = 5;
        keyboardShortcuts();
        arrowKeyShortcuts();
        scheduler.scheduleAtFixedRate(
                this::syncIfChanged, 0, period, TimeUnit.SECONDS);
        setUpLanguages();
        setUpCollections();
        markDownTitle();
        markDownContent();
        loadNotesFromServer();
        setupNotesListView();
        loadTagsFromServer();
        handleTitleEdits();
        prevMatch();
        nextMatch();
    }

    /**
     * Method to handle title edits.
     */
    private void handleTitleEdits() {
        notesListView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        originalTitle = newValue.getTitle();
                        noteTitleF.setText(originalTitle);
                        // Reset the flag when use selects a note
                        isTitleEditInProgress = false;
                    }
                });

        noteTitleF.focusedProperty()
                .addListener((observable, oldValue, newValue) -> {
                    // Only call if title was edited
                    if (!newValue && isTitleEditInProgress) {
                        titleEdit();
                    }
                });

        noteTitleF
                .textProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (!newValue.equals(originalTitle)) {
                        isTitleEditInProgress = true; // Title is being edited
                    }
                });
    }


    private void loadTagsFromServer() {
        try (var response = ClientBuilder.newClient()
                .target("http://localhost:8080/api/tags")
                .request(MediaType.APPLICATION_JSON)
                .get()) {

            if (response.getStatus() == requestSuccessfulCode) {
                String json = response.readEntity(String.class);
                ObjectMapper mapper = new ObjectMapper();
                List<Tag> fetchedTags = mapper
                        .readValue(json, mapper
                                .getTypeFactory()
                                .constructCollectionType(List.class,
                                        Tag.class));
                availableTags.clear();
                availableTags.addAll(fetchedTags);
            } else {
                System.err.println("Failed to fetch tags. Status: "
                        + response.getStatus());
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // todo edit and delete button for the tags

    /**
     * Pops up the tags view.
     */
    @FXML
    public void handleTagsButtonAction() {
        final int tagProfileHeight = 100;
        final int v = 10;
        if (currentNote == null) {
            System.err.println("No note selected. Cannot assign tags.");
            return;
        }

        // Log current tags
        System.out.println(
                "Current tags for note '" + currentNote.getTitle() + "':");
        currentNote.getTags()
                .forEach(tag -> System.out.println("- " + tag.getName()));

        // Create the dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Manage Tags");
        dialog.setHeaderText("Select or create tags for the note:");

        // Container for tag checkboxes
        VBox tagListContainer = new VBox(v);
        List<CheckBox> tagCheckBoxes = new ArrayList<>();

        // Populate the tag list with checkboxes
        for (Tag tag : availableTags) {
            CheckBox checkBox = new CheckBox(tag.getName());
            checkBox.setSelected(currentNote.getTags().contains(tag));
            tagCheckBoxes.add(checkBox);
            tagListContainer.getChildren().add(checkBox);
        }

        // ScrollPane for tag list
        ScrollPane scrollPane = new ScrollPane(tagListContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(tagProfileHeight);
        // Limit to approx. 4 tags visible

        // Add Tag Button
        Button addTagButton = new Button("Add a Tag");
        addTagButton.setOnAction(event -> {
            TextInputDialog inputDialog = new TextInputDialog();
            inputDialog.setTitle("Add Tag");
            inputDialog.setHeaderText("Enter the name of the new tag:");
            inputDialog.setContentText("Tag name:");

            Optional<String> result = inputDialog.showAndWait();
            result.ifPresent(tagName -> {
                if (tagName.trim().isEmpty()) {
                    showErrorDialog(
                            "Invalid Tag Name",
                            "The tag name cannot be empty.");
                    return;
                }

                if (availableTags.stream()
                        .anyMatch(tag -> tag
                                .getName()
                                .equals(tagName.trim()))) {
                    showErrorDialog("Duplicate Tag",
                            "A tag with the name '"
                                    + tagName.trim()
                                    + "' already exists.");
                    return;
                }

                Tag newTag = new Tag(tagName.trim());
                availableTags.add(newTag);

                CheckBox newCheckBox = new CheckBox(newTag.getName());
                tagCheckBoxes.add(newCheckBox);
                tagListContainer.getChildren().add(newCheckBox);

                try {
                    saveTagToServer(newTag);
                    System.out.println(
                            "New tag saved to server: " + newTag.getName());
                } catch (Exception e) {
                    System.err.println(
                            "Failed to save the new tag: " + e.getMessage());
                }
            });
        });

        // Layout for the dialog content
        final int v2 = 15;
        VBox dialogContent = new VBox(v2, scrollPane, addTagButton);
        dialog.getDialogPane().setContent(dialogContent);
        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(ButtonType.OK, ButtonType.CANCEL);

        // Handle dialog result
        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                Set<Tag> selectedTags = new HashSet<>();
                for (int i = 0; i < tagCheckBoxes.size(); i++) {
                    if (tagCheckBoxes.get(i).isSelected()) {
                        selectedTags.add(availableTags.get(i));
                    }
                }
                updateNoteTags(selectedTags);
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void showErrorDialog(
            final String currentTitle, final String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(currentTitle);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void updateNoteTags(final Set<Tag> selectedTags) {
        Set<Tag> currentTags = new HashSet<>(currentNote.getTags());
        boolean tagsUpdated = false;

        // Add new tags
        for (Tag tag : selectedTags) {
            if (!currentTags.contains(tag)) {
                currentTags.add(tag);
                System.out.println("Tag added to note: " + tag.getName());
                tagsUpdated = true;
            }
        }

        // Remove unselected tags
        for (Tag tag : new HashSet<>(currentTags)) {
            if (!selectedTags.contains(tag)) {
                currentTags.remove(tag);
                System.out.println("Tag removed from note: " + tag.getName());
                tagsUpdated = true;
            }
        }

        if (tagsUpdated) {
            currentNote.setTags(currentTags);
            syncNoteTagsWithServer(currentNote);
        }
    }

    private void saveTagToServer(final Tag tag) throws IOException {
        String json = new ObjectMapper().writeValueAsString(tag);
        try (var response = ClientBuilder.newClient()
                .target("http://localhost:8080/api/tags/create")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(json, MediaType.APPLICATION_JSON))) {

            if (response.getStatus() != creationSuccessfulCode) {
                throw new IOException("Failed to save tag. Status: "
                        + response.getStatus());
            }
        }
    }


    private void syncNoteTagsWithServer(final Note note) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Set<String> tagNames = note
                    .getTags()
                    .stream()
                    .map(Tag::getName)
                    .collect(Collectors.toSet());
            String json = mapper.writeValueAsString(tagNames);

            try (Response response = ClientBuilder.newClient()
                    .target("http://localhost:8080/api/notes/"
                            + note.getNoteId()
                            + "/tags")
                    .request(MediaType.APPLICATION_JSON)
                    .put(Entity.entity(json, MediaType.APPLICATION_JSON))) {

                if (response.getStatus() != requestSuccessfulCode) {
                    System.err.println(
                            "Failed to sync tags with server. Status: "
                                    + response.getStatus()
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Error syncing tags: " + e.getMessage());
        }
    }


    /**
     * This method calls the keyboard shortcuts, separately when noteListView is
     * in focus because it did not work normally
     */
    public void keyboardShortcuts() {
        Platform.runLater(() -> {
            Scene currentScene = addB.getScene();
            if (currentScene == null) {
                System.out.println("Scene is not set!");
                return;
            }
            // Normal shortcuts
            currentScene.setOnKeyPressed(event -> handleKeyboardShortcuts(event));
            // Special case if focus is on the notes list view
            notesListView.setOnKeyPressed(event -> handleKeyboardShortcuts(event));
        });
    }

    /**
     * This method is calling the keyboard shortcuts
     * @param event - the key being pressed
     */
    private void handleKeyboardShortcuts(KeyEvent event) {
        handleAddDeleteShortcuts(event);
        handleNavigationShortcuts(event);
        handleUtilityShortcuts(event);
        handleChoiceShortcuts(event);
    }

    /**
     * This method is calling the keyboard shortcuts for add and delete and specifies them
     * @param event - the key being pressed
     */
    private void handleAddDeleteShortcuts(KeyEvent event) {
        // When clicking 'Shift + A' add method will be called
        if (event.isShiftDown() && event.getCode() == KeyCode.A) {
            try {
                add();
                event.consume();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        // When clicking 'Delete' delete method will be called
        if (event.getCode() == KeyCode.DELETE) {
            delete();
            event.consume();
        }
    }

    /**
     * This method is calling the keyboard shortcuts for navigation such as ESC to search,
     * note search, show shortcuts, etc.
     * @param event - the key being pressed
     */
    private void handleNavigationShortcuts(KeyEvent event) {
        // When clicking 'Shift + S' the show shortcuts alert will
        // pop up
        if (event.isShiftDown()
                && event.getCode() == KeyCode.S) {
            showShortcuts();
            event.consume();
        }
        // ESC sets the focus to collection search
        if(event.getCode() == KeyCode.ESCAPE) {
            if(searchCollectionF.isFocused()) {
                notesListView.requestFocus();
                event.consume();
            } else {
                searchCollectionF.requestFocus();
                event.consume();
            }
        }
        // Ctrl + F to search in a note
        if(event.isControlDown()
                && event.getCode() == KeyCode.F) {
            searchNoteF.requestFocus();
            event.consume();
        }
    }

    /**
     * This method is calling the keyboard shortcuts for utilities,
     * such as undo, refresh, tags, etc.
     * @param event - the key being pressed
     */
    private void handleUtilityShortcuts(KeyEvent event) {
        // Ctrl + Z for undo
        if(event.isControlDown()
                && event.getCode() == KeyCode.Z) {
            undo();
            event.consume();
        }
        // F5 for refresh
        if(event.getCode() == KeyCode.F5) {
            refresh();
            event.consume();
        }
        // Shift + T to open up the tags edit
        if(event.isShiftDown()
                && event.getCode() == KeyCode.T) {
            handleTagsButtonAction();
            event.consume();
        }
        // Shift + E to open up edit collections
        if(event.isShiftDown() && event.getCode() == KeyCode.E) {
            editCollections();
            event.consume();
        }
    }

    /**
     * This method is calling the keyboard shortcuts for focus,
     * so to open collection choice box and language combo box
     * @param event - the key being pressed
     */
    private void handleChoiceShortcuts(KeyEvent event) {
        // Shift + L to open up language combo box
        if(event.isShiftDown()
                && event.getCode() == KeyCode.L) {
            selectLangBox.requestFocus();
            selectLangBox.show();
            event.consume();
        }
        // Shift + C to open up collection choice box
        if(event.isShiftDown()
                && event.getCode() == KeyCode.C) {
            selectCollectionBox.requestFocus();
            selectCollectionBox.show();
            event.consume();
        }
    }

    /**
     * This method sets up the arrow key shortcuts in the TextField for custom
     * navigation with shift.
     */
    private void arrowKeyShortcuts() {
        // For note title
        noteTitleF.setOnKeyPressed(event -> {
            // Page down - go from note title to note body
            if (event.getCode() == KeyCode.PAGE_DOWN) {
                noteBodyF.requestFocus();
            }
        });
        // For note body
        noteBodyF.setOnKeyPressed(event -> {
            // Page up - go from note body to note title
            if (event.getCode() == KeyCode.PAGE_UP) {
                noteTitleF.requestFocus();
            }
        });
    }


    /**
     * Displays a pop-up with the list of keyboard shortcuts and their
     * descriptions.
     */
    public void showShortcuts() {
        Alert shortcuts = new Alert(Alert.AlertType.INFORMATION);
        shortcuts.setTitle("Keyboard Shortcuts");
        shortcuts.setHeaderText("Available Keyboard Shortcuts");
        shortcuts.setContentText(
                """
                        Shift + A: Add a new note
                        Delete: Delete the selected note
                        Page Up/Down: Navigate between note title and content
                        Shift + S: Show shortcuts pop-up
                        Shift + L: Show available languages
                        Shift + C: Show available collections
                        Shift + E: Edit collections
                        Shift + T: Edit Tags
                        F5: \t\tRefresh
                        Ctrl + F: Search within a note
                        ESC: Set/reset focus to the collection search
                        """
        );
        shortcuts.showAndWait();
    }

    /**
     * This method ensures that the title and the content of the Note
     * will be synced with the database every 5 seconds if something was
     * changed.
     * 5 seconds is specified in initialize method
     */
    private void syncIfChanged() {
        Platform.runLater(() -> {
            // Check if the note content has changed since the last sync
            if (currentNote != null
                    && (!currentNote.getBody().equals(lastSyncedBody)
            || !currentNote.getTitle().equals(lastSyncedTitle))) {

                // Sync with the server if change
                syncNoteWithServer(currentNote);

                // Update the last synced title and body to current title
                // and body
                lastSyncedTitle = currentNote.getTitle();
                lastSyncedBody = currentNote.getBody();

                System.out.println("Note synced with the server at: "
                        + java.time.LocalTime.now()); // for testing
            }
        });

    }

    /**
     * This method ensures the syncing with the server (database).
     * @param note - note provided - in syncIfChanged method to be specific
     */
    private void syncNoteWithServer(final Note note) {
        try {
            String json = new ObjectMapper().writeValueAsString(note);
            System.out.println("Serialized JSON: " + json);  // for testing

            var requestBody = Entity.entity(json, MediaType.APPLICATION_JSON);
            // the put request to actually update the content with json
            try (var response = ClientBuilder.newClient()
                    .target("http://localhost:8080/api/notes/update")
                    .request(MediaType.APPLICATION_JSON)
                    .put(requestBody)) {

                // Refresh the notes list
                Platform.runLater(() -> refreshNotesInListView(note));
                // for testing
                System.out.println("Response Status: " + response.getStatus());
                System.out.println("Response Body: "
                        + response.readEntity(String.class));
                // if something screwed up :D
                if (response.getStatus() != requestSuccessfulCode) {
                    System.err.println(
                            "Failed to update note on server. Status code: "
                                    + response.getStatus()
                    );
                }
            }
        } catch (Exception e) {
            errorLogger.log(Level.INFO, e.getMessage(), e);
        }
    }

    private void refreshNotesInListView(final Note note) {
        for (int i = 0; i < notes.size(); i++) {
            Note update = notes.get(i);
            if (note.getNoteId() == update.getNoteId()) {
                if (update.getTitle() != null && !update.getTitle().isEmpty()) {
                    // Update the note's data in the ObservableList
                    notes.set(i, update);
                    break;
                }
            }
        }
    }

    /**
     * Edits the collections in the scene.
     */
    public void editCollections() {
        System.out.println("Edit Collections View Selected");
        sc.showEditCollection();
    }


    private void loadNotesFromServer() {
        try {
            // Fetch notes from the server
            var response = ClientBuilder.newClient()
                    // todo - Update with your server's API URL
                    .target("http://localhost:8080/api/notes/fetch")
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            if (response.getStatus() == requestSuccessfulCode) {
                // Parse the JSON response into a List of Note objects
                String json = response.readEntity(String.class);
                ObjectMapper mapper = new ObjectMapper();
                List<Note> fetchedNotes = mapper.readValue(
                        json,
                        mapper.getTypeFactory()
                                .constructCollectionType(
                                        List.class, Note.class));

                // Add the fetched notes to the ObservableList
                notes.clear(); // Clear existing notes
                notes.addAll(fetchedNotes);
                for (Note note:fetchedNotes) {
                    currentCollection.addNote(note);
                }
            } else {
                System.err.println(
                        "Failed to fetch notes. Error code "
                                + response.getStatus());
            }
        } catch (Exception e) {
            System.err.println("Error loading the notes: " + e.getMessage());
        }
    }

    private void setupNotesListView() {
        // Binding the ObservableList to the ListView
        notesListView.setItems(notes);

        // Setting the rendering of each note in the ListView
        notesListView.setCellFactory(param -> new ListCell<>() {

            /**
             * Updates the content and appearance of a cell in the ListView.
             * This method is called whenever the item in the cell changes, or
             *      the cell becomes empty, or it is being re-rendered due to
             *      changes in the ListView (e.g., scrolling or data updates).
             *
             * @param note - the Note object associated with this cell. It may
             *             be null if the cell is empty.
             * @param empty - a boolean indicating whether the cell is empty.
             *              If true, the cell should be cleared and not display
             *              any content.
             */
            @Override
            protected void updateItem(final Note note, final boolean empty) {
                super.updateItem(note, empty);
                setText(empty || note == null ? null : note.getTitle());
            }
        });

        // Handling selection in the ListView
        notesListView.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldNote, newNote) -> {
            if (newNote != null) {
                currentNote = newNote;
                //change the output in the front-end for title and body
                noteTitleF.setText(newNote.getTitle());
                noteBodyF.setText(newNote.getBody());

                Platform.runLater(() -> notesListView.getSelectionModel());
            }
        });
    }

    /**
     * This method adds the listener to the title field. It automatically
     * converts the content to a heading of type h1, because it is a title
     */
    public void markDownTitle() {
        noteTitleF.textProperty().addListener((observable, oldValue, newValue)
                -> {
            currentNote.setTitle(newValue);

            // Convert the title and body to HTML
            String showTitle = "<h1>"
                    + renderer.render(parser.parse(newValue))
                    + "</h1>";
            String showContent = renderer.render(
                    parser.parse(noteBodyF.getText()));

            // Load the combined title and content into the WebView
            String titleAndContent = showTitle + showContent;
            markDownOutput.getEngine().loadContent(titleAndContent);
        });
    }


    /**
     * This method adds the listener to the content/body field.
     * It fully supports the Markdown syntax based on the commonmark library.
     */
    public void markDownContent() {
        noteBodyF.textProperty().addListener((observable, oldValue, newValue)
                -> {
            currentNote.setBody(newValue);

            // Convert the title and body to HTML
            String showTitle = "<h1>"
                    + renderer.render(parser.parse(noteTitleF.getText()))
                    + "</h1>";
            String showContent = renderer.render(parser.parse(newValue));

            // Load the combined title and content into the WebView
            String titleAndContent = showTitle + showContent;
            markDownOutput.getEngine().loadContent(titleAndContent);
        });
    }



    /**
     * Utility function used to locate resources within applications filepath.
     * @param path - path of the scene
     * @return - returns the URL for that particular scene
     */
    private static URL getLocation(final String path) {
        return HomeScreen.class.getClassLoader().getResource(path);
    }

    /**
     * Adds a new note to ListView and Database.
     */
    @FXML
    public void add() throws IOException, InterruptedException {
        // Generate a unique title for the new note
        String baseTitle = "New note";
        String newTitle = baseTitle;
        int counter = 1;
        List<String> existingTitles = notes.stream()
                .map(Note::getTitle)
                .toList();
        while (existingTitles.contains(newTitle)) {
            newTitle = baseTitle + " (" + counter + ")";
            counter++;
        }

        // Create the new note
        Note newNote = new Note(newTitle, "", currentCollection);
        // Create command for adding new note
        Command addNoteCommand = new AddNoteCommand(this, newNote);
        // Use the invoker to execute the command
        invoker.executeCommand(addNoteCommand);
    }

    /**
     * Method to add a new note.
     * @param newNote - note to be added
     * @throws IOException - exception that will occur if server error
     */
    public void addCommand(final Note newNote) throws IOException {
        Note savedNote = saveNoteToServer(newNote);

        currentCollection.addNote(savedNote);  // Add to the collection
        notes.add(savedNote);                   // Add to the ObservableList
        notesListView.getSelectionModel().select(savedNote);
        // Update UI fields
        currentNote = savedNote;
        noteTitleF.setText(savedNote.getTitle());
        noteBodyF.setText(savedNote.getBody());
    }

    /**
     * Sends the note to the server via the create endpoint and returns
     * the saved note.
     * This ensures the note gets a valid noteId from the server.
     * It is very similar to addRequest method, however I needed to
     * return a new Note object
     * for the unique ID
     * @param note - note provided
     * @return a Note that was saved with a unique id
     */
    public Note saveNoteToServer(final Note note) throws IOException {
        var json = new ObjectMapper().writeValueAsString(note);
        var requestBody = Entity.entity(json, MediaType.APPLICATION_JSON);
        // Connect to the create endpoint, where add requests are processed
        try (var response = ClientBuilder.newClient()
                .target("http://localhost:8080/api/notes/create")
                .request(MediaType.APPLICATION_JSON)
                .post(requestBody)) {

            if (response.getStatus() == creationSuccessfulCode) {
                return response.readEntity(Note.class);
            } else {
                throw new IOException(
                        "Server returned status: " + response.getStatus());
            }
        }
    }


    /**
     * Sends request to the server to add a note with a provided Note.
     * @param note - Note
     */
    public void addRequest(final Note note) {
        try {
            var json = new ObjectMapper().writeValueAsString(note);
            var requestBody = Entity.entity(json, MediaType.APPLICATION_JSON);
            try (var response = ClientBuilder.newClient()
                    // Update with the correct endpoint for adding a note
                    .target("http://localhost:8080/api/notes/create")
                    .request(MediaType.APPLICATION_JSON)
                    .post(requestBody)) {
                System.out.println(
                        "Server addition request sent. Response is "
                                + response.toString());
            }
        } catch (Exception e) {
            errorLogger.log(
                    Level.INFO,
                    "Error requesting addition of note: " + e.getMessage());


        }
    }

    /**
     * Removes a selected note and stores it in a stack for future restoration.
     */
    public void delete() {
        Note selectedNote = notesListView.getSelectionModel().getSelectedItem();
        if (selectedNote != null) {
            // Create a confirmation alert
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Delete Note");
            alert.setHeaderText("Are you sure you want to delete this note?");
            alert.setContentText("Note: \"" + selectedNote.getTitle() + "\"");
            var result = alert.showAndWait(); // Waiting for user response

            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Create a delete command and execute it
                Command deleteCommand = new DeleteNoteCommand(
                        this, selectedNote);
                lastDeletedNote = selectedNote;
                invoker.executeCommand(deleteCommand);
                // For testing purposes
                System.out.println(
                        "Note deleted: " + lastDeletedNote.getTitle());
                System.out.println(
                        "Note deleted: " + lastDeletedNote.getNoteId());

                //Confirmation alert that note was deleted
                alert.setTitle("Note Deleted");
                alert.setHeaderText(null);
                alert.setContentText("The note has been successfully deleted!");
                alert.showAndWait();
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Note Selected!");
            alert.setHeaderText("No note selected to delete.");
            alert.setContentText(
                    "Please select a note from the list to delete.");
            alert.showAndWait();
        }
        System.out.println("Delete");  //Temporary for testing
    }

    /**
     * Deletes note.
     * @param noteId - ID of note to be deleted
     */
    public void deleteCommand(final long noteId) {
        // Find the note in the ObservableList by its ID
        Note noteToDelete = null;
        for (Note note : notes) {
            if (note.getNoteId() == noteId) {
                noteToDelete = note;
                break;
            }
        }
        // If the note is found, proceed with deletion
        if (noteToDelete != null) {
            // Remove the note from the ObservableList (UI)
            notes.remove(noteToDelete);
            // Remove the note from the current collection
            currentCollection.getNotes().remove(noteToDelete);
            // Send a delete request to the server
            deleteRequest(noteId);
        } else {
            // If the note is not found in the ObservableList, log a warning
            System.err.println("Note not found in the UI. ID: " + noteId);
        }
    }

    /**
     * Sends request to the server to delete a note by a provided ID.
     * @param noteId - ID of the note to be deleted
     */
    public static void deleteRequest(final long noteId) {
        Response response = ClientBuilder.newClient()
                // Endpoint for deletion
                .target("http://localhost:8080/api/notes/delete/" + noteId)
                .request()
                .delete();
        if (response.getStatus() == Response.Status.NO_CONTENT
                .getStatusCode()) {
            System.out.println("Note successfully deleted.");
        } else if (response.getStatus() == Response.Status.NOT_FOUND
                .getStatusCode()) {
            System.out.println("Note not found.");
        } else {
            System.out.println("Failed to delete note. Status: " + response
                    .getStatus());
        }
        response.close();
    }

    /**
     * Undoes the last action.
     */
    public void undo() {
        //Temporary for testing
        System.out.println("Undo");
        invoker.undoLastCommand();
    }

    /**
     * Edits the title of the currently selected note.
     */
    public void titleEdit() {
        if (!isTitleEditInProgress) {
            return;
        }

        Note selectedNote = notesListView.getSelectionModel().getSelectedItem();
        if (selectedNote != null) {
            String newTitle = noteTitleF.getText().trim();

            if (!newTitle.equals(originalTitle)) {
                try {
                    boolean isDuplicate = validateTitleWithServer(
                            currentCollection.getCollectionId(), newTitle);
                    // Show and alert if the title is duplicate
                    if (isDuplicate) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Duplicate Title");
                        alert.setHeaderText("Title Already Exists");
                        alert.setContentText("The title \""
                                + newTitle
                                + "\" already exists in this collection. "
                                + "Please choose a different one.");
                        alert.showAndWait();

                        // Revert to the original title
                        Platform.runLater(() -> noteTitleF
                                .setText(originalTitle));
                    } else {
                        // If not duplicate, update title and sync with
                        // the server
                        selectedNote.setTitle(newTitle);
                        originalTitle = newTitle;
                        syncNoteWithServer(selectedNote);
                    }
                } catch (Exception e) {
                    errorLogger.log(
                            Level.INFO,
                            "Error validating title with server: "
                                    + e.getMessage());
                }
            }
        }
    }

    /**
     * This method calls the noteValidator class and validates the
     * title with server.
     * @param collectionId - id of the collection that is the note
     *                     associated with
     * @param newTitle - the title to be checked
     * @return true if it is a duplicate, false if it is not
     * @throws IOException when it returns something else then 200/409 code
     */
    public boolean validateTitleWithServer(
            final Long collectionId, final String newTitle) throws IOException {
        return serverUtils.validateTitleWithServer(collectionId, newTitle);
    }


    /**
     * Refreshes the notes list by re-fetching the notes from the server.
     */
    public void refresh() {
        Platform.runLater(() -> {
            System.out.println("Refreshing all notes...");
            try {
                // Re-fetches all notes from the server
                // and updates the ObservableList
                loadNotesFromServer();
                System.out.println("All notes refreshed successfully!");
            } catch (Exception e) {
                errorLogger.log(
                        Level.FINE,
                        "Error refreshing notes: " + e.getMessage());
            }
        });
    }

    /**
     * Fetches a specific note from the server by its ID.
     *
     * @param noteId The ID of the note to fetch
     * @return The fetched Note object or null if it was not found
     * or there was an error
     */
    private Note fetchNoteById(final long noteId) {
        try {
            var response = ClientBuilder.newClient()
                    // Replace with actual API endpoint for fetching a single
                    // note
                    .target("http://localhost:8080/api/notes/" + noteId)
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            if (response.getStatus() == requestSuccessfulCode) {
                String json = response.readEntity(String.class);
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(json, Note.class);
            } else {
                System.err.println(
                        "Failed to fetch note with ID "
                                + noteId
                                + ". Status code: "
                                + response.getStatus()
                );
                return null;
            }
        } catch (Exception e) {
            System.err.println(
                    "Error fetching note with ID "
                            + noteId
                            + ": "
                            + e.getMessage()
            );
            return null;
        }
    }

    /**
     * Searches for a note based on text field input.
     */
    public void searchNote() { //make sure this remains like this after merge.
        String searchText = searchNoteF.textProperty().getValue();
        noteMatchIndices = currentNote.getMatchIndices(searchText);
        if (searchText.isEmpty()) {
            currentSearchIndex = 0;
        }
        String titleHighlighted = currentNote.getTitle();
        String bodyHighlighted = currentNote.getBody();
        if (!noteMatchIndices.isEmpty()) {
            if (noteMatchIndices.getFirst() == -1L
                    && noteMatchIndices.size() == 1L) {
                System.out.println(
                        "Not found in \"" + currentNote.getTitle() + "\"");
            } else {
                //parse in special way such that the found results are
                // highlighted
                for (int i = noteMatchIndices.size() - 1; i >= 0; i--) {
                    //iterating from the back to not have to consider changes in
                    // index due to additions
                    if (noteMatchIndices.get(i) < titleHighlighted.length()) {
                        if (i == currentSearchIndex) {
                            titleHighlighted = titleHighlighted.substring(
                                    0, Math.toIntExact(noteMatchIndices.get(i)))
                                    + "<mark style=\"background: #E1C16E\">"
                                    + searchText
                                    + "</mark>"
                                    + titleHighlighted
                                    .substring(Math.toIntExact(noteMatchIndices
                                            .get(i)) + searchText.length()
                                    );

                        } else {
                            titleHighlighted = titleHighlighted.substring(
                                    0, Math.toIntExact(noteMatchIndices.get(i)))
                                    + "<mark>"
                                    + searchText
                                    + "</mark>"
                                    + titleHighlighted
                                    .substring(Math.toIntExact(noteMatchIndices
                                            .get(i)) + searchText.length()
                                    );
                        }
                    } else {
                        if (i == currentSearchIndex) {
                            bodyHighlighted = bodyHighlighted
                                    .substring(0, Math.toIntExact(
                                            noteMatchIndices.get(i))
                                            - titleHighlighted.length())
                                    + "<mark style=\"background: #E1C16E\">"
                                    + searchText
                                    + "</mark>"
                                    + bodyHighlighted.substring(
                                    (Math.toIntExact(noteMatchIndices
                                            .get(i))
                                            - titleHighlighted
                                            .length())
                                            + searchText.length());
                        } else {
                            bodyHighlighted = bodyHighlighted
                                    .substring(
                                            0, Math.toIntExact(noteMatchIndices
                                                    .get(i))
                                                    - titleHighlighted.length())
                                    + "<mark>"
                                    + searchText
                                    + "</mark>"
                                    + bodyHighlighted.substring(
                                    Math.toIntExact(noteMatchIndices
                                            .get(i))
                                            - titleHighlighted.length()
                                            + searchText.length());
                        }
                    }
                }
            }
        }
        titleHighlighted  = "<h1>"
                + renderer.render(parser.parse(titleHighlighted))
                + "</h1>";
        bodyHighlighted = renderer.render(parser.parse(bodyHighlighted));
        String totalContent = titleHighlighted + bodyHighlighted;
        markDownOutput.getEngine().loadContent(totalContent);


    }

    /**
     * Searches through collection and displays notes that match search.
     */
    public void searchCollection() {
        String searchText = searchCollectionF.textProperty().getValue();
        ArrayList<ArrayList<Long>> collectionMatchIndices = currentCollection
                .getSearch(searchText);

        ObservableList<Note> displayNotes = FXCollections.observableArrayList();
        if (!collectionMatchIndices.isEmpty()) {
            if (collectionMatchIndices.getFirst().getFirst() == -1) {
                System.out.println("There are no matches for " + searchText);
                displayNotes.clear(); //gives an empty display
            } else {

                for (ArrayList<Long> collectionMatchIndex
                        : collectionMatchIndices) {
                    displayNotes.add(currentCollection
                            .getNoteByID(Math
                                    .toIntExact(collectionMatchIndex
                                            .getFirst())));
                }
            }
        } else {
            displayNotes = notes;
        }
        notesListView.setItems(displayNotes);
    }

    /**
     * Sets up the languages (adds them to the collection box).
     * @noinspection checkstyle:MagicNumber
     */
    public void setUpLanguages() {
        final double height = 15;
        final double width = 30;
        selectLangBox.getItems().forEach(lang -> System.out.println(
                "Language: " + lang.getAbbr()));
        selectLangBox.getItems()
                .setAll(LanguageOptions.getInstance().getLanguages());
        selectLangBox.setValue(selectLangBox.getItems().getFirst());
        /* How to do this gotten from stack overflow
        (https://stackoverflow.com/questions/32334
        137/javafx-choicebox-with-image-and-text)
         */
        selectLangBox.setCellFactory(
                new Callback<>() {
                    @Override
                    public ListCell<Language> call(
                            final ListView<Language> listView) {
                        return new ListCell<>() {
                            @Override
                            protected void updateItem(
                                    final Language item, final boolean empty) {
                                super.updateItem(item, empty);
                                if (empty || item == null) {
                                    setText(null);
                                    setGraphic(null);
                                } else {
                                    String iconPath = item.getImg_path();
                                    Image icon = new Image(Objects
                                            .requireNonNull(getClass()
                                                    .getClassLoader()
                                                    .getResourceAsStream(
                                                            iconPath)));
                                    ImageView iconImageView = new ImageView(
                                            icon);
                                    iconImageView.setFitHeight(height);
                                    iconImageView.setFitWidth(width);
                                    iconImageView.setPreserveRatio(false);
                                    setGraphic(iconImageView);

                                }
                            }
                        };
                    }
                });

        selectLangBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(
                    final Language item, final boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    //path described from client location
                    String iconPath = item.getImg_path();
                    Image icon = new Image(Objects.requireNonNull(
                            getClass()
                                    .getClassLoader()
                                    .getResourceAsStream(iconPath)));
                    ImageView iconImageView = new ImageView(icon);
                    iconImageView.setFitHeight(height);
                    iconImageView.setFitWidth(width);
                    iconImageView.setPreserveRatio(false);
                    setGraphic(iconImageView); // only shows the flag
                }
            }
        });

        selectLangBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(final Language language) {
                return language.getAbbr();
            }

            @Override
            public Language fromString(final String s) {
                Language lang;
                for (int i = 0; i < selectLangBox.getItems().size(); i++) {
                    if (selectLangBox.getItems().get(i).getAbbr().equals(s)) {
                        lang = selectLangBox.getItems().get(i);
                        return lang;
                    }
                }
                return selectLangBox.getItems().getFirst();
            }
        });

        selectLangBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldLang, newLang) -> {
                    if (!newLang.equals(oldLang)) {
                        System.out.println(selectLangBox.getValue().getAbbr());
                        locale = switch (selectLangBox.getValue().getAbbr()) {
                            case "ES" -> Locale.of("es", "ES");
                            case "NL" -> Locale.of("nl", "NL");
                            case "ZZ" -> Locale.of("zz", "ZZ");
                            default -> Locale.of("en", "US");
                        };
                        bundle = ResourceBundle.getBundle("MyBundle", locale);

                        editCollectionsB.setText(bundle.
                                getString("edit_collection"));
                        searchCollectionF.setPromptText(bundle
                                .getString("Search"));
                        searchNoteF.setPromptText(bundle.getString("Search"));
                        collectionText.setText(bundle.getString("Collection"));
                        languageText.setText(bundle.getString("Language"));
                        noteTitleF.setPromptText(bundle.getString("Untitled"));
                        noteBodyF.setPromptText(bundle.getString("Text_Area"));

                    }
                });

    }

    /**
     * Obtains the current collection. In case there is none, it creates one.
     */
    public void setUpCollections() {
        selectCollectionBox.getItems().setAll(
                new Collection(currentServer, "Default")
                //todo - add all collections
        );

        selectCollectionBox.setValue(selectCollectionBox.getItems().getFirst());
        selectCollectionBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(final Collection collection) {
                return collection.getCollectionTitle();
            }

            @Override
            public Collection fromString(final String s) {
                return null;
            }
        });

        selectCollectionBox.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldCollection, newCollection) -> {
                    if (!newCollection.equals(oldCollection)) {
                        System.out.println(selectCollectionBox
                                .getValue()
                                .getCollectionTitle());
                    }
                });
    }

    /**
     * Highlights the previous match of the note search.
     */
    public void prevMatch() {
        if (noteMatchIndices == null || noteMatchIndices.isEmpty()) {
            System.out.println("No text been searched");
        } else if (noteMatchIndices.getFirst() == -1) {
            System.out.println("No matches; no previous instance");
        } else if (currentSearchIndex > 0) {
            currentSearchIndex--;
            searchNote();
        } else {
            currentSearchIndex = Math.toIntExact(noteMatchIndices.size() - 1);
            searchNote();
        }
    }
    /**
     * Highlights the next match of the note search.
     */
    public void nextMatch() {
        if (noteMatchIndices == null || noteMatchIndices.isEmpty()) {
            System.out.println("No text been searched");
        } else if (noteMatchIndices.getFirst() == -1) {
            System.out.println("No matches; no next instance");
        } else if (currentSearchIndex < noteMatchIndices.size() - 1) {
            currentSearchIndex++;
            searchNote();
        } else {
            currentSearchIndex = 0;
            searchNote();
        }
    }
}


