package client.scenes;

import client.HomeScreen;
import client.utils.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import com.fasterxml.jackson.databind.ObjectMapper;
import commons.Collection;
import commons.Language;
import commons.LanguageOptions;
import commons.Note;
import commons.Server;
import commons.Tag;
import commons.Images;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Pair;
import javafx.util.StringConverter;
import netscape.javascript.JSObject;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import server.database.TagRepository;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private ScreenCtrl sc;

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
     *
     * @param localPrimaryStage - main stage to be used
     * @param home              - home screen controller
     * @param editCollection    - edit collections controller
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
     * Sets the ScreenCtrl instance for managing scene transitions.
     * @param screenCtrl - the ScreenCtrl instance to be assigned
     */
    public void setScreenCtrl(ScreenCtrl screenCtrl) {
        this.sc = screenCtrl;
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
    public void showEditCollection() { //TODO: should this be used? if yes, call inside: initialise() from EditCollectionsViewCtrl
        if (primaryStage == null) {
            throw new IllegalStateException("Primary stage is not initialized");
        }
        primaryStage.setScene(editCollectionScene);
    }


    /**
     * Constructor for the home screen controller.
     * @param localScene       - scene used
     * @param localServerUtils - server to be used
     */
    @Inject
    public HomeScreenCtrl(
            final ScreenCtrl localScene, final ServerUtils localServerUtils) {
        this.sc = localScene;
        this.serverUtils = localServerUtils;
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
    private ArrayList<Long> noteMatchIndices;

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
     * List of filtered tags
     */
    @FXML
    private HBox selectedTagsContainer;

    @FXML
    private ScrollPane tagsScrollPane;

    @FXML
    private ScrollBar horizontalScrollBar;

    /**
     * current server being used.
     */
    public final Server currentServer = new Server();

    /**
     * Current collection. If just the program for the first time
     * makes a default collection.
     */
    public Collection default_collection = new Collection(
            currentServer, "Default", "default");
    public Collection currentCollection = default_collection;


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
    private ObservableList<Note> notes = FXCollections
            .observableArrayList();
    /**
     * Button that allows for selection of images
     */
    public Button uploadImageB;
    /**
     * Provides list of uploaded images on bottom of the screen
     */
    public ListView<String> imageListView;
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
    public void initialize() throws IOException {
        final int period = 5;
        keyboardShortcuts();
        arrowKeyShortcuts();
        scheduler.scheduleAtFixedRate(
                this::syncIfChanged, 0, period, TimeUnit.SECONDS);
        setUpLanguages();
        loadSavedLanguageChoice();
        loadCollectionsFromServer();
        setUpCollections();
        markDownTitle();
        markDownContent();
        loadNotesFromServer();
        setupNotesListView();
        handleTitleEdits();
        prevMatch();
        nextMatch();
        enableJavaScript();
        scrollBarInitialize();
        configureScrollPane();
        configureTextFiltering();
        Platform.runLater(this::refresh); // Ensure tags and notes sync correctly
    }

    private void configureTextFiltering() {
        // Placeholder Text for "Filter by tag..."
        Text placeholderText = new Text("Filter by tag...");
        placeholderText.setStyle("-fx-fill: lightgray; -fx-font-size: 12; -fx-font-style: italic;");

        // Add the placeholder text to the container by default
        selectedTagsContainer.getChildren().add(placeholderText);
    }

    private void scrollBarInitialize() {
        Platform.runLater(() -> {
            // Set initial ScrollBar range
            horizontalScrollBar.setMin(0);
            horizontalScrollBar.setMax(1); // ScrollBar value ranges from 0 to 1 (normalized)

            // Update the ScrollBar's visibility and size dynamically
            tagsScrollPane.viewportBoundsProperty().addListener((obs, oldBounds, newBounds) -> updateScrollBar());
            selectedTagsContainer.boundsInParentProperty().addListener((obs, oldBounds, newBounds) -> updateScrollBar());

            // Bind ScrollPane's hvalue to ScrollBar's value
            horizontalScrollBar.valueProperty().addListener((obs, oldVal, newVal) ->
                    tagsScrollPane.setHvalue(newVal.doubleValue())
            );

            // Bind ScrollBar's value to ScrollPane's hvalue
            tagsScrollPane.hvalueProperty().addListener((obs, oldVal, newVal) ->
                    horizontalScrollBar.setValue(newVal.doubleValue())
            );

            // Ensure scrollbar state is correct at initialization
            updateScrollBar();
        });
    }

    private void updateScrollBar() {
        double contentWidth = selectedTagsContainer.getBoundsInParent().getWidth();
        double viewportWidth = tagsScrollPane.getViewportBounds().getWidth();

        if (contentWidth <= viewportWidth || contentWidth == 0) {
            // No scrolling needed: ScrollBar spans the full width and is disabled
            horizontalScrollBar.setVisibleAmount(1.0); // Full size
            horizontalScrollBar.setDisable(true); // Disable interaction
            horizontalScrollBar.setValue(0); // Reset position
        } else {
            // Scrolling needed: Adjust ScrollBar size based on visible area
            double visibleRatio = viewportWidth / contentWidth;
            horizontalScrollBar.setVisibleAmount(visibleRatio);
            horizontalScrollBar.setDisable(false); // Enable interaction
        }

        // Update ScrollBar range to stay in sync
        horizontalScrollBar.setMax(1.0); // Normalized to ScrollPane hvalue range (0 to 1)
    }

    private void configureScrollPane() {
        // Ensure that the ScrollPane allows horizontal scrolling based on content width
        selectedTagsContainer.setPrefWidth(Region.USE_COMPUTED_SIZE);
        selectedTagsContainer.setMinWidth(Region.USE_PREF_SIZE);
        selectedTagsContainer.setMaxWidth(Region.USE_COMPUTED_SIZE);

        tagsScrollPane.setFitToHeight(true); // Ensure vertical fit
        tagsScrollPane.setFitToWidth(false); // Disable automatic horizontal fit

        // Intercept scroll events to allow only horizontal scrolling
        tagsScrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            // Calculate horizontal scroll delta
            double delta = event.getDeltaY(); // Mouse wheel scroll
            double newHValue = tagsScrollPane.getHvalue() - delta / tagsScrollPane.getWidth();

            // Constrain newHValue to valid range [0, 1]
            newHValue = Math.max(0, Math.min(1, newHValue));
            tagsScrollPane.setHvalue(newHValue);

            // Consume the event to disable vertical scrolling
            event.consume();
        });
    }



    /**
     * Enable JavaScript in the WebView and bind Java methods to JavaScript
     */
    private void enableJavaScript() {
        // Enable JavaScript in the WebView
        markDownOutput.getEngine().setJavaScriptEnabled(true);

        // Bind Java methods to JavaScript
        markDownOutput.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                // Rebind `javaApp` after the WebView finishes loading
                JSObject window = (JSObject) markDownOutput.getEngine().executeScript("window");
                window.setMember("javaApp", this);
            }
        });
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
                        syncNoteWithServer(currentNote);
                        //updateMarkdownView();
                    }
                });

        noteTitleF
                .textProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (!newValue.equals(originalTitle)) {
                        isTitleEditInProgress = true; // Title is being edited
                        currentNote.setTitle(newValue.trim());
                    }
                });
    }

    /**
     * Obtains the current collection. In case there is none, it creates one.
     */
    public void setUpCollections() {
        ObservableList<Collection> collectionOptions = FXCollections.observableArrayList();
        collectionOptions.add(default_collection);
        collectionOptions.add(new Collection(currentServer, "All", "all"));
        collectionOptions.addAll(currentServer.getCollections());

        selectCollectionBox.setItems(collectionOptions);

        selectCollectionBox.setValue(collectionOptions.get(0)); //auto-set to the Default collection

        // Setting up the converter for displaying collection titles
        selectCollectionBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Collection collection){
                return collection.getCollectionTitle();
            }

            @Override
            public Collection fromString(String s) {
                return null;
            }
        });

        selectCollectionBox.getSelectionModel().selectedItemProperty().addListener((obs, oldCollection, newCollection) -> {
            if (!newCollection.equals(oldCollection)) {
                updateNotesList(newCollection);
                System.out.println("\nShow " + selectCollectionBox.getValue().getCollectionTitle()); //testing

                // Update current collection based on selection
                if (newCollection.getCollectionTitle().equals("All")) {
                    currentCollection = default_collection; // Resetting to Default
                } else {
                    currentCollection = newCollection; // Setting to the selected collection
                }

                System.out.println("\nCurrent collection: " + currentCollection.getCollectionTitle());
            }
        });

    }

    /**
     * Syncs a specific collection with the server to ensure consistency
     */
    private void syncCollectionWithServer(Collection collection) {
        try {
            // Serializing the collection to JSON
            String json = new ObjectMapper().writeValueAsString(collection);
            System.out.println("Serialized JSON for collection: " + json); // For testing

            // Creating a PUT request to update the specific collection
            var requestBody = Entity.entity(json, MediaType.APPLICATION_JSON);
            var response = ClientBuilder.newClient()
                    .target("http://localhost:8080/api/collections/update/" + collection.getCollectionId())
                    .request(MediaType.APPLICATION_JSON)
                    .put(requestBody);

            System.out.println("Response Status: " + response.getStatus()); // For testing

            if (response.getStatus() == 200) {
                // Parsing the server's response into a Collection object
                String updatedCollectionJson = response.readEntity(String.class);
                System.out.println("Updated collection received from server: " + updatedCollectionJson); // For testing

                ObjectMapper mapper = new ObjectMapper();
                Collection updatedCollection = mapper.readValue(updatedCollectionJson, Collection.class);

                // Replacing the collection in place to maintain order
                for (int i = 0; i < currentServer.getCollections().size(); i++) {
                    if (currentServer.getCollections().get(i).getCollectionId() == updatedCollection.getCollectionId()) {
                        currentServer.getCollections().set(i, updatedCollection);
                        break;
                    }
                }

                // Refreshing the UI collections
                Platform.runLater(() -> {
                    setUpCollections();
                });

            } else {
                System.err.println("Failed to sync collection. Status code: " + response.getStatus());
            }
        } catch (Exception e) {
            System.err.println("Error syncing collection with the server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Updates "notes" based on the chosen collection in selectCollectionBox
     * @param selectedCollection (chosen collection)
     */
    private void updateNotesList(Collection selectedCollection) {
        if (selectedCollection.getCollectionTitle().equals("All")) {
            notes.setAll(currentServer.getCollections().stream()
                    .flatMap(collection -> collection.getNotes().stream())
                    .toList());
        } else {
            notes.setAll(selectedCollection.getNotes());
        }
    }



    /**
     * Fetches collections from the server and stores them locally
     */
    public void loadCollectionsFromServer() {
        try {
            // Fetch collections from the server
            var response = ClientBuilder.newClient()
                    .target("http://localhost:8080/api/collections/fetch")
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            if (response.getStatus() == 200) {
                // Parse the JSON response into a List of Collection objects
                String json = response.readEntity(String.class);
                ObjectMapper mapper = new ObjectMapper();
                List<Collection> fetchedCollections = mapper.readValue(json,
                        mapper.getTypeFactory().constructCollectionType(List.class, Collection.class));

                // Update the current server's collections
                currentServer.getCollections().clear(); // Clear existing collections
                currentServer.getCollections().addAll(fetchedCollections);

                System.out.println("Collections loaded successfully from the server.");
            } else {
                System.err.println("Failed to fetch collections. Error code: " + response.getStatus());
            }
        } catch (Exception e) {
            System.err.println("Error loading the collections: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sends a request to the server to delete a collection by a provided ID.
     * @param collectionId - ID of the collection to be deleted
     */
    public static void deleteCollectionFromServer(long collectionId) {
        Response response = ClientBuilder.newClient()
                // Endpoint for deletion
                .target("http://localhost:8080/api/collections/delete/" + collectionId)
                .request()
                .delete();
        if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
            System.out.println("Collection successfully deleted.");
        } else if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
            System.out.println("Collection not found.");
        } else {
            System.out.println("Failed to delete collection. Status: " + response.getStatus());
        }
        response.close();
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
     *
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
     *
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
     *
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
        if (event.getCode() == KeyCode.ESCAPE) {
            if (searchCollectionF.isFocused()) {
                notesListView.requestFocus();
                event.consume();
            } else {
                searchCollectionF.requestFocus();
                event.consume();
            }
        }
        // Ctrl + F to search in a note
        if (event.isControlDown()
                && event.getCode() == KeyCode.F) {
            searchNoteF.requestFocus();
            event.consume();
        }
    }

    /**
     * This method is calling the keyboard shortcuts for utilities,
     * such as undo, refresh, tags, etc.
     *
     * @param event - the key being pressed
     */
    private void handleUtilityShortcuts(KeyEvent event) {
        // Ctrl + Z for undo
        if (event.isControlDown()
                && event.getCode() == KeyCode.Z) {
            undo();
            event.consume();
        }
        // F5 for refresh
        if (event.getCode() == KeyCode.F5) {
            refresh();
            event.consume();
        }
        // Shift + T to open up the tags edit
        if (event.isShiftDown()
                && event.getCode() == KeyCode.T) {
            // todo - the way tags are handeled
            event.consume();
        }
        // Shift + E to open up edit collections
        if (event.isShiftDown() && event.getCode() == KeyCode.E) {
            editCollections();
            event.consume();
        }
    }

    /**
     * This method is calling the keyboard shortcuts for focus,
     * so to open collection choice box and language combo box
     *
     * @param event - the key being pressed
     */
    private void handleChoiceShortcuts(KeyEvent event) {
        // Shift + L to open up language combo box
        if (event.isShiftDown()
                && event.getCode() == KeyCode.L) {
            selectLangBox.requestFocus();
            selectLangBox.show();
            event.consume();
        }
        // Shift + C to open up collection choice box
        if (event.isShiftDown()
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
            if (currentNote != null &&
                    !isTitleEditInProgress && // Prevent syncing if title is being edited
                    (!currentNote.getBody().equals(lastSyncedBody) ||
                            !currentNote.getTitle().equals(lastSyncedTitle))) {

                // Sync with the server if there is a change in title or body
                syncNoteWithServer(currentNote);

                // Update the last synced title and body
                lastSyncedTitle = currentNote.getTitle();
                lastSyncedBody = currentNote.getBody();

                System.out.println("Note synced with the server at: " + java.time.LocalTime.now());
            }
        });
    }

    /**
     * This method ensures the syncing with the server (database).
     *
     * @param note - note provided - in syncIfChanged method to be specific
     */
    public void syncNoteWithServer(Note note) {
        try {
            String json = new ObjectMapper().writeValueAsString(note);
            var requestBody = Entity.entity(json, MediaType.APPLICATION_JSON);

            try (var response = ClientBuilder.newClient()
                    .target("http://localhost:8080/api/notes/update")
                    .request(MediaType.APPLICATION_JSON)
                    .put(requestBody)) {

                if (response.getStatus() == 200) {
                    // Fetch the updated note from the server
                    Note updatedNote = fetchNoteById(note.getNoteId());
                    if (updatedNote != null) {
                        currentNote = updatedNote;
                        refreshNotesInListView(currentNote);
                    }
                } else {
                    System.err.println("Failed to update note on server. Status code: " + response.getStatus());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void refreshNotesInListView(Note updatedNote) {
        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).getNoteId() == updatedNote.getNoteId()) {
                notes.set(i, updatedNote); // Replace with updated note
                break;
            }
        }
        notesListView.refresh(); // Refresh UI display
        //updateMarkdownView();

        // Also update filtered list to be sure
        ObservableList<Note> filteredNotes = notesListView.getItems();
        for (int i = 0; i < filteredNotes.size(); i++) {
            if (filteredNotes.get(i).getNoteId() == updatedNote.getNoteId()) {
                filteredNotes.set(i, updatedNote);
                break;
            }
        }
    }

    private void updateMarkdownView() {
        String titleHtml = "<h1>" + renderer.render(parser.parse(currentNote.getTitle())) + "</h1>";
        String bodyHtml = renderer.render(parser.parse(currentNote.getBody()));
        markDownOutput.getEngine().loadContent(titleHtml + bodyHtml);
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
            var response = ClientBuilder.newClient()
                    .target("http://localhost:8080/api/notes/fetch")
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            if (response.getStatus() == 200) {
                String json = response.readEntity(String.class);

                ObjectMapper mapper = new ObjectMapper();
                List<Note> fetchedNotes = mapper.readValue(
                        json,
                        mapper.getTypeFactory().constructCollectionType(List.class, Note.class)
                );

                notes.clear();
                notes.addAll(fetchedNotes);
                System.out.println("Deserialized Notes: " + notes);
            } else {
                System.err.println("Failed to fetch notes. Status: " + response.getStatus());
            }
        } catch (Exception e) {
            errorLogger.log(Level.SEVERE, "Error loading notes: " + e.getMessage(), e);
        }
    }


    /**
     * Sets up the ListView in the front-end
     */
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
                        loadImagesForCurrentNote();
                        //updateMarkdownView(); // Refresh the Markdown display
                        Platform.runLater(() -> notesListView.getSelectionModel());
                    }
                });
    }

    /**
     * This method adds the listener to the title field. It automatically
     * converts the content to a heading of type h1, because it is a title
     */
    public void markDownTitle() {
        noteTitleF.textProperty().addListener((observable, oldValue, newValue) -> {
            currentNote.setTitle(newValue);

            // Process #tags and [[notes]] in the body
            String processedContent = processTagsAndReferences(noteBodyF.getText());

            // Convert the title and body to HTML
            String showTitle = "<h1>"
                    + renderer.render(parser.parse(newValue))
                    + "</h1>";
            String showContent = renderer.render(parser.parse(processedContent));

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
        noteBodyF.textProperty().addListener((observable, oldValue, newValue) -> {
            currentNote.setBody(newValue);

            // Send the updated note to the server
            try {
                syncNoteWithServer(currentNote);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Note (MD): " + currentNote.getNoteId() + currentNote.getTags().toString());

            // Process #tags to make them clickable
            String processedContent = newValue.replaceAll("#(\\w+)",
                    "<button style=\"background-color: #e43e38; color: white; border: none; padding: 2px 6px; border-radius: 4px; cursor: pointer;\" " +
                            "onclick=\"javaApp.filterByTag('#$1')\">#$1</button>");

            // Process [[Note]] references
            Matcher matcher = Pattern.compile("\\[\\[(.*?)\\]\\]").matcher(processedContent);
            StringBuffer result = new StringBuffer();

            while (matcher.find()) {
                String title = matcher.group(1);
                boolean noteExists = notes.stream().anyMatch(note -> note.getTitle().equals(title));
                String replacement;

                if (noteExists) {
                    replacement = "<a href=\"#\" style=\"color: blue; text-decoration: underline;\" onclick=\"javaApp.openNoteByTitle('" + title.replace("'", "\\'") + "')\">" + title + "</a>";
                } else {
                    replacement = "<span style=\"color: red; font-style: italic;\">" + title + "</span>";
                }

                matcher.appendReplacement(result, replacement);
            }
            matcher.appendTail(result);

            // Convert the title and body to HTML
            String showTitle = "<h1>"
                    + renderer.render(parser.parse(noteTitleF.getText()))
                    + "</h1>";
            String showContent = renderer.render(parser.parse(result.toString()));

            // Load the combined title and content into the WebView
            String titleAndContent = showTitle + showContent;
            markDownOutput.getEngine().loadContent(titleAndContent);
        });
    }

    private void refreshTagsDisplay() {
        Platform.runLater(() -> {
            selectedTagsContainer.getChildren().clear();
            for (String tag : getAllTags()) {
                ChoiceBox<String> tagBox = new ChoiceBox<>();
                tagBox.setValue(tag);
                selectedTagsContainer.getChildren().add(tagBox);
            }
            togglePlaceholderText(); // Handle placeholder visibility.
        });
    }


    @FXML
    public void filterByTag(String tag) {
        Platform.runLater(() -> {
            // Clean the tag (remove # if present)
            String cleanTag = tag.startsWith("#") ? tag.substring(1) : tag;

            // Check if the tag already exists in the filtering box
            boolean tagExists = selectedTagsContainer.getChildren().stream()
                    .filter(node -> node instanceof ChoiceBox)
                    .map(node -> ((ChoiceBox<String>) node).getValue())
                    .anyMatch(existingTag -> existingTag.equals(cleanTag));

            if (!tagExists) {
                // Create the ChoiceBox for the new tag
                ChoiceBox<String> tagChoiceBox = new ChoiceBox<>();
                tagChoiceBox.setValue(cleanTag);

                // Store the original value to revert if the selection is canceled
                final String[] originalValue = {cleanTag};

                // Refresh the available tags when the ChoiceBox is opened
                tagChoiceBox.setOnShowing(event -> refreshAvailableTags(tagChoiceBox));

                // Revert to the original value if the dropdown is closed without selecting anything
                tagChoiceBox.setOnHiding(event -> {
                    if (tagChoiceBox.getValue() == null) {
                        Platform.runLater(() -> tagChoiceBox.setValue(originalValue[0])); // Revert to the original value
                    }
                });

                // Validate the new tag combination when a tag is selected
                tagChoiceBox.getSelectionModel().selectedItemProperty().addListener((obs, oldTag, newTag) -> {
                    if (newTag != null) {
                        Set<String> selectedTags = collectSelectedTags(newTag, oldTag);

                        if (isCombinationValid(selectedTags)) {
                            // Valid tag: adjust UI and apply filtering
                            adjustChoiceBoxWidth(tagChoiceBox, newTag);
                            filterNotesByTags();
                            originalValue[0] = newTag; // Update the original value
                        } else {
                            // Invalid tag: show alert and revert to the previous tag
                            showAlert("Invalid Tag Combination",
                                    "No notes match the selected tag combination. Please try again.");
                            Platform.runLater(() -> tagChoiceBox.setValue(originalValue[0])); // Revert selection
                        }
                    }
                });

                adjustChoiceBoxWidth(tagChoiceBox, cleanTag);

                // Remove placeholder text when tags are added
                togglePlaceholderText();

                // Add the new ChoiceBox to the UI
                selectedTagsContainer.getChildren().add(tagChoiceBox);

                // Apply filtering with the initial tag
                filterNotesByTags();
            }
        });
    }


    private Set<String> collectSelectedTags(String newTag, String oldTag) {
        // Collect currently selected tags
        Set<String> selectedTags = selectedTagsContainer.getChildren().stream()
                .filter(node -> node instanceof ChoiceBox)
                .map(node -> ((ChoiceBox<String>) node).getValue())
                .collect(Collectors.toSet());

        // Replace old tag with the new one in the tag set
        if (oldTag != null) selectedTags.remove(oldTag);
        if (newTag != null) selectedTags.add(newTag);

        return selectedTags;
    }

    private void refreshAvailableTags(ChoiceBox<String> choiceBox) {
        // Get all tags except the ones already selected in other ChoiceBoxes
        Set<String> selectedTags = selectedTagsContainer.getChildren().stream()
                .filter(node -> node instanceof ChoiceBox)
                .map(node -> ((ChoiceBox<String>) node).getValue())
                .collect(Collectors.toSet());

        List<String> availableTags = getAllTags().stream()
                .filter(tagName -> !selectedTags.contains(tagName))
                .collect(Collectors.toList());

        // Update the available options in the ChoiceBox
        choiceBox.setItems(FXCollections.observableArrayList(availableTags));
    }



    private void togglePlaceholderText() {
        Platform.runLater(() -> {
            boolean hasTags = selectedTagsContainer.getChildren().stream()
                    .anyMatch(node -> node instanceof ChoiceBox);

            if (!hasTags) {
                // Show placeholder text if no tags are present
                if (selectedTagsContainer.getChildren().stream().noneMatch(node -> node instanceof Text)) {
                    Text placeholderText = new Text("Filter by tag...");
                    placeholderText.setStyle("-fx-fill: lightgray; -fx-font-size: 12; -fx-font-style: italic;");
                    selectedTagsContainer.getChildren().add(placeholderText);
                }
            } else {
                // Remove placeholder text if tags are present
                selectedTagsContainer.getChildren().removeIf(node -> node instanceof Text);
            }
        });
    }

    private void adjustChoiceBoxWidth(ChoiceBox<String> choiceBox, String tag) {
        // Calculate the approximate text width for the tag
        double textWidth = computeTextWidth(tag, 12); // 12 is the font size

        // Define the maximum allowable width
        double maxWidth = 150; // Adjust this value as needed
        double padding = 30; // Space for dropdown arrow

        // Set the ChoiceBox width to the smaller of the calculated width or maxWidth
        choiceBox.setPrefWidth(Math.min(textWidth + padding, maxWidth));
    }

    private double computeTextWidth(String text, int fontSize) {
        // Create a temporary Text node to calculate the width
        Text tempText = new Text(text);
        tempText.setStyle("-fx-font-size: " + fontSize + "px;"); // Set the font size

        // Use a dummy scene to accurately calculate the layout bounds
        new Scene(new Group(tempText));
        return tempText.getLayoutBounds().getWidth();
    }

    private List<String> getAllTags() {
        return notes.stream()
                .flatMap(note -> note.getTags().stream())
                .map(Tag::getName)
                .distinct()
                .collect(Collectors.toList());
    }



    private void filterNotesByTags() {
        // Collect the currently selected tags
        Set<String> selectedTags = selectedTagsContainer.getChildren().stream()
                .filter(node -> node instanceof ChoiceBox)
                .map(node -> ((ChoiceBox<String>) node).getValue())
                .map(tag -> tag.startsWith("#") ? tag.substring(1) : tag)
                .collect(Collectors.toSet());

        System.out.println("Filtering by tags: " + selectedTags);

        // Filter notes using the selected tags
        ObservableList<Note> filteredNotes = FXCollections.observableArrayList(
                notes.stream()
                        .filter(note -> {
                            Set<String> noteTags = note.getTags().stream()
                                    .map(Tag::getName)
                                    .collect(Collectors.toSet());
                            return noteTags.containsAll(selectedTags);
                        })
                        .toList()
        );

        // Update the notes list view with the filtered notes
        notesListView.setItems(filteredNotes);
        System.out.println("Filtered notes: " + filteredNotes);

        // Update the current note selection
        if (!filteredNotes.contains(currentNote) && !filteredNotes.isEmpty()) {
            Note firstNote = filteredNotes.get(0);
            notesListView.getSelectionModel().select(firstNote);
            currentNote = firstNote;

            // Update UI fields to reflect the selected note
            noteTitleF.setText(firstNote.getTitle());
            noteBodyF.setText(firstNote.getBody());
            loadImagesForCurrentNote();
        }

        syncFilteredNotes(filteredNotes);
    }

    private void syncFilteredNotes(ObservableList<Note> filteredNotes) {
        for (Note note : filteredNotes) {
            int index = notes.indexOf(note);
            if (index != -1) {
                notes.set(index, note); // Sync changes back to the original list
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private boolean isCombinationValid(Set<String> selectedTags) {
        // Check if there are any notes matching the selected tags
        return notes.stream()
                .anyMatch(note -> {
                    Set<String> noteTags = note.getTags().stream()
                            .map(Tag::getName)
                            .collect(Collectors.toSet());
                    return noteTags.containsAll(selectedTags);
                });
    }

    public void refresh() {
        Platform.runLater(() -> {
            System.out.println("Refreshing notes and tags...");
            try {
                // Fetch updated notes from the server
                loadNotesFromServer();

                // Fetch updated collections (optional, if needed)
                loadCollectionsFromServer();

                // Refresh tags (if tag list UI is separate, ensure it's updated too)
                selectedTagsContainer.getChildren().clear();
                togglePlaceholderText();

                // Update the notes list view
                notesListView.setItems(FXCollections.observableArrayList(notes));

                System.out.println("Refresh completed.");
            } catch (Exception e) {
                errorLogger.log(Level.SEVERE, "Error during refresh: " + e.getMessage(), e);
            }
        });
    }

    @FXML
    private void clearTags() {
        // Check if there are any tags in the filter box
        boolean hasTags = selectedTagsContainer.getChildren().stream()
                .anyMatch(node -> node instanceof ChoiceBox);

        if (!hasTags) {
            // Show alert if no tags are present
            showAlert("No Tags to Clear", "There are no tags in the filter box to clear.");
            return;
        }

        Platform.runLater(() -> {
            selectedTagsContainer.getChildren().clear(); // Clear selected tags
            togglePlaceholderText(); // Show placeholder text
            notesListView.setItems(FXCollections.observableArrayList(notes)); // Reset to full notes list
            System.out.println("All tags cleared and notes list reset.");
        });
    }


    /**
     * Find the referenced note and open it
     * @param title - the title of the referenced note
     */
    @FXML
    public void openNoteByTitle(String title) {
        // Find the target note by its title
        Note targetNote = notesListView.getItems().stream()
                .filter(note -> note.getTitle().equals(title))
                .findFirst()
                .orElse(null);

        if (targetNote != null) {
            // Update the current note
            currentNote = targetNote;

            // Update the UI fields
            Platform.runLater(() -> {
                noteTitleF.setText(targetNote.getTitle());
                noteBodyF.setText(targetNote.getBody());
                notesListView.getSelectionModel().select(targetNote);
            });

            System.out.println("Switched to note: " + targetNote.getTitle());
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Referenced Note Not Found");
            alert.setHeaderText("Cannot Open Referenced Note");
            alert.setContentText("The referenced note \"" + title + "\" is not in the current filtered list.");
            alert.showAndWait();
        }
    }


    /**
     * Update all the references from notes when the title of the referenced
     * note was changed
     * @param oldTitle - the original title
     * @param newTitle - the new title
     */
    private void updateReferencesInNotes(String oldTitle, String newTitle) {
        for (Note note : notes) {
            if (!note.equals(currentNote)) { // Skip the note being renamed
                String updatedBody = note.getBody().replaceAll(
                        "\\[\\[" + Pattern.quote(oldTitle) + "\\]\\]",
                        "[[" + newTitle + "]]"
                );
                if (!updatedBody.equals(note.getBody())) {
                    note.setBody(updatedBody);
                    syncNoteWithServer(note);
                    refreshNotesInListView(note); // Update the ListView display
                }
            }
        }
    }

    /**
     * Utility function used to locate resources within applications filepath.
     *
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
     *
     * @param newNote - note to be added
     * @throws IOException - exception that will occur if server error
     */
    public void addCommand(final Note newNote) throws IOException {
        Note savedNote = saveNoteToServer(newNote);

        currentCollection.addNote(savedNote);  // Add to the collection
        System.out.println("New note added to collection: " + currentCollection.getCollectionTitle()); //testing
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
     *
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
                Note addedNote = response.readEntity(Note.class);
                note.setNoteId(addedNote.getNoteId());
                return addedNote;
            } else {
                throw new IOException(
                        "Server returned status: " + response.getStatus());
            }
        }
    }


    /**
     * Sends request to the server to add a note with a provided Note.
     *
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
     *
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
     *
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

        // Refresh UI fields to reflect the reverted state of the note
        // Currently for testing
        if (currentNote != null) {
            noteTitleF.setText(currentNote.getTitle()); // Update title field
            noteBodyF.setText(currentNote.getBody());// Update body field
            String title = "<h1>" + renderer.render(parser.parse(currentNote.getTitle())) + "</h1>";
            String titleAndContent = title + currentNote.getBody();
            markDownOutput.getEngine().loadContent(titleAndContent);
        }
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
                        Platform.runLater(() -> noteTitleF.setText(originalTitle));
                    }
                    else if (newTitle.isEmpty()) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Empty Title");
                        alert.setHeaderText("Title Cannot be Empty!");
                        alert.setContentText("Please enter a valid title!");
                        alert.showAndWait();

                        // Revert to the original title
                        Platform.runLater(() -> noteTitleF.setText(originalTitle));
                    }
                    else {
                        // If not duplicate, update title and sync with the server (Invoke command for editing title)
                        Command editTitleCommand = new EditTitleCommand( currentNote,originalTitle, newTitle,HomeScreenCtrl.this);
                        invoker.executeCommand(editTitleCommand);
                        // Notify the backend to update references
                        updateReferencesInNotes(originalTitle, newTitle);
                        originalTitle = newTitle;
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
     *
     * @param collectionId - id of the collection that is the note
     *                     associated with
     * @param newTitle     - the title to be checked
     * @return true if it is a duplicate, false if it is not
     * @throws IOException when it returns something else then 200/409 code
     */
    public boolean validateTitleWithServer(
            final Long collectionId, final String newTitle) throws IOException {
        return serverUtils.validateTitleWithServer(collectionId, newTitle);
    }

    /**
     * Fetches a specific note from the server by its ID.
     *
     * @param noteId The ID of the note to fetch
     * @return The fetched Note object or null if it was not found
     * or there was an error
     */
    private Note fetchNoteById(long noteId) {
        try {
            var response = ClientBuilder.newClient()
                    .target("http://localhost:8080/api/notes/" + noteId)
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            if (response.getStatus() == 200) {
                String json = response.readEntity(String.class);
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(json, Note.class);
            } else {
                System.err.println("Failed to fetch note with ID " + noteId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
        // Process #tags and [[notes]] after highlighting
        String processedBody = processTagsAndReferences(bodyHighlighted);

        titleHighlighted = "<h1>"
                + renderer.render(parser.parse(titleHighlighted))
                + "</h1>";
        bodyHighlighted = renderer.render(parser.parse(bodyHighlighted));
        String totalContent = titleHighlighted + bodyHighlighted;
        markDownOutput.getEngine().loadContent(totalContent);


    }

    /**
     * Processes #tags and [[note]] references in the provided text.
     */
    private String processTagsAndReferences(String text) {
        // Process #tags
        String processedContent = text.replaceAll("#(\\w+)",
                "<button style=\"background-color: #e43e38; color: white; border: none; padding: 2px 6px; border-radius: 4px; cursor: pointer;\" " +
                        "onclick=\"javaApp.filterByTag('#$1')\">#$1</button>");

        // Process [[note]] references
        Matcher matcher = Pattern.compile("\\[\\[(.*?)\\]\\]").matcher(processedContent);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String title = matcher.group(1);
            boolean noteExists = notes.stream().anyMatch(note -> note.getTitle().equals(title));
            String replacement;

            if (noteExists) {
                replacement = "<a href=\"#\" style=\"color: blue; text-decoration: underline;\" onclick=\"javaApp.openNoteByTitle('" + title.replace("'", "\\'") + "')\">" + title + "</a>";
            } else {
                replacement = "<span style=\"color: red; font-style: italic;\">" + title + "</span>";
            }

            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        return result.toString();
    }



    /**
     * Searches through the notes and respects the current filters, including tags.
     */
    public void searchCollection() {
        String searchText = searchCollectionF.getText().trim().toLowerCase(); // Normalize search text

        // Determine the base list to filter (current filtered notes or all notes)
        ObservableList<Note> baseList = notesListView.getItems(); // Start with the currently displayed notes

        if (searchText.isEmpty()) {
            // If the search is cleared, reapply the tag filtering
            filterNotesByTags(); // Reapply tag filtering to refresh the view
            return;
        }

        // Filter the base list of notes based on the search text
        ObservableList<Note> filteredNotes = FXCollections.observableArrayList(
                baseList.stream()
                        .filter(note -> note.getTitle().toLowerCase().contains(searchText) ||
                                note.getBody().toLowerCase().contains(searchText))
                        .toList()
        );

        // Update the ListView with the filtered notes
        notesListView.setItems(filteredNotes);

        // Handle no matches
        if (filteredNotes.isEmpty()) {
            System.out.println("No matches found for: " + searchText);
        } else {
            // Optionally, select the first match
            notesListView.getSelectionModel().selectFirst();
        }
    }



    /**
     * Sets up the languages (adds them to the collection box).
     *
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
                        try {
                            saveLanguageChoice(newLang);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
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
     * This method saves the selected language to a config file
     * @param languageToSave - language to be saved
     */
    private void saveLanguageChoice(Language languageToSave) throws IOException {
        File languageFile = new File("language-choice.txt");
        if(!languageFile.exists()) {
            languageFile.createNewFile();
        }
        try(FileWriter fw = new FileWriter(languageFile)) {
            fw.write(languageToSave.getAbbr());
        }
    }

    /**
     * This method loads the language that is stored in the file
     * @throws IOException - when something messes up
     */
    private void loadSavedLanguageChoice() throws IOException {
        File languageFile = new File("language-choice.txt");
        if(languageFile.exists()) {
            try(BufferedReader br = new BufferedReader(new FileReader(languageFile))) {
                String savedLanguageAbbr = br.readLine();
                for(Language language : selectLangBox.getItems()) {
                    if(language.getAbbr().equals(savedLanguageAbbr)) {
                        selectLangBox.setValue(language);
                        locale = switch (language.getAbbr()) {
                            case "ES" -> Locale.of("es", "ES");
                            case "NL" -> Locale.of("nl", "NL");
                            case "ZZ" -> Locale.of("zz", "ZZ");
                            default -> Locale.of("en", "US");
                        };
                        break;
                    }
                }
            }
        }
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

    /**
     * Allows for the selection of a file, and displays the name in a list view
     */
    @FXML
    public void uploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select file");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(uploadImageB.getScene().getWindow());

        if (file != null) {
            try {
                byte[] imageData = Files.readAllBytes(file.toPath());

                Images image = new Images(
                        null,
                        file.getName(),
                        imageData,
                        currentNote
                );

                imageListView.getItems().add(file.getName());

                saveImageToServer(image);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
    }

    /**
     * Saves images to the server
     * @param image
     * @return Images object
     * @throws IOException
     */
    public Images saveImageToServer(final Images image) throws IOException {
        if (currentNote == null) {
            throw new IllegalStateException("Current note or note ID is not set");
        }

        // Convert the image object to JSON
        var json = new ObjectMapper().writeValueAsString(image);
        var requestBody = Entity.entity(json, MediaType.APPLICATION_JSON);

        // Construct the URL with the noteId
        String url = "http://localhost:8080/api/images/" + currentNote.getNoteId() + "/addImage";

        // Send a POST request to the server
        try (var response = ClientBuilder.newClient()
                .target(url)
                .request(MediaType.APPLICATION_JSON)
                .post(requestBody)) {

            if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                System.out.println("Image saved successfully");
                return response.readEntity(Images.class);
            } else if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                throw new IOException("Server returned 404: Note not found");
            } else {
                throw new IOException("Server returned status: " + response.getStatus());
            }
        }
    }

    /**
     * Fetches the notes images from the server
     * @return List of image names
     */
    public List<Images> fetchImagesForNote() {
        if (currentNote == null) {
            throw new IllegalStateException("Current note or note ID is not set");
        }

        String url = "http://localhost:8080/api/images/" + currentNote.getNoteId() + "/allImages";

        try (var response = ClientBuilder.newClient()
                .target(url)
                .request(MediaType.APPLICATION_JSON)
                .get()) {

            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                // Parse the JSON response
                String json = response.readEntity(String.class);
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(json,
                        mapper.getTypeFactory().constructCollectionType(List.class, Images.class));
            } else if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
                System.err.println("No images found for note: " + currentNote.getNoteId());
                return List.of(); // Return empty list
            } else {
                throw new IOException("Failed to fetch images. Server returned status: " + response.getStatus());
            }
        } catch (Exception e) {
            System.err.println("Error fetching images: " + e.getMessage());
            return List.of(); // Return empty list in case of failure
        }
    }

    /**
     * Loads the images from the server onto the note's listview
     */
    @FXML
    public void loadImagesForCurrentNote() {
        if (currentNote == null) {
            System.err.println("No note selected. Cannot load images.");
            return;
        }

        List<Images> images = fetchImagesForNote();
        Platform.runLater(() -> {
            imageListView.getItems().clear(); // Clear existing items
            for (Images image : images) {
                imageListView.getItems().add(image.getName());
            }
            System.out.println("Loaded " + images.size() + " images for note: " + currentNote.getTitle());
        });
    }
}


