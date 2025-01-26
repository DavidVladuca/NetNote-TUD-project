package client.scenes;

import client.HomeScreen;
import client.utils.*;
import com.google.inject.Inject;
import commons.Collection;
import commons.*;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.Pair;
import javafx.util.StringConverter;
import netscape.javascript.JSObject;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;
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
     *
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
    //TODO: should this be used? if yes, call inside: initialise() from EditCollectionsViewCtrl
    public void showEditCollection() {
        if (primaryStage == null) {
            throw new IllegalStateException("Primary stage is not initialized");
        }
        primaryStage.setScene(editCollectionScene);
    }


    /**
     * Constructor for the home screen controller.
     *
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

    private final String webviewCSSPath =
            getClass().getResource("/css/webview.css").toExternalForm();


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
     * Button for deleting a selected image
     */
    private Button deleteImageB;
    /**
     * Button for undo-ing last change.
     */
    private Button undoB;
    /**
     * searched through note.
     */
    private Button searchNoteB;
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
     * Downloads images
     */
    public Button downloadImageB;

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
    private static ResourceBundle bundle;
    /**
     * current locale.
     */
    private static Locale locale;
    /**
     * Language selection box in JavaFX.
     */
    public ComboBox<Language> selectLangBox = new ComboBox<>();

    /**
     * Boolean variable that keeps track,
     * whether the edits of the title and body are due to program or user change
     * Default is true, as the first selected note sets the text fields programmatically
     */
    boolean isProgrammaticChange = true;


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
     * Output showing actual note.
     */
    public WebView markDownOutput;
    /**
     * Selection box for the working collection.
     */
    public ComboBox<Collection> selectCollectionBox = new ComboBox<>();
    /**
     * Invoker for keeping history of commands and executing them.
     */
    private final CommandInvoker invoker = new CommandInvoker();

    /**
     * Button to show tags scene.
     */
    public Button clearTagsB;


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
            currentServer, "Default", "default", true);
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
     * The original body, prior to the change.
     */
    private String originalBody;
    /**
     * Boolean that determines whether body is in progress or not.
     */
    private boolean isBodyEditInProgress = false;
    /**
     * Title of note in last sync to server.
     */
    private String lastSyncedTitle = "";
    /**
     * Body of note in last sync to server.
     */
    private String lastSyncedBody = "";

    /**
     * String buffer for the title edit that has not been saved properly
     */
    String titleBuffer = "";

    /**
     * Boolean that indicated whether the buffered title shoud be restored
     */
    boolean shouldTitleBuffer = false;

    /**
     * Value that asserts if it's the first run in the session
     */
    private int ok = 1;

    /**
     * A queue that holds commands to be processed one at a time in a separate thread.
     * * This ensures commands are executed in the order they are added to the queue.
     */
    private final BlockingQueue<Command> commandQueue = new LinkedBlockingQueue<>();


    /**
     * This method initializes the controller
     * and sets up the listener for the text area that the user types in.
     * based on other methods it calls
     */
    @FXML
    public void initialize() throws IOException {
        final int period = 1;
        keyboardShortcuts();
        arrowKeyShortcuts();
        scheduler.scheduleAtFixedRate(
                this::syncIfChanged, 0, period, TimeUnit.SECONDS);
        setUpLanguages();
        loadSavedLanguageChoice();
        loadCollectionsFromServer();
        setUpCollections();
        noteTitleF.setDisable(true);
        noteBodyF.setDisable(true);
        markDownTitle();
        markDownContent();
        loadNotesFromServer();
        setupNotesListView();
        setupImageListView();
        handleTitleEdits();
        handleBodyEdits();
        handleNoteChanges();
        startCommandProcessor();
        prevMatch();
        nextMatch();
        enableJavaScript();
        scrollBarInitialize();
        configureScrollPane();
        configureTextFiltering();
        Platform.runLater(this::refresh); // Ensure tags and notes sync correctly
        Platform.runLater(this::updateUIAfterChange);
    }

    /**
     * Configures the placeholder text for the tag filter container.
     * Ensures placeholder visibility when no tags are selected.
     */
    private void configureTextFiltering() {
        // Placeholder Text for "Filter by tag..."
        bundle = ResourceBundle.getBundle("MyBundle", locale);
        String placeholderValue = bundle.getString("Filter_by_tag");

        Text placeholderText = new Text(placeholderValue);
        placeholderText.setStyle("-fx-fill: lightgray; -fx-font-size: 12; -fx-font-style: italic;");

        // Add the placeholder text to the container by default
        selectedTagsContainer.getChildren().add(placeholderText);
    }

    /**
     * Initializes the horizontal scroll bar for the tags display.
     * Links the ScrollPane and ScrollBar to ensure proper scrolling behavior.
     */
    private void scrollBarInitialize() {
        Platform.runLater(() -> {
            // Set initial ScrollBar range
            horizontalScrollBar.setMin(0);
            horizontalScrollBar.setMax(1); // ScrollBar value ranges from 0 to 1 (normalized)

            // Update the ScrollBar's visibility and size dynamically
            tagsScrollPane.viewportBoundsProperty().addListener((obs, oldBounds, newBounds)
                    -> updateScrollBar());
            selectedTagsContainer.boundsInParentProperty().addListener((obs, oldBounds, newBounds)
                    -> updateScrollBar());

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

    /**
     * Updates the visibility and size of the horizontal scroll bar
     * based on the content width and viewport size of the tags container.
     */
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

    /**
     * Configures the ScrollPane for horizontal scrolling
     * and disables vertical scrolling for the tags container.
     * Intercepts scroll events to handle horizontal scrolling behavior.
     */
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
        markDownOutput.getEngine().getLoadWorker().stateProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue == Worker.State.SUCCEEDED) {
                        // Rebind `javaApp` after the WebView finishes loading
                        JSObject window =
                                (JSObject) markDownOutput.getEngine().executeScript("window");
                        window.setMember("javaApp", this);
                    }
                });
    }

    /**
     * Method which handles upcoming edits in the title
     */
    private void handleTitleEdits() {
        // Listener for focus changes on the title field
        noteTitleF.focusedProperty().addListener((observable, oldValue, newValue) -> {

            if (newValue) { // Gaining focus
                isProgrammaticChange = false; // Indicate it is not user edit
                currentEditState = EditState.TITLE;// Set active edit state
                // For testing purposes
            } else { // Losing focus
                // Only finalize edits if this is a user-initiated focus loss
                if (!isProgrammaticChange && isTitleEditInProgress) {
                    titleEdit();// Save title changes
                    isTitleEditInProgress = false; // Reset the edit flag
                }

                currentEditState = EditState.NONE; // Reset state
            }
        });

        // Listener for text changes in the title field
        noteTitleF.textProperty().addListener((observable, oldValue, newValue) -> {
            // Only track changes if not programmatically triggered
            if (!isProgrammaticChange) {
                if (!newValue.equals
                        (notesListView.getSelectionModel().getSelectedItem().getTitle())) {
                    titleBuffer = newValue;
                }
                if (newValue == null || newValue.equals(originalTitle)) {
                    isTitleEditInProgress = false;
                } else {
                    isTitleEditInProgress = true;
                }
            }
        });
    }

    /**
     * Method which handles upcoming edits in the body
     */
    private void handleBodyEdits() {
        // Listener for focus changes on the body field
        noteBodyF.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                isProgrammaticChange = false;// Gaining focus
                currentEditState = EditState.BODY;// Set active edit state
            } else { // Losing focus
                // Finalize body edits if they are in progress
                if (isBodyEditInProgress) {
                    bodyEdit(); // Save body changes
                    isBodyEditInProgress = false; // Reset the edit flag
                }
                currentEditState = EditState.NONE; // Reset state
            }
        });

        // Listener for text changes in the body field
        noteBodyF.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.equals(originalBody)) {
                isBodyEditInProgress = false;
            } else {
                isBodyEditInProgress = true;
            }
        });
    }

    /**
     * Updates the fields to the newly selected note
     *
     * @param newNote - Note currently selected
     */
    public void setNewNoteFields(Note newNote) {
        if (newNote != null) {
            {
                // Update the fields with the new note's data
                originalTitle = newNote.getTitle();
                originalBody = newNote.getBody();
                noteTitleF.setText(originalTitle);
                noteBodyF.setText(originalBody);

                // Reset edit flags and state
                isTitleEditInProgress = false;
                isBodyEditInProgress = false;
                currentEditState = EditState.NONE;
            }
        }
    }

    /**
     * Handles changing the currently selected note (Important for finalizing editing operations)
     */
    private void handleNoteChanges() {
        notesListView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldNote, newNote) -> {
                    if (isTitleEditInProgress && !titleBuffer.isEmpty()) {
                        // Show a confirmation alert if edits are in progress
                        isTitleEditInProgress = false;
                        showConfirmationAlert(oldNote);
                        return; // Exit early to avoid other operations
                    }

                    if (notesListView.getSelectionModel().getSelectedItem() == null) {
                        // No note selected: disable fields
                        noteTitleF.setDisable(true);
                        noteBodyF.setDisable(true);
                        noteTitleF.clear();
                        noteBodyF.clear();
                    } else {
                        // Note selected: enable fields and update UI
                        noteTitleF.setDisable(false);
                        noteBodyF.setDisable(false);
                        setNewNoteFields(newNote);
                    }

                    if (shouldTitleBuffer) {
                        shouldTitleBuffer = false;
                        try {
                            boolean isDuplicate = validateTitleWithServer(
                                    currentCollection.getCollectionId(), titleBuffer);
                            if (isDuplicate) {
                                isDuplicateAlert(titleBuffer);
                            } else if (titleBuffer.isEmpty()) {
                                isEmptyAlert();
                            } else {
                                notesListView.getSelectionModel().getSelectedItem()
                                        .setTitle(titleBuffer);
                            }
                            titleBuffer = "";
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    isProgrammaticChange = true; // Indicate programmatic change
                });
    }


    /**
     * This method shows the confirmation alert that the user has unsaved changes
     * The alert automatically closes after 4 seconds, simulating an ok switch
     * @param oldNote - the old note
     */
    private void showConfirmationAlert(Note oldNote) {
        // Load strings from the resource bundle
        ResourceBundle bundle = ResourceBundle.getBundle("MyBundle", locale);
        String alertTitle = bundle.getString("unsaved_changes_title");
        String alertHeader = bundle.getString("unsaved_changes_header");
        String alertContent = bundle.getString("unsaved_changes_content");
        String cancelLabel = bundle.getString("cancel_button");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(alertTitle);
        alert.setHeaderText(alertHeader);
        alert.setContentText(alertContent);

        ButtonType cancelButton = new ButtonType(cancelLabel, ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        alert.getButtonTypes().setAll(cancelButton, okButton);

        // Access the dialog's OK button node to update its text dynamically
        DialogPane dialogPane = alert.getDialogPane();
        Button okButtonNode = (Button) dialogPane.lookupButton(okButton);

        // Start a countdown to update the OK button text
        final int[] countdown = {4}; // Initial countdown value
        Timeline countdownTimeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    if (countdown[0] > 1) {
                        countdown[0]--;
                        okButtonNode.setText("OK (" + countdown[0] + ")");
                    } else {
                        alert.setResult(okButton); // Simulate OK button being clicked
                        alert.hide();             // Close the alert
                    }
                })
        );
        countdownTimeline.setCycleCount(5); // Run for 5 seconds
        countdownTimeline.play(); // Start the countdown

        // Show the alert and wait for the user's response
        Optional<ButtonType> result = alert.showAndWait();
        if (!result.isPresent() || result.get() == cancelButton) {
            // Indicate that the title buffer should be restored
            shouldTitleBuffer = true;
            // Restore the old title
            notesListView.getSelectionModel().select(oldNote);
        }
    }


    /**
     * Obtains the current collection. In case there is none, it creates one.
     */
    public void setUpCollections() {
        ObservableList<Collection> collectionOptions = FXCollections.observableArrayList();

        // check if it's the first run and no defaultCollection is on the server
        ensureDefaultCollectionExists();
        collectionOptions.add(new Collection(currentServer, "All",
                "all", false));
        collectionOptions.addAll(currentServer.getCollections());
        selectCollectionBox.setItems(collectionOptions);

        if (ok == 1) { // check if it's the first run in the session
            //auto-set to show All notes
            selectCollectionBox.setValue(collectionOptions.get(0));
            ok = 0;
            // Show a pop-up informing the user about the default collection
            PauseTransition delay = new PauseTransition(Duration.seconds(2.5));
            delay.setOnFinished(event -> Platform.runLater(this::showDefaultCollectionPopup));
            delay.play(); // Delayed the execution of the popup
            updateDefaultCollection();
        }

        // Setting up the converter for displaying collection titles
        selectCollectionBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Collection collection) {
                return collection.getCollectionTitle();
            }

            @Override
            public Collection fromString(String s) {
                return null;
            }
        });

        selectCollectionBox.getSelectionModel()
                .selectedItemProperty().addListener((obs, oldCollection, newCollection) -> {
                    if (!newCollection.equals(oldCollection)) {
                        loadCollectionsFromServer();
                        updateNotesList(newCollection);

                        // Update current collection based on selection
                        if (newCollection.getCollectionTitle().equals("All")) {
                            currentCollection = default_collection; // Resetting to Default
                        } else {
                            currentCollection = newCollection; // Setting to the selected collection
                        }
                    }
                });
    }

    /**
     * Updates the default_collection locally
     */
    private void updateDefaultCollection() {
        loadCollectionsFromServer();

        for (int i = 0; i < currentServer.getCollections().size(); i++) {
            if (currentServer.getCollections().get(i).getCollectionTitle().equals("Default")) {
                default_collection = currentServer.getCollections().get(i);
                currentCollection = default_collection;
                break;
            }
        }
    }

    /**
     * Ensures the default collection exists in the server
     * for the first run of the session.
     */
    private void ensureDefaultCollectionExists() {
        if (ok == 1 && currentServer.getCollections().stream().noneMatch(
                collection -> collection.getCollectionTitle().equals("Default"))) {
            currentServer.addCollection(default_collection);
            saveDefaultCollectionToServer();
        }
    }

    private void showDefaultCollectionPopup() {
        ResourceBundle bundle = ResourceBundle.getBundle("MyBundle", locale);
        String alertTitle = bundle.getString("collection_title");
        String alertContent = bundle.getString("collection_content");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(alertTitle);
        alert.setHeaderText(null);
        alert.setContentText(alertContent);
        alert.showAndWait();
    }

    private void saveDefaultCollectionToServer() {
        try {
            Collection savedCollection = serverUtils.saveCollectionToServer(default_collection);
            if (savedCollection != null) {
                System.out.println("Collection " + savedCollection.getCollectionTitle()
                        + " has been added to the server.");
            } else {
                System.out.println("Failed to add collection. " +
                        "The server was unable to save the new collection->returned empty");
            }
        } catch (IOException e) {
            System.err.println("An error occurred while saving the collection: "
                    + e.getMessage());
        }
    }

    /**
     * Updates "notes" based on the chosen collection in selectCollectionBox
     *
     * @param selectedCollection (chosen collection)
     */
    private void updateNotesList(Collection selectedCollection) {
        if (selectedCollection.getCollectionTitle().equals("All")) {
            loadCollectionsFromServer();
            notes.setAll(currentServer.getCollections().stream()
                    .flatMap(collection -> collection.getNotes().stream())
                    .toList());
            updateUIAfterChange();
        } else {
            loadCollectionsFromServer();
            notes.setAll(selectedCollection.getNotes());
            updateUIAfterChange();
        }
    }

    /**
     * EditState enum for keeping track of the editing of the fields
     */
    private EditState currentEditState = EditState.NONE;

    /**
     * Fetches collections from the server and stores them locally
     */
    public void loadCollectionsFromServer() {
        List<Collection> fetchedCollections = serverUtils.loadCollectionsFromServer();

        if (!fetchedCollections.isEmpty()) {
            currentServer.getCollections().clear(); // Clear existing collections
            currentServer.getCollections().addAll(fetchedCollections);
            System.out.println("Collections loading status from the current server: not empty.");
        } else {
            System.err.println("No collections loaded. Server is empty.");
        }
    }

    /**
     * Sends a request to the server to delete a collection by a provided ID.
     *
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
        handleTagsAndEmbeddedShortcuts(event);
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
        // Shift + U to upload a file
        if (event.isShiftDown() && event.getCode() == KeyCode.U) {
            uploadImage();
            event.consume();
        }
    }

    private void handleTagsAndEmbeddedShortcuts(KeyEvent event) {
        // Shift + U to upload a file
        if (event.isShiftDown() && event.getCode() == KeyCode.U) {
            uploadImage();
            event.consume();
        }
        // Control + C to clear tags
        if (event.isControlDown() && event.getCode() == KeyCode.C) {
            clearTags();
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
        shortcuts.setTitle(bundle.getString("Keyboard_Shortcuts"));
        shortcuts.setHeaderText(bundle.getString("Keyboard_Shortcuts_h"));
        shortcuts.setContentText(bundle.getString("Keyboard_Shortcuts_b"));
        //Shortcuts text jic something goes wrong in bundle:
//            Shift + A: Add a new note
//            Delete: Delete the selected note
//            Page Up/Down: Navigate between note title and content
//            Shift + S: Show shortcuts pop-up
//            Shift + L: Show available languages
//            Shift + C: Show available collections
//            Shift + E: Edit collections
//            Shift + T: Edit Tags
//            Shift + U: Upload a file
//            Control + C: Clear Tags
//            F5: \t\tRefresh
//            Ctrl + F: Search within a note
//            ESC: Set/reset focus to the collection search
//
//            COLLECTION VIEW:
//            Control + A: Add a collection
//            Control + Delete: Delete a collection
//            Control + D: Make collection default
//            Control + S: Save a collection
        shortcuts.showAndWait();
    }

    /**
     * This method ensures that the title and the content of the Note
     * will be synced with the database every second if something was
     * changed.
     * 1 second is specified in initialize method
     */
    private void syncIfChanged() {
        if (currentNote != null &&
                !isTitleEditInProgress && // Prevent syncing if title is being edited
                (!noteBodyF.getText().equals(lastSyncedBody))) {

            //Save body edits automatically and syncs note with server
            bodyEdit();

            lastSyncedTitle = currentNote.getTitle();
            lastSyncedBody = currentNote.getBody();

            System.out.println("Note synced with the server at: " + java.time.LocalTime.now());
        }
    }

    /**
     * This method ensures the syncing with the server (database).
     *
     * @param note - note provided - in syncIfChanged method to be specific
     */
    public void syncNoteWithServer(final Note note) {
        if (serverUtils.syncNoteWithServer(note)) {
            // mabie try Platform.runLater()
            Note updatedNote = fetchNoteById(note.getNoteId());
            if (updatedNote != null) {
                currentNote = updatedNote;
                refreshNotesInListView(currentNote);
            }
            System.out.println("Note synced successfully with server.");
        } else {
            System.err.println("Failed to sync note with server.");
        }
    }


    /**
     * Refreshes the note in the ListView to reflect any updates.
     * Also updates the filtered notes list.
     *
     * @param updatedNote - the note to be refreshed
     */
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

    /**
     * Updates the Markdown display for the current note's title and body.
     */
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

    /**
     * Fetches all notes from the server and updates the local notes list.
     * Logs errors if fetching fails.
     */
    private void loadNotesFromServer() {
        List<Note> fetchedNotes = serverUtils.loadNotesFromServer();

        if (!fetchedNotes.isEmpty()) {
            notes.clear();
            notes.addAll(fetchedNotes);

            currentCollection.getNotes().clear();
            currentCollection.getNotes().addAll(fetchedNotes);
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
            // Process #tags and [[notes]] in the body
            String processedContent = processTagsAndReferences(noteBodyF.getText());

            String processedImages = processImageMarkdown(processedContent);

            // Convert the title and body to HTML
            String showTitle = "<h1 class=\"title\">"
                    + renderer.render(parser.parse(newValue))
                    + "</h1>";
            String showContent = renderer.render(parser.parse(processedImages));

            // Load the combined title and content into the WebView
            String titleAndContent = showTitle + "<hr>" + showContent;
            titleAndContent = "<link rel='stylesheet' type='text/css' href='"
                    + webviewCSSPath + "'>" + titleAndContent;
            markDownOutput.getEngine().loadContent(titleAndContent);
        });
    }


    /**
     * This method adds the listener to the content/body field.
     * It fully supports the Markdown syntax based on the commonmark library.
     */
    public void markDownContent() {
        noteBodyF.textProperty().addListener((observable, oldValue, newValue) -> {
            if (currentNote == null || currentNote.getNoteId() <= 0) {
                System.err.println("No note selected or note ID is invalid");
                return;
            }
            System.out.println("Note (MD): " + currentNote.getNoteId()
                    + currentNote.getTags().toString());

            // Process #tags to make them clickable
            String processedContent = newValue.replaceAll("#(\\w+)",
                    "<button style=\"background-color: #e43e38; color: white; " +
                            "border: none; padding: 2px 6px; border-radius: " +
                            "4px; cursor: pointer;\" " +
                            "onclick=\"javaApp.filterByTag('#$1')\">#$1</button>");

            // Process [[Note References]]
            StringBuffer processedReferences = processNoteReferences(processedContent);

            // Process Image Syntax (![alt-text](image-url))
            String processedImages = processImageMarkdown(processedReferences.toString());

            // Convert the title and body to HTML
            String showTitle = "<h1 class=\"title\">"
                    + renderer.render(parser.parse(noteTitleF.getText()))
                    + "</h1>";
            String showContent = renderer.render(parser.parse(processedImages));

            String titleAndContent = showTitle + "<hr>" + showContent;
            titleAndContent = "<link rel='stylesheet' type='text/css' href='"
                    + webviewCSSPath + "'>" + titleAndContent;
            markDownOutput.getEngine().loadContent(titleAndContent);
        });
    }

    private String processImageMarkdown(String content) {
        // Fetch the images for the current note
        List<Images> availableImages = fetchImagesForNote();
        String list = "";
        for (Images images : availableImages) {
            list += images.getName() + " ";
        }
        System.out.println("Available Images: " + list);

        for (Images image : availableImages) {
            // Construct the image URL
            String encodedTitle = URLEncoder.encode(currentNote.getTitle(), StandardCharsets.UTF_8)
                    .replace("+", "%20");
            String encodedName = URLEncoder.encode(image.getName(), StandardCharsets.UTF_8)
                    .replace("+", "%20");
            String imageUrl = String.format("http://127.0.0.1:8080/api/images/files/notes/%s/%s", encodedTitle, encodedName);

            // Log the constructed image URL
            System.out.println("Generated Image URL for " + image.getName() + ": " + imageUrl);

            // Match the specific Markdown syntax for this image
            String markdownPattern = String.format("!\\[(.*?)\\]\\(%s\\)",
                    Pattern.quote(image.getName()));
            content = content.replaceAll(markdownPattern, Matcher.quoteReplacement(
                    String.format("<img src=\"%s\" alt=\"$1\" " +
                            "style=\"max-width: 100%%; height: auto;\"/>", imageUrl)
            ));
        }

        // Log the final processed content
        System.out.println("Final Processed Content: " + content);

        return content;
    }


    /**
     * This method processes the [[Other Note]] references
     *
     * @param processedContent - the content that is processed
     * @return the StringBuffer representing the other note
     */
    private StringBuffer processNoteReferences(String processedContent) {
        Matcher matcher = Pattern.compile("\\[\\[(.*?)\\]\\]").matcher(processedContent);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String title = matcher.group(1);
            boolean noteExists = notes.stream().anyMatch(note -> note.getTitle().equals(title));
            String replacement;

            if (noteExists) {
                replacement = "<a href=\"#\" style=\"color: blue; text-decoration: underline;\" onclick=\"javaApp.openNoteByTitle('" + title.replace("'", "\\'") + "')\">" + title + "</a>";
            } else {
                replacement = "<span style=\"color: red; font-style: italic;\">"
                        + title + "</span>";
            }

            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        return result;
    }

    /**
     * Refreshes the display of tags in the tag filter container.
     * Updates the UI based on the available tags and ensures placeholder visibility.
     */
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

    /**
     * Filters notes by the specified tag and updates the tag selection UI.
     * Adds a new tag to the filter if it doesn't exist, validates tag combinations,
     * and applies filtering to the notes.
     *
     * @param tag The tag to filter by. "#" is removed if present.
     */
    @FXML
    public void filterByTag(String tag) {
        Platform.runLater(() -> { // Clean the tag (remove # if present)
            String cleanTag = tag.startsWith("#") ? tag.substring(1) : tag;
            // Check if the tag already exists in the filtering box
            boolean tagExists = selectedTagsContainer.getChildren().stream()
                    .filter(node -> node instanceof ChoiceBox)
                    .map(node -> ((ChoiceBox<String>) node).getValue())
                    .anyMatch(existingTag -> existingTag.equals(cleanTag));
            if (!tagExists) { // Create the ChoiceBox for the new tag
                ChoiceBox<String> tagChoiceBox = new ChoiceBox<>();
                tagChoiceBox.setValue(cleanTag);
                // Store the original value to revert if the selection is canceled
                final String[] originalValue = {cleanTag};
                // Refresh the available tags when the ChoiceBox is opened
                tagChoiceBox.setOnShowing(event -> refreshAvailableTags(tagChoiceBox));
                // Revert to the original value if the dropdown is closed without selecting anything
                tagChoiceBox.setOnHiding(event -> {
                    if (tagChoiceBox.getValue() == null) {
                        Platform.runLater(() -> tagChoiceBox.setValue(originalValue[0]));
                    }
                }); // Validate the new tag combination when a tag is selected
                tagChoiceBox.getSelectionModel()
                        .selectedItemProperty()
                        .addListener((obs, oldTag, newTag) -> {
                            if (newTag != null) {
                                Set<String> selectedTags = collectSelectedTags(newTag, oldTag);
                                if (isCombinationValid(selectedTags)) {
                                    adjustChoiceBoxWidth(tagChoiceBox, newTag);
                                    filterNotesByTags();
                                    originalValue[0] = newTag; // Update the original value
                                } else {// Revert selection
                                    bundle = ResourceBundle.getBundle("MyBundle", locale);
                                    String invalidMsgTitle = bundle
                                            .getString("InvTagComb_t");
                                    String invalidMsg1 = bundle.getString("InvTagComb_m1");
                                    String invalidMsg2 = bundle.getString("InvTagComb_m2");
                                    showAlert(invalidMsgTitle, invalidMsg1 + invalidMsg2);
                                    Platform.runLater(()
                                            -> tagChoiceBox.setValue(originalValue[0]));
                                }
                            }
                        });
                adjustChoiceBoxWidth(tagChoiceBox, cleanTag);
                togglePlaceholderText();// Remove placeholder text when tags are added
                selectedTagsContainer.getChildren().add(tagChoiceBox);
                filterNotesByTags();// Apply filtering with the initial tag
            }
        });
    }

    /**
     * Collects all currently selected tags from the tag filter container.
     *
     * @param newTag - the newly selected tag
     * @param oldTag - the previously selected tag being replaced
     * @return a set of all currently selected tags
     */
    private Set<String> collectSelectedTags(String newTag, String oldTag) {
        // Collect currently selected tags
        Set<String> selectedTags = selectedTagsContainer
                .getChildren().stream()
                .filter(node -> node instanceof ChoiceBox)
                .map(node -> ((ChoiceBox<String>) node).getValue())
                .collect(Collectors.toSet());

        // Replace old tag with the new one in the tag set
        if (oldTag != null) selectedTags.remove(oldTag);
        if (newTag != null) selectedTags.add(newTag);

        return selectedTags;
    }

    /**
     * Refreshes the list of available tags for selection in a specific ChoiceBox.
     * Excludes tags that are already selected in other ChoiceBoxes.
     *
     * @param choiceBox - the ChoiceBox for which the available tags are refreshed
     */
    private void refreshAvailableTags(ChoiceBox<String> choiceBox) {
        // Get all tags except the ones already selected in other ChoiceBoxes
        Set<String> selectedTags = selectedTagsContainer
                .getChildren().stream()
                .filter(node -> node instanceof ChoiceBox)
                .map(node -> ((ChoiceBox<String>) node).getValue())
                .collect(Collectors.toSet());

        List<String> availableTags = getAllTags().stream()
                .filter(tagName -> !selectedTags.contains(tagName))
                .collect(Collectors.toList());

        // Update the available options in the ChoiceBox
        choiceBox.setItems(FXCollections
                .observableArrayList(availableTags));
    }


    /**
     * Toggles the visibility of the placeholder text in the tag filter container.
     * Ensures the placeholder is visible when no tags are selected and hidden otherwise.
     */
    private void togglePlaceholderText() {
        Platform.runLater(() -> {
            boolean hasTags = selectedTagsContainer.getChildren().stream()
                    .anyMatch(node -> node instanceof ChoiceBox);

            if (!hasTags) {
                // Show placeholder text if no tags are present
                if (selectedTagsContainer.getChildren().stream()
                        .noneMatch(node -> node instanceof Text)) {
                    bundle = ResourceBundle.getBundle("MyBundle", locale);
                    String placeholderValue = bundle.getString("Filter_by_tag");
                    Text placeholderText = new Text(placeholderValue);
                    placeholderText.setStyle("-fx-fill: lightgray; " +
                            "-fx-font-size: 12; -fx-font-style: italic;");
                    selectedTagsContainer.getChildren().add(placeholderText);
                }
            } else {
                // Remove placeholder text if tags are present
                selectedTagsContainer.getChildren()
                        .removeIf(node -> node instanceof Text);
            }
        });
    }

    /**
     * Adjusts the width of a ChoiceBox based on the length of the tag it displays.
     *
     * @param choiceBox - the ChoiceBox whose width needs adjustment
     * @param tag       - the tag being displayed in the ChoiceBox
     */
    private void adjustChoiceBoxWidth(ChoiceBox<String> choiceBox, String tag) {
        // Calculate the approximate text width for the tag
        double textWidth = computeTextWidth(tag, 12); // 12 is the font size

        // Define the maximum allowable width
        double maxWidth = 150; // Adjust this value as needed
        double padding = 30; // Space for dropdown arrow

        // Set the ChoiceBox width to the smaller of the calculated width or maxWidth
        choiceBox.setPrefWidth(Math.min(textWidth + padding, maxWidth));
    }

    /**
     * Computes the width of a given text string based on a specified font size.
     *
     * @param text     - the text whose width is to be computed
     * @param fontSize - the font size used for the computation
     * @return the computed width of the text in pixels
     */
    private double computeTextWidth(String text, int fontSize) {
        // Create a temporary Text node to calculate the width
        Text tempText = new Text(text);
        tempText.setStyle("-fx-font-size: " + fontSize + "px;"); // Set the font size

        // Use a dummy scene to accurately calculate the layout bounds
        new Scene(new Group(tempText));
        return tempText.getLayoutBounds().getWidth();
    }

    /**
     * Retrieves a list of all unique tags across all notes.
     *
     * @return a list of distinct tag names from all notes
     */
    private List<String> getAllTags() {
        return notes.stream()
                .flatMap(note -> note.getTags().stream())
                .map(Tag::getName)
                .distinct()
                .collect(Collectors.toList());
    }


    /**
     * Filters the notes displayed in the ListView based on the selected tags.
     * Updates the UI to show only notes that match all selected tags.
     */
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

    /**
     * Syncs changes in the filtered notes list back to the main notes list.
     *
     * @param filteredNotes - the filtered list of notes
     */
    private void syncFilteredNotes(ObservableList<Note> filteredNotes) {
        for (Note note : filteredNotes) {
            int index = notes.indexOf(note);
            if (index != -1) {
                notes.set(index, note); // Sync changes back to the original list
            }
        }
    }

    /**
     * Displays an alert dialog with the specified title and message.
     *
     * @param title   - the title of the alert dialog
     * @param message - the message to be displayed in the alert dialog
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Validates whether the selected combination of tags matches any existing notes.
     *
     * @param selectedTags - the set of selected tags to validate
     * @return true if the combination matches at least one note, false otherwise
     */
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

    /**
     * Refreshes the notes and tags by reloading data from the server and updating the UI.
     */
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
                errorLogger.log(Level.SEVERE,
                        "Error during refresh: " + e.getMessage(), e);
            }
        });
    }

    /**
     * Clears all selected tags from the filter and resets the ListView to display all notes.
     * Displays an alert if no tags are present to clear.
     */
    @FXML
    private void clearTags() {
        // Check if there are any tags in the filter box
        boolean hasTags = selectedTagsContainer.getChildren().stream()
                .anyMatch(node -> node instanceof ChoiceBox);

        if (!hasTags) {
            // Show alert if no tags are present
            bundle = ResourceBundle.getBundle("MyBundle", locale);
            String titleAlert = bundle.getString("No_tags_to_clear_t");
            String messageAlert = bundle.getString("No_tags_to_clear_m");
            showAlert(titleAlert,
                    messageAlert);
            return;
        }

        Platform.runLater(() -> {
            selectedTagsContainer.getChildren().clear(); // Clear selected tags
            togglePlaceholderText(); // Show placeholder text
            notesListView.setItems(FXCollections.observableArrayList(notes));

            System.out.println("All tags cleared and notes list reset.");
        });
    }


    /**
     * Find the referenced note and open it
     *
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
            bundle = ResourceBundle.getBundle("MyBundle", locale);
            String refNoteNotFoundT = bundle
                    .getString("Ref_note_not_found_t");
            String refNoteNotFoundH = bundle
                    .getString("Ref_note_not_found_h"); //header
            String refNoteNotFoundM1 = bundle
                    .getString("Ref_note_not_found_m1");
            String refNoteNotFoundM2 = bundle
                    .getString("Ref_note_not_found_m2");
            alert.setTitle(refNoteNotFoundT);
            alert.setHeaderText(refNoteNotFoundH);
            alert.setContentText(refNoteNotFoundM1
                    + title + refNoteNotFoundM2);
            alert.showAndWait();
        }
    }


    /**
     * Update all the references from notes when the title of the referenced
     * note was changed
     *
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
        //success msg needs to be invoked AFTER ensuring it is correct
        bundle = ResourceBundle.getBundle("MyBundle", locale);
        showAddInfo(bundle.getString("NoteSuccessfulAdded"));

    }

    /**
     * This method shows the 'toast' pop up when a note is added
     *
     * @param message - the message to be displayed
     */
    private void showAddInfo(String message) {
        // Get the root node of the scene
        Pane root = (Pane) addB.getScene().getRoot();
        // Create a label for the toast message
        Label toastLabel = new Label(message);
        toastLabel.setStyle("-fx-background-color:  #1E1E1E; " +
                "-fx-text-fill: white; -fx-padding: 10px;" +
                " -fx-border-radius: 5; -fx-background-radius: 5;");

        // Wrap the toast in a temporary StackPane
        StackPane toastContainer = new StackPane(toastLabel);
        toastContainer.setStyle("-fx-alignment: center; " +
                "-fx-background-color: transparent;");
        toastContainer.setMouseTransparent(true);

        // Add the StackPane to the root node
        root.getChildren().add(toastContainer);

        // This centers the label
        if (root instanceof Pane pane) {
            toastContainer.layoutXProperty()
                    .bind(pane.widthProperty()
                            .subtract(toastContainer.widthProperty()).divide(2));
            toastContainer.layoutYProperty()
                    .bind(pane.heightProperty()
                            .subtract(toastContainer.heightProperty()).divide(2));
        }
        // Fade-in animation
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), toastContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        // Pause before fade-out
        PauseTransition pause = new PauseTransition(Duration.millis(1500));
        // Fade-out animation
        FadeTransition fadeOut = new FadeTransition(Duration.millis(500), toastContainer);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(event -> root.getChildren().remove(toastContainer));
        // Play animations sequentially
        new SequentialTransition(fadeIn, pause, fadeOut).play();
    }


    /**
     * Method to add a new note.
     *
     * @param newNote - note to be added
     * @throws IOException - exception that will occur if server error
     */
    public void addCommand(final Note newNote) throws IOException {
        Note savedNote = saveNoteToServer(newNote);
        if (savedNote != null) {
            currentCollection.addNote(savedNote);  // Add to the collection
            notes.add(savedNote);                   // Add to the ObservableList
            notesListView.getSelectionModel().select(savedNote);
            // Update UI fields
            currentNote = savedNote;
            noteTitleF.setText(savedNote.getTitle());
            noteBodyF.setText(savedNote.getBody());
        }
        updateUIAfterChange();
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
        System.out.println(currentCollection.getCollectionId() + " " +
                currentCollection.getCollectionId()); // testing
        try {
            Note savedNote = serverUtils.saveNoteToServer(note,
                    currentCollection.getCollectionId());
            note.setNoteId(savedNote.getNoteId());
            return savedNote;
        } catch (IOException e) {
            return null;
        }
    }


    /**
     * Sends request to the server to add a note with a provided Note.
     *
     * @param note - Note
     */
    public void addRequest(final Note note) {
        boolean success = serverUtils.addNoteToServer(note);

        if (success) {
            System.out.println("Note added successfully to the server.");
        } else {
            System.err.println("Failed to add note to the server.");
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
            bundle = ResourceBundle.getBundle("MyBundle", locale);
            String deleteNoteT = bundle.getString("Delete_note_t");
            String deleteNoteH = bundle.getString("Delete_note_h");
            String deleteNoteM = bundle.getString("Delete_note_m");

            alert.setTitle(deleteNoteT);
            alert.setHeaderText(deleteNoteH);
            alert.setContentText(deleteNoteM + selectedNote.getTitle() + "\"");
            var result = alert.showAndWait(); // Waiting for user response
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Create a delete command and execute it
                Command deleteCommand = new DeleteNoteCommand(
                        this, selectedNote);
                invoker.executeCommand(deleteCommand);
                //Confirmation alert that note was deleted
                bundle = ResourceBundle.getBundle("MyBundle", locale);
                String alertTitle = bundle.getString("Note_deleted");
                String alertBody = bundle.getString("Delete_successful");
                alert.setTitle(alertTitle);
                alert.setHeaderText(null);
                alert.setContentText(alertBody);
                alert.showAndWait();
            }
        } else {
            bundle = ResourceBundle.getBundle("MyBundle", locale);
            String alertTitle = bundle.getString("No_note_selected_t");
            String alertHeader = bundle.getString("No_note_selected_h");
            String alertContent = bundle.getString("No_note_selected_m");

            Alert alert = new Alert(Alert.AlertType.WARNING);

            alert.setTitle(alertTitle);
            alert.setHeaderText(alertHeader);
            alert.setContentText(alertContent);
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
            if (currentCollection.getNotes().contains(noteToDelete)) {
                currentCollection.getNotes().remove(noteToDelete);
                notes.remove(noteToDelete);
            } else {
                noteToDelete.getCollection().getNotes().remove(noteToDelete);
                notes.remove(noteToDelete);
            }
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
    public void deleteRequest(final long noteId) {
        boolean success = serverUtils.deleteRequest(noteId);

        if (success) {
            updateUIAfterChange();
            System.out.println("Note with ID " + noteId + " deleted successfully.");
        } else {
            System.err.println("Failed to delete note with ID " + noteId + ".");
        }
    }

    /**
     * Undoes the last action.
     */
    public void undo() {
        // Indicate that the undo change restores the title and body, and it is not user edit
        isProgrammaticChange = true;
        invoker.undoLastCommand(); // Undo the last command with invoker

        // Refresh UI fields to reflect the reverted state of the note
        // Currently for testing
        if (currentNote != null) {
            noteTitleF.setText(currentNote.getTitle()); // Update title field
            noteBodyF.setText(currentNote.getBody());// Update body field
            String processedContent = currentNote.getBody().replaceAll("#(\\w+)",
                    "<button style=\"background-color: #e43e38; color: white; " +
                            "border: none; padding: 2px 6px; border-radius: 4px; cursor: " +
                            "pointer;\" onclick=\"javaApp.filterByTag('#$1')\">#$1</button>");
            StringBuffer processedReferences = processNoteReferences(processedContent);
            String processedImages = processImageMarkdown(processedReferences.toString());
            String showTitle = "<h1 class=\"title\">"
                    + renderer.render(parser.parse(currentNote.getTitle()))
                    + "</h1>";
            String showContent = renderer.render(parser.parse(processedImages));

            String titleAndContent = showTitle + "<hr>" + showContent;
            titleAndContent = "<link rel='stylesheet' type='text/css' href='"
                    + webviewCSSPath + "'>" + titleAndContent;
            markDownOutput.getEngine().loadContent(titleAndContent);
        }
        updateUIAfterChange();

    }

    /**
     * Method that edits the body of the currently selected note
     */
    public void bodyEdit() {
        if (!isBodyEditInProgress) { // Check if a body edit is in progress
            return;
        }

        Note selectedNote = notesListView.getSelectionModel().getSelectedItem();
        if (selectedNote != null) {
            String newBody = noteBodyF.getText().trim();

            if (!newBody.equals(originalBody)) {
                try {
                    // If different from original body, update body and sync with the server
                    // (Invoke command for editing body)
                    Command editBodyCommand = new EditBodyCommand(
                            currentNote, originalBody, newBody, HomeScreenCtrl.this);
                    commandQueue.offer(editBodyCommand);
                    originalBody = newBody; // Update the original body to the new body
                } catch (Exception e) {
                    errorLogger.log(
                            Level.INFO,
                            "Error handling body edit: " + e.getMessage()
                    );
                }
            }
            isBodyEditInProgress = false;
            isProgrammaticChange = true;
        }
    }

    /**
     * Edits the title of the currently selected note.
     */
    public void titleEdit() {
        bundle = ResourceBundle.getBundle("MyBundle", locale);
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
                        isDuplicateAlert(newTitle);
                        Platform.runLater(() -> noteTitleF.setText(originalTitle));
                        updateUIAfterChange();
                    } else if (newTitle.isEmpty()) {
                        // Revert to the original title
                        isEmptyAlert();
                        Platform.runLater(() -> noteTitleF.setText(originalTitle));
                        updateUIAfterChange();
                    } else { // If not duplicate, update title and sync with server
                        Command editTitleCommand = new EditTitleCommand(currentNote,
                                originalTitle, newTitle, HomeScreenCtrl.this);
                        commandQueue.offer(editTitleCommand);
                        originalTitle = newTitle;
                    }
                } catch (Exception e) {
                    errorLogger.log(
                            Level.INFO, bundle.getString("ErrValidation")
                                    + e.getMessage());
                }
            }
            isTitleEditInProgress = false;
            isProgrammaticChange = true;
        }
    }

    /**
     * This method shows the alert represented as WARNING
     */
    private void showWarningAlert(String title, String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);
        alert.showAndWait();
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
    private Note fetchNoteById(final long noteId) {
        Note note = serverUtils.fetchNoteById(noteId);

        if (note != null) {
            return note;
        } else {
            System.err.println("Failed to fetch note with ID: " + noteId);
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
                    && noteMatchIndices.size() == 1) {
                System.out.println(
                        "Not found in \"" + currentNote.getTitle() + "\"");
            } else {
                //parse in special way such that the found results are
                // highlighted
                bodyHighlighted
                        = highlightMatchesInBody(bodyHighlighted, searchText, titleHighlighted);
                titleHighlighted = highlightMatchesInTitle(titleHighlighted, searchText);

            }
        }
        // Process #tags and [[notes]] after highlighting
        String processedContent = bodyHighlighted.replaceAll("#(\\w+)",
                "<button style=\"background-color: #e43e38; color: white; " +
                        "border: none; padding: 2px 6px; border-radius: " +
                        "4px; cursor: pointer;\" " +
                        "onclick=\"javaApp.filterByTag('#$1')\">#$1</button>");
        StringBuffer processedReferences = processNoteReferences(processedContent);
        String processedImages = processImageMarkdown(processedReferences.toString());
        String showTitle = "<h1 class=\"title\">"
                + renderer.render(parser.parse(titleHighlighted))
                + "</h1>";
        String showContent = renderer.render(parser.parse(processedImages));

        String titleAndContent = showTitle + "<hr>" + showContent;
        titleAndContent = "<link rel='stylesheet' type='text/css' href='"
                + webviewCSSPath + "'>" + titleAndContent;
        markDownOutput.getEngine().loadContent(titleAndContent);
    }

    /**
     * This method highlights the matches in the title.
     *
     * @param titleHighlighted - the title highlighted
     * @param searchText       - the search text
     * @return a String representing the highlighted title part
     */
    private String highlightMatchesInTitle(String titleHighlighted, String searchText) {
        String highlightedTitle = titleHighlighted;
        for (int i = noteMatchIndices.size() - 1; i >= 0; i--) {
            if (noteMatchIndices.get(i) < highlightedTitle.length()) {
                highlightedTitle = highlightMatch(
                        highlightedTitle,
                        searchText,
                        Math.toIntExact(noteMatchIndices.get(i)),
                        i == currentSearchIndex
                );
            }
        }
        return highlightedTitle;
    }

    /**
     * This method highlights the matches in the body of the note
     *
     * @param body       - the body of the note
     * @param searchText - the search text
     * @param title      - the title provided
     * @return a String representing the highlighted part
     */
    private String highlightMatchesInBody(String body, String searchText, String title) {
        String highlightedBody = body;
        int titleLength = title.length();
        for (int i = noteMatchIndices.size() - 1; i >= 0; i--) {
            if (noteMatchIndices.get(i) >= titleLength) {
                int bodyIndex = Math.toIntExact(noteMatchIndices.get(i)) - titleLength;
                highlightedBody = highlightMatch(
                        highlightedBody,
                        searchText,
                        bodyIndex,
                        i == currentSearchIndex
                );
            }
        }
        return highlightedBody;
    }

    /**
     * This method highlights the match
     *
     * @param content    - the content
     * @param searchText - the search text
     * @param index      - the index of the current match
     * @param isCurrent  - is it current?
     * @return a String representing the highlighted part
     */
    private String highlightMatch(String content, String searchText, int index, boolean isCurrent) {
        String highlightStyle = isCurrent ? " style=\"background: rgb(225,193,110)\"" : "";
        return content.substring(0, index)
                + "<mark" + highlightStyle + ">"
                + searchText
                + "</mark>"
                + content.substring(index + searchText.length());
    }

    /**
     * Processes text to identify and format `#tags` and `[[note]]` references
     * into clickable elements. Tags are converted to buttons, and note references
     * are converted to links or placeholders.
     *
     * @param text - the text containing tags and note references
     * @return the processed text with formatted tags and references
     */
    private String processTagsAndReferences(String text) {
        // Process #tags
        String processedContent = text.replaceAll("#(\\w+)",
                "<button style=\"background-color: #e43e38; color: " +
                        "white; border: none; padding: 2px 6px; border-radius:" +
                        " 4px; cursor: pointer;\" " +
                        "onclick=\"javaApp.filterByTag('#$1')\">#$1</button>");

        // Process [[note]] references
        Matcher matcher = Pattern.compile("\\[\\[(.*?)\\]\\]").matcher(processedContent);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String title = matcher.group(1);
            boolean noteExists = notes.stream().anyMatch(note -> note.getTitle().equals(title));
            String replacement;

            if (noteExists) {
                replacement = "<a href=\"#\" style=\"color: blue; " +
                        "text-decoration: underline;\" onclick=\"javaApp" +
                        ".openNoteByTitle('" + title.replace("'", "\\'")
                        + "')\">" + title + "</a>";
            } else {
                replacement = "<span style=\"color: red; font-style: italic;\">"
                        + title + "</span>";
            }

            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Refreshes the ListView to display the latest state of the notes list.
     */
    public void updateUIAfterChange() {
        Platform.runLater(() -> {
            notesListView.refresh(); // Refresh the ListView
            notesListView.setItems(FXCollections.observableArrayList(notes));
        });
    }

    /**
     * Searches through the notes and respects the current filters, including tags.
     */
    public void searchCollection() {
        filterNotesByTags();
        // Normalize search text
        String searchText = searchCollectionF.getText().trim().toLowerCase();

        // Determine the base list to filter (current filtered notes or all notes)
        // Start with the currently displayed notes
        ObservableList<Note> baseList = notesListView.getItems();

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
     * This method renders the highlighted content in the markdown
     *
     * @param title - the title of the note
     * @param body  - the body of the note
     */
    private void renderHighlightedContent(String title, String body) {
        String renderedTitle = "<h1>" + renderer.render(parser.parse(title)) + "</h1>";
        String renderedBody = renderer.render(parser.parse(body));
        String totalContent = renderedTitle + renderedBody;
        markDownOutput.getEngine().loadContent(totalContent);
    }

    /**
     * Sets up the languages (adds them to the collection box).
     *
     * @noinspection checkstyle:MagicNumber
     */
    public void setUpLanguages() {
        final double height = 15;
        final double width = 30;

        initializeLanguageOptions();
        configureLanguageComboBox(height, width);
        configureLanguageConverter();
        configureLanguageSelectionListener(height, width);
    }

    /**
     * This method initializes the language options
     */
    private void initializeLanguageOptions() {
        selectLangBox.getItems().forEach(lang -> System.out.println(
                "Language: " + lang.getAbbr()));
        selectLangBox.getItems()
                .setAll(LanguageOptions.getInstance().getLanguages());
        selectLangBox.setValue(selectLangBox.getItems().getFirst());
    }

    /**
     * This method configures the combo box with languages
     *
     * @param height - the height of the combo box
     * @param width  - the width of the combo box
     */
    private void configureLanguageComboBox(double height, double width) {
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
                                    setGraphic(createLanguageIcon(item.getImgPath(),
                                            height,
                                            width));
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
                    setGraphic(createLanguageIcon(item.getImgPath(), height, width));
                }
            }
        });
    }

    private ImageView createLanguageIcon(String iconPath, double height, double width) {
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
        return iconImageView;
    }

    /**
     * This method configures the language converter
     */
    private void configureLanguageConverter() {
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
    }

    private void configureLanguageSelectionListener(double height, double width) {
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
                        uploadImageB.setText(bundle.getString("Upload"));
                        downloadImageB.setText(bundle.getString("Download"));
                        clearTagsB.setText(bundle.getString("ClearTags"));
                        String placeholderValue = bundle.getString("Filter_by_tag");
                        Text placeholderText = new Text(placeholderValue);
                        placeholderText.setStyle("-fx-fill: lightgray; "
                                + "-fx-font-size: 12; -fx-font-style: italic;");
                        // Add the placeholder text to the container by default
                        selectedTagsContainer.getChildren().clear();
                        selectedTagsContainer.getChildren().add(placeholderText);
                    }
                });
    }

    /**
     * This method saves the selected language to a config file
     *
     * @param languageToSave - language to be saved
     */
    private void saveLanguageChoice(Language languageToSave) throws IOException {
        File languageFile = new File("language-choice.txt");
        if (!languageFile.exists()) {
            languageFile.createNewFile();
        }
        try (FileWriter fw = new FileWriter(languageFile)) {
            fw.write(languageToSave.getAbbr());
        }
    }

    /**
     * This method loads the language that is stored in the file
     *
     * @throws IOException - when something messes up
     */
    private void loadSavedLanguageChoice() throws IOException {
        File languageFile = new File("language-choice.txt");
        if (languageFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(languageFile))) {
                String savedLanguageAbbr = br.readLine();
                if (!selectLangBox.getItems().isEmpty())
                    initializeLanguageOptions();
                for (Language language : selectLangBox.getItems()) {
                    if (language.getAbbr().equals(savedLanguageAbbr)) {
                        selectLangBox.setValue(language);
                        locale = switch (language.getAbbr()) {
                            case "ES" -> Locale.of("es", "ES");
                            case "NL" -> Locale.of("nl", "NL");
                            case "ZZ" -> Locale.of("zz", "ZZ");
                            default -> Locale.of("en", "US");
                        };
                        System.out.println(locale.toString());
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
     * Bundle getter.
     *
     * @return bundle for the current (updated) language.
     */
    public static ResourceBundle getBundle() {
        bundle = ResourceBundle.getBundle("MyBundle", locale);
        return bundle;
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
        bundle = ResourceBundle.getBundle("MyBundle", locale);

        fileChooser.setTitle(bundle.getString("Select_File"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(uploadImageB.getScene().getWindow());

        if (file != null) {
            String fileName = file.getName();

            // Check for duplicate names
            List<String> existingNames = fetchImagesForNote().stream()
                    .map(Images::getName)
                    .collect(Collectors.toList());
            if (existingNames.contains(fileName)) {
                bundle = ResourceBundle.getBundle("MyBundle", locale);
                String alertTitle = bundle.getString("Duplicate_img_t");
                String alertBod1 = bundle.getString("Duplicate_img_m1");
                String alertBod2 = bundle.getString("Duplicate_img_m2");
                String alertBod3 = bundle.getString("Duplicate_img_m3");

                showErrorDialog(alertTitle,
                        alertBod1 + fileName + alertBod2 + alertBod3);
                return;
            }

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
     * Saves images to the server.
     *
     * @param image
     * @return Images object
     * @throws IOException
     */
    public Images saveImageToServer(final Images image) throws IOException {
        if (currentNote == null) {
            throw new IllegalStateException("Current note or note ID is not set");
        }

        try {
            return serverUtils.saveImageToServer(image, currentNote.getNoteId());
        } catch (IOException e) {
            System.err.println("Error saving image to server: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Fetches the notes images from the server
     *
     * @return List of image names
     */
    public List<Images> fetchImagesForNote() {
        if (currentNote == null || currentNote.getNoteId() <= 0) {
            throw new IllegalStateException("Current note or note ID is not set");
        }

        return serverUtils.fetchImagesForNote(currentNote.getNoteId());
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
            System.out.println("Loaded " + images.size()
                    + " images for note: " + currentNote.getTitle());
        });
    }

    /**
     * Allows for the editing of image names on double click
     */
    private void setupImageListView() {
        imageListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedImageName = imageListView.getSelectionModel().getSelectedItem();
                if (selectedImageName != null) {
                    renameImage(selectedImageName);
                }
            }
        });
    }

    /**
     * Changes the name of an image and ensures the name
     * cannot be empty/file extension cant be changed
     *
     * @param currentName
     */
    private void renameImage(String currentName) {
        TextInputDialog dialog = new TextInputDialog(currentName);
        dialog.setTitle("Rename Image");
        dialog.setHeaderText("Rename Image");
        dialog.setContentText("New name:");
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newName -> {
            if (!isValidNameChange(currentName, newName)) {
                bundle = ResourceBundle.getBundle("MyBundle", locale);
                showErrorDialog(bundle.getString("InvalidNameT"),
                        bundle.getString("InvalidNameM"));
                return;
            } // Check for duplicate names
            List<String> existingNames = fetchImagesForNote().stream()
                    .map(Images::getName).collect(Collectors.toList());
            if (existingNames.contains(newName)) {
                bundle = ResourceBundle.getBundle("MyBundle", locale);
                String eTitle = bundle.getString("Duplicate_img_t");
                String eM1 = bundle.getString("Duplicate_img_m1");
                String eM2 = bundle.getString("Duplicate_img_m2");
                String eM3 = bundle.getString("Duplicate_img_m3");
                showErrorDialog(eTitle, eM1 + newName + eM2 + eM3);
                return;
            }
            Images imageToRename = fetchImageByName(currentName);
            if (imageToRename != null) {
                imageToRename.setName(newName);
                try {
                    Images updatedImage = serverUtils.updateImageOnServer(imageToRename);
                    if (updatedImage != null) {
                        imageListView.getItems().set(
                                imageListView.getItems().indexOf(currentName), newName);
                    } else {
                        bundle = ResourceBundle.getBundle("MyBundle", locale);
                        String updateFailedT = bundle.getString("updateFailedT");
                        String updateFailedM = bundle.getString("updateFailedM");
                        showErrorDialog(updateFailedT,
                                updateFailedM);
                    }
                } catch (IOException e) {
                    bundle = ResourceBundle.getBundle("MyBundle", locale);
                    String serverT = bundle.getString("ServerErrorT");
                    String serverM = bundle.getString("ServerErrorM");
                    showErrorDialog(serverT, serverM + e.getMessage());
                }
            }
        });
    }

    /**
     * Checks for the validity of the changed image name
     *
     * @param oldName
     * @param newName
     * @return boolean value for validity
     */
    private boolean isValidNameChange(String oldName, String newName) {
        if (newName == null || newName.trim().isEmpty()) return false;

        String oldExtension = oldName.substring(oldName.lastIndexOf('.') + 1);
        String newExtension = newName.substring(newName.lastIndexOf('.') + 1);

        return oldExtension.equalsIgnoreCase(newExtension);
    }

    /**
     * Retrieves the image based on its name
     *
     * @param name
     * @return
     */
    private Images fetchImageByName(String name) {
        List<Images> images = fetchImagesForNote();
        return images.stream()
                .filter(img -> img.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    /**
     * Method is called when clicking the delete button,
     * deletes a selected image from note
     */
    @FXML
    public void deleteImage() {
        String selectedImageName = imageListView.getSelectionModel().getSelectedItem();
        bundle = ResourceBundle.getBundle("MyBundle", locale);
        if (selectedImageName == null) {
            String noImgSelectT = bundle.getString("NoImgSelectT");
            String noImgSelectM = bundle.getString("NoImgSelectM");
            showErrorDialog(noImgSelectT, noImgSelectM);
            return;
        }

        // Confirm deletion
        String deleteImgT = bundle.getString("deleteImgT");
        String deleteImgH = bundle.getString("deleteImgH");
        String deleteImgM = bundle.getString("deleteImgM");
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(deleteImgT);
        alert.setHeaderText(deleteImgH);
        alert.setContentText(deleteImgM + selectedImageName + "\"");
        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            Images imageToDelete = fetchImageByName(selectedImageName);

            if (imageToDelete == null) {
                String alertT = bundle.getString("ImgNotFoundT");
                String alertM = bundle.getString("ImgNotFoundM");
                showErrorDialog(alertT, alertM);
                return;
            }

            try {
                boolean success = serverUtils.deleteImageFromServer(imageToDelete);

                if (success) {
                    Platform.runLater(() -> imageListView.getItems().remove(selectedImageName));
                    System.out.println("Image deleted: " + selectedImageName);
                } else {
                    bundle = ResourceBundle.getBundle("MyBundle", locale);
                    String delImgErrT = bundle.getString("delImgErrT");
                    String delImgErrM = bundle.getString("delImgErrM");
                    showErrorDialog(delImgErrT, delImgErrM);
                }
            } catch (Exception e) {
                bundle = ResourceBundle.getBundle("MyBundle", locale);
                String imgDelErrorT = bundle.getString("ImgDelErrorT");
                String imgDelErrorM = bundle.getString("ImgDelErrorM");
                showErrorDialog(imgDelErrorT, imgDelErrorM + e.getMessage());
            }
        }
    }

    /**
     * Shows an image download screen
     */
    @FXML
    public void showDownloadImageScreen() {
        Stage downloadStage = new Stage();
        bundle = ResourceBundle.getBundle("MyBundle", locale);
        downloadStage.setTitle(bundle.getString("DownloadImg"));

        // Layout
        VBox layout = new VBox(10);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        // Input field for the image name
        TextField imageNameField = new TextField();
        imageNameField.setPromptText(bundle.getString("Enter_image_name"));

        // Download button
        Button downloadButton = new Button(bundle.getString("Download"));
        downloadButton.setOnAction(event -> {
            String imageName = imageNameField.getText().trim();
            if (!imageName.isEmpty()) {
                downloadImage(imageName);
            } else {
                String invalidInput = bundle.getString("InvalidInput");
                String invalidInputM = bundle.getString("InvalidInputM");
                showErrorDialog(invalidInput, invalidInputM);
            }
        });


        layout.getChildren().addAll(new Label(bundle.getString("Download_an_Image")),
                imageNameField, downloadButton);

        // Scene setup
        Scene downloadScene = new Scene(layout, 300, 150);
        downloadStage.setScene(downloadScene);
        downloadStage.show();
    }

    /**
     * Downloads an image to the device
     *
     * @param imageName
     */
    private void downloadImage(String imageName) {
        if (currentNote == null || currentNote.getNoteId() <= 0) {
            bundle = ResourceBundle.getBundle("MyBundle", locale);
            String noNoteSelectT = bundle.getString("noNoteSelectT");
            String noNoteSelectM = bundle.getString("noNoteSelectM");
            showErrorDialog(noNoteSelectT,
                    noNoteSelectM);
            return;
        }
        try {
            // Construct the image URL
            String encodedNoteTitle = URLEncoder.encode(currentNote.getTitle(),
                    StandardCharsets.UTF_8).replace("+", "%20");
            String encodedImageName = URLEncoder.encode(imageName, StandardCharsets.UTF_8)
                    .replace("+", "%20");
            String imageUrl = String.format("http://localhost:8080/api/images/files/notes/%s/%s",
                    encodedNoteTitle, encodedImageName);
            // Fetch the image data from the server
            HttpResponse<byte[]> response = HttpClient.newHttpClient()
                    .send(HttpRequest.newBuilder()
                            .uri(URI.create(imageUrl))
                            .GET()
                            .build(), HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() == 200) {
                byte[] imageData = response.body();
                // Save the image locally
                FileChooser fileChooser = new FileChooser();
                fileChooser.setInitialFileName(imageName);
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter("All Files", "*.*"));

                File saveFile = fileChooser.showSaveDialog(null);
                if (saveFile != null) {
                    Files.write(saveFile.toPath(), imageData);
                    bundle = ResourceBundle.getBundle("MyBundle", locale);
                    showInfoDialog(bundle.getString("Download_Successful"),
                            bundle.getString("Image_downloaded_successfully_to")
                                    + saveFile.getAbsolutePath());
                }
            } else {
                bundle = ResourceBundle.getBundle("MyBundle", locale);
                showErrorDialog(bundle.getString("Download_Failed"),
                        "Download_Failed_m");
            }
        } catch (Exception e) {
            bundle = ResourceBundle.getBundle("MyBundle", locale);
            showErrorDialog(bundle.getString("Error"),
                    bundle.getString("ErrorM") + e.getMessage());
        }
    }

    /**
     * Information dialog
     *
     * @param title
     * @param message
     */
    private void showInfoDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showErrorDialog(
            final String currentTitle, final String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(currentTitle);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Starts a separate thread to process commands from the queue.
     * Optimizes command invoking performance
     */
    private void startCommandProcessor() {
        Thread commandProcessorThread = new Thread(() -> {
            try {
                while (true) {
                    // Take the next command from the queue and execute it
                    Command command = commandQueue.take();
                    Platform.runLater(() -> {
                        // Invoke the command on JavaFX thread for no errors
                        invoker.executeCommand(command);
                    });
                    System.out.println("COMMAND EXECUTED: " + command.toString());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupted status
                System.err.println("Command processor interrupted: " + e.getMessage());
            }
        });

        commandProcessorThread.setDaemon(true); // Ensure the thread exits with the application
        commandProcessorThread.start();
    }

    /**
     * Alert if title is empty
     */
    public void isEmptyAlert() {
        String alertTitle = bundle.getString("Empty_title_t");
        String alertHeader = bundle.getString("Empty_title_h");
        String alertBod = bundle.getString("Empty_title_m");
        showWarningAlert(alertTitle, alertHeader, alertBod);
    }

    /**
     * Alert if title is duplica
     *
     * @param title - String title
     */
    public void isDuplicateAlert(String title) {
        String alertTitle = bundle.getString("Title_dup_t");
        String alertHeader = bundle.getString("Title_dup_h");
        String alertContent1 = bundle.getString("Title_dup_m1");
        String alertContent2 = bundle.getString("Title_dup_m2");
        String alertContent3 = bundle.getString("Title_dup_m3");
        showWarningAlert(alertTitle,
                alertHeader, alertContent1 + title
                        + alertContent2 + alertContent3);

    }

}


