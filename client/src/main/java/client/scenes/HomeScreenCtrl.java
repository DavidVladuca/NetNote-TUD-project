package client.scenes;


import client.HomeScreen;
import client.utils.Command;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import commons.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import commons.Collection;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.io.IOException;
import commons.Collection;
import commons.Server;

public class HomeScreenCtrl {
    //todo - for all methods, change strings title and body to getting them from the note instead
    private final ScreenCtrl sc;
    private Stage primaryStage;
    private javafx.scene.Scene homeScene;
    private javafx.scene.Scene editCollectionScene;

    public void init(Stage primaryStage,
                     Pair<HomeScreenCtrl, Parent> home,
                     Pair<EditCollectionsViewCtrl, Parent> editCollection) {
        this.primaryStage = primaryStage;
        this.homeScene = new javafx.scene.Scene(home.getValue());
        this.editCollectionScene = new javafx.scene.Scene(editCollection.getValue());
        showHome();
        primaryStage.show();
    }

    public void showHome() {
        if (primaryStage == null) {
            throw new IllegalStateException("Primary stage is not initialized");
        }
        primaryStage.setScene(homeScene);
    }

    public void showEditCollection() {
        if (primaryStage == null) {
            throw new IllegalStateException("Primary stage is not initialized");
        }
        primaryStage.setScene(editCollectionScene);
    }

    @Inject
    public HomeScreenCtrl(ScreenCtrl sc) {
        this.sc = sc;
    }

    @FXML
    public Button addB;
    public Button deleteB;
    public Button undoB;
    public Button dropDownSearchNoteB;
    public Button prevMatchB;
    public Button nextMatchB;

    public Button refreshB;
    public Button editCollectionsB;

    public Text collection_text;
    public Text language_text;

    ResourceBundle bundle;
    Locale locale;
    public ComboBox<Language> selectLangBox = new ComboBox<Language>();
    public TextField noteTitleF;
    public TextArea noteBodyF;

    public TextField searchCollectionF;
    public TextField searchNoteF;
    private int current_search_index = 0;
    private  ArrayList<Long> note_match_indices;
    public Button searchMore;
    public Button getNextMatch;
    public Button getPreviousMatch;
    public WebView markDownOutput;
    public ChoiceBox<Collection> selectCollectionBox = new ChoiceBox<>();
    private CommandInvoker invoker = new CommandInvoker(); // Invoker for keeping history of commands and executing them
    private Note lastDeletedNote = null;
    public Button tagsButton;

    // List of all available tags (replace with actual fetching logic)
    private final ObservableList<Tag> availableTags = FXCollections.observableArrayList(
            new Tag("#Tag1"), new Tag("#Tag2"), new Tag("#Tag3")
    );

    /*
    todo - change the 3 below (current_server, current_collection, current_note) -
     should not be initialized every time, but i needed to have them to have the search method.
     */
    public Server current_server = new Server();
    public Collection current_collection = new Collection(current_server, "Default");
    public Note current_note = new Note("", "", current_collection);

    public ListView<Note> notesListView;
    private ObservableList<Note> notes = FXCollections.observableArrayList();

    public String title = "";
    public String content = ""; //todo - this should be part of the note - not independent strings

    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private String lastSyncedTitle = "";
    private String lastSyncedBody = "";

    public final Parser parser = Parser.builder().build();
    public final HtmlRenderer renderer = HtmlRenderer.builder().build();

    /**
     * This method initializes the controller
     * and sets up the listener for the text area that the user types in. - based on other methods it calls
     */
    @FXML
    public void initialize() {
        keyboardShortcuts();
        arrowKeyShortcuts();
        scheduler.scheduleAtFixedRate(this::syncIfChanged, 0, 5, TimeUnit.SECONDS);
        setUpLanguages();
        setUpCollections();
        markDownTitle();
        markDownContent();
        loadNotesFromServer();
        setupNotesListView();
        loadTagsFromServer();
    }

    private void loadTagsFromServer() {
        try {
            var response = ClientBuilder.newClient()
                    .target("http://localhost:8080/api/tags")
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            if (response.getStatus() == 200) {
                String json = response.readEntity(String.class);
                ObjectMapper mapper = new ObjectMapper();
                List<Tag> fetchedTags = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, Tag.class));
                availableTags.clear();
                availableTags.addAll(fetchedTags);
            } else {
                System.err.println("Failed to fetch tags. Status: " + response.getStatus());
            }
        } catch (Exception e) {
            System.err.println("Error loading tags: " + e.getMessage());
        }
    }

    // todo edit and delete button for the tags
    @FXML
    public void handleTagsButtonAction() {
        if (current_note == null) {
            System.err.println("No note selected. Cannot assign tags.");
            return;
        }

        // Log current tags
        System.out.println("Current tags for note '" + current_note.getTitle() + "':");
        current_note.getTags().forEach(tag -> System.out.println("- " + tag.getName()));

        // Create the dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Manage Tags");
        dialog.setHeaderText("Select or create tags for the note:");

        // Container for tag checkboxes
        VBox tagListContainer = new VBox(10);
        List<CheckBox> tagCheckBoxes = new ArrayList<>();

        // Populate the tag list with checkboxes
        for (Tag tag : availableTags) {
            CheckBox checkBox = new CheckBox(tag.getName());
            checkBox.setSelected(current_note.getTags().contains(tag));
            tagCheckBoxes.add(checkBox);
            tagListContainer.getChildren().add(checkBox);
        }

        // ScrollPane for tag list
        ScrollPane scrollPane = new ScrollPane(tagListContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(100); // Limit to approx. 4 tags visible

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
                    showErrorDialog("Invalid Tag Name", "The tag name cannot be empty.");
                    return;
                }

                if (availableTags.stream().anyMatch(tag -> tag.getName().equals(tagName.trim()))) {
                    showErrorDialog("Duplicate Tag", "A tag with the name '" + tagName.trim() + "' already exists.");
                    return;
                }

                Tag newTag = new Tag(tagName.trim());
                availableTags.add(newTag);

                CheckBox newCheckBox = new CheckBox(newTag.getName());
                tagCheckBoxes.add(newCheckBox);
                tagListContainer.getChildren().add(newCheckBox);

                try {
                    saveTagToServer(newTag);
                    System.out.println("New tag saved to server: " + newTag.getName());
                } catch (Exception e) {
                    System.err.println("Failed to save the new tag: " + e.getMessage());
                }
            });
        });

        // Layout for the dialog content
        VBox dialogContent = new VBox(15, scrollPane, addTagButton);
        dialog.getDialogPane().setContent(dialogContent);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

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

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


    private void updateNoteTags(Set<Tag> selectedTags) {
        Set<Tag> currentTags = new HashSet<>(current_note.getTags());
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
            current_note.setTags(currentTags);
            syncNoteTagsWithServer(current_note);
        }
    }

    private void saveTagToServer(Tag tag) throws IOException {
        String json = new ObjectMapper().writeValueAsString(tag);
        var response = ClientBuilder.newClient()
                .target("http://localhost:8080/api/tags/create")
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(json, MediaType.APPLICATION_JSON));

        if (response.getStatus() != 201) {
            throw new IOException("Failed to save tag. Status: " + response.getStatus());
        }
    }


    private void syncNoteTagsWithServer(Note note) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Set<String> tagNames = note.getTags().stream().map(Tag::getName).collect(Collectors.toSet());
            String json = mapper.writeValueAsString(tagNames);

            Response response = ClientBuilder.newClient()
                    .target("http://localhost:8080/api/notes/" + note.getNoteId() + "/tags")
                    .request(MediaType.APPLICATION_JSON)
                    .put(Entity.entity(json, MediaType.APPLICATION_JSON));

            if (response.getStatus() != 200) {
                System.err.println("Failed to sync tags with server. Status: " + response.getStatus());
            }
        } catch (Exception e) {
            System.err.println("Error syncing tags: " + e.getMessage());
        }
    }


    /**
     * This method sets up the keyboard shortcuts specified here
     * For add - the user needs to click 'Shift + A'
     * For delete - the user needs to click 'Control + Shift + A'
     */
    public void keyboardShortcuts() {
        Platform.runLater(() -> {
            addB.getScene().setOnKeyPressed(event -> {
                // When clicking 'Shift + A' add method will be called
                if(event.isShiftDown() && event.getCode() == KeyCode.A) {
                    try {
                        add();
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                // When clicking 'Shift + D' delete method will be called
                if(event.isShiftDown()
                        && event.getCode() == KeyCode.D) {
                    delete();
                }
                // When clicking 'Shift + Tab' the show shortcuts alert will pop up
                if(event.isShiftDown()
                        && event.getCode() == KeyCode.TAB) {
                    showShortcuts();
                }
            });
        });
    }

    /**
     * This method sets up the arrow key shortcuts in the TextField for custom navigation with shift
     */
    public void arrowKeyShortcuts() {
        // For note title
        noteTitleF.setOnKeyPressed(event -> {
            // When clicking 'Shift + Right Arrow' the focus will go to the notes list view
            if(event.isShiftDown() && event.getCode() == KeyCode.LEFT) {
                notesListView.requestFocus();
            }
            // When clicking 'Shift + Left Arrow' the focus will go to the note body
            if(event.isShiftDown() && event.getCode() == KeyCode.RIGHT) {
                noteBodyF.requestFocus();
            }
        });
        // For note body
        noteBodyF.setOnKeyPressed(event -> {
            // When clicking 'Shift + Left Arrow' the focus will go to the note title
            if(event.isShiftDown() && event.getCode() == KeyCode.LEFT) {
                noteTitleF.requestFocus();
            }
        });
    }

    /**
     * Displays a pop-up with the list of keyboard shortcuts and their descriptions.
     */
    public void showShortcuts() {
        Alert shortcuts = new Alert(Alert.AlertType.INFORMATION);
        shortcuts.setTitle("Keyboard Shortcuts");
        shortcuts.setHeaderText("Available Keyboard Shortcuts");
        shortcuts.setContentText(
                "Shift + A: Add a new note\n" +
                        "Shift + D: Delete the selected note\n" +
                        "Shift + Arrow Left/Right: Navigate between fields\n\t\twhen in the text field\n" +
                        "Shift + Tab: Show shortcuts pop-up"
        );
        shortcuts.showAndWait();
    }

    /**
     * This method ensures that the title and the content of the Note
     * will be synced with the database every 5 seconds if something was changed.
     * 5 seconds is specified in initialize method
     */
    private void syncIfChanged() {
        Platform.runLater(() -> {
            // Check if the note content has changed since the last sync
            if (current_note != null &&
                    (!current_note.getTitle().equals(lastSyncedTitle) ||
                            !current_note.getBody().equals(lastSyncedBody))) {

                // Sync with the server if change
                syncNoteWithServer(current_note);

                // Update the last synced title and body to current title and body
                lastSyncedTitle = current_note.getTitle();
                lastSyncedBody = current_note.getBody();

                System.out.println("Note synced with the server at: " + java.time.LocalTime.now()); // for testing
            }
        });
    }

    /**
     * This method ensures the syncing with the server (database)
     * @param note - note provided - in syncIfChanged method to be specific
     */
    private void syncNoteWithServer(Note note) {
        try {
            String json = new ObjectMapper().writeValueAsString(note);
            System.out.println("Serialized JSON: " + json);  // for testing

            var requestBody = Entity.entity(json, MediaType.APPLICATION_JSON);
            // the put request to actually update the content with json
            var response = ClientBuilder.newClient()
                    .target("http://localhost:8080/api/notes/update")
                    .request(MediaType.APPLICATION_JSON)
                    .put(requestBody);

            // Refresh the notes list
            Platform.runLater(() -> refreshNotesInListView(note));

            System.out.println("Response Status: " + response.getStatus()); // for testing
            System.out.println("Response Body: " + response.readEntity(String.class)); // for testing
            // if something screwed up :D
            if (response.getStatus() != 200) {
                System.err.println("Failed to update note on server. Status code: " + response.getStatus());
            }
        } catch (Exception e) {
            System.err.println("Error syncing note with server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void refreshNotesInListView(Note note) {
        for (int i = 0; i < notes.size(); i++) {
            Note update = notes.get(i);
            if (note.getNoteId() == update.getNoteId()) {
                if(update.getTitle() != null && !update.getTitle().equals("")) {
                    // Update the note's data in the ObservableList
                    notes.set(i, update);
                    break;
                }
            }
        }
    }

    public void editCollections() {
        System.out.println("Edit Collections View Selected");
        sc.showEditCollection();
    }

    private void loadNotesFromServer() {
        try {
            // Fetch notes from the server
            var response = ClientBuilder.newClient()
                    .target("http://localhost:8080/api/notes/fetch") // Update with your server's API URL
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            if (response.getStatus() == 200) {
                // Parse the JSON response into a List of Note objects
                String json = response.readEntity(String.class);
                ObjectMapper mapper = new ObjectMapper();
                List<Note> fetchedNotes = mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, Note.class));

                // Add the fetched notes to the ObservableList
                notes.clear(); // Clear existing notes
                notes.addAll(fetchedNotes);
            } else {
                System.err.println("Failed to fetch notes. Error code " + response.getStatus());
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
             *      the cell becomes empty, or it is being re-rendered due to changes
             *      in the ListView (e.g., scrolling or data updates).
             *
             * @param note - the Note object associated with this cell. It may be null if the cell is empty.
             * @param empty - a boolean indicating whether the cell is empty.
             *              If true, the cell should be cleared and not display any content.
             */
            @Override
            protected void updateItem(Note note, boolean empty) {
                super.updateItem(note, empty);
                setText(empty || note == null ? null : note.getTitle());
            }
        });

        // Handling selection in the ListView
        notesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldNote, newNote) -> {
            if (newNote != null) {
                current_note = newNote;
                //change the output in the front-end for title and body
                noteTitleF.setText(newNote.getTitle());
                noteBodyF.setText(newNote.getBody());

                Platform.runLater(() -> notesListView.getSelectionModel());
            }
        });
    }

    /**
     * This method adds the listener to the title field. It automatically converts the content to
     * a heading of type h1, because it is a title
     */
    public void markDownTitle() {
        noteTitleF.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                current_note.setTitle(newValue);
                // MD -> HTML
                title = "<h1>" + renderer.render(parser.parse(newValue)) + "</h1>";
                // Adds title and content together so it's not overridden
                String titleAndContent = title + content;
                // WebView is updated based on the HTML file
                markDownOutput.getEngine().loadContent(titleAndContent);
            }
        });
    }

    /**
     * This method adds the listener to the content/body field.
     * It fully supports the Markdown syntax based on the commonmark library.
     */
    public void markDownContent() {
        noteBodyF.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // MD -> HTML
                current_note.setBody(newValue);
                content = renderer.render(parser.parse(newValue));
                // Adds title and content together so it's not overridden
                String titleAndContent = title + content;
                // WebView is updated based on the HTML file
                markDownOutput.getEngine().loadContent(titleAndContent);
            }
        });
    }


    /**
     * Utility function used to locate resources within applications filepath
     *
     * @param path
     * @return
     */
    private static URL getLocation(String path) {
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
        List<String> existingTitles = notes.stream().map(Note::getTitle).toList();
        while (existingTitles.contains(newTitle)) {
            newTitle = baseTitle + " (" + counter + ")";
            counter++;
        }

        // Create the new note
        Note newNote = new Note(newTitle, "", current_collection);
        // Create command for adding new note
        Command addNoteCommand = new AddNoteCommand(this,newNote);
        // Use the invoker to execute the command
        invoker.executeCommand(addNoteCommand);

//        // Immediately send the new note to the server to get a valid noteID that is NOT 0
//        try {
//            Note savedNote = saveNoteToServer(newNote); // Calls the create endpoint - see the method
//
//            // Update the collection and UI with the saved note
//            current_collection.addNote(savedNote);  // Add to the collection
//            notes.add(savedNote);                   // Add to the ObservableList
//
//            // Select the new note in the ListView
//            notesListView.getSelectionModel().select(savedNote);
//
//            // Update UI fields
//            current_note = savedNote;
//            noteTitleF.setText(savedNote.getTitle());
//            noteBodyF.setText(savedNote.getBody());
//
//        } catch (Exception e) {
//            System.err.println("Failed to save the note: " + e.getMessage());
//        }
    }
    public void addCommand(Note newNote) throws IOException, InterruptedException {
        Note savedNote = saveNoteToServer(newNote);

        current_collection.addNote(savedNote);  // Add to the collection
        notes.add(savedNote);                   // Add to the ObservableList
        notesListView.getSelectionModel().select(savedNote);
        // Update UI fields
        current_note = savedNote;
        noteTitleF.setText(savedNote.getTitle());
        noteBodyF.setText(savedNote.getBody());
    }


    /**
     * Sends the note to the server via the create endpoint and returns the saved note.
     * This ensures the note gets a valid noteId from the server.
     * It is very similar to addRequest method, however I needed to return a new Note object
     * for the unique ID
     * @param note - note provided
     * @return a Note that was saved with a unique id
     */
    public Note saveNoteToServer(Note note) throws IOException {
        var json = new ObjectMapper().writeValueAsString(note);
        var requestBody = Entity.entity(json, MediaType.APPLICATION_JSON);
        // Connect to the create endpoint, where add requests are processed
        var response = ClientBuilder.newClient()
                .target("http://localhost:8080/api/notes/create")
                .request(MediaType.APPLICATION_JSON)
                .post(requestBody);

        if (response.getStatus() == 201) {
            return response.readEntity(Note.class);
        } else {
            throw new IOException("Server returned status: " + response.getStatus());
        }
    }


    /**
     * Sends request to the server to add a note with a provided Note
     * @param note - Note
     */
    public void addRequest(Note note) throws JsonProcessingException {
        var json = new ObjectMapper().writeValueAsString(note);
        var requestBody = Entity.entity(json, MediaType.APPLICATION_JSON);
        var response = ClientBuilder.newClient()
                .target("http://localhost:8080/api/notes/create") // Update with the correct endpoint for adding a note
                .request(MediaType.APPLICATION_JSON)
                .post(requestBody);
    }

    /**
     * Removes a selected note and stores it in a stack for future restoration
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
                Command deleteCommand = new DeleteNoteCommand(this, selectedNote);
                invoker.executeCommand(deleteCommand);

                System.out.println("Note deleted: " + selectedNote.getTitle()); // For testing purposes
                System.out.println("Note deleted: " + selectedNote.getNoteId());

                //Confirmation alert that note was deleted
                alert.setTitle("Note Deleted");
                alert.setHeaderText(null);
                alert.setContentText("The note has been successfully deleted!");
                alert.showAndWait();
            }
        }
        // Show a warning if no note is selected
        else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Note Selected!");
            alert.setHeaderText("No note selected to delete.");
            alert.setContentText("Please select a note from the list to delete.");
            alert.showAndWait();
        }
        System.out.println("Delete");  //Temporary for testing
    }

    public void deleteCommand(long noteId){
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
            current_collection.getNotes().remove(noteToDelete);
            // Send a delete request to the server
            deleteRequest(noteId);
        } else {
            // If the note is not found in the ObservableList, log a warning
            System.err.println("Note not found in the UI. ID: " + noteId);
        }
    }

    /**
     * Sends request to the server to delete a note by a provided ID
     * @param noteId
     */
    public static void deleteRequest(long noteId){
        Response response = ClientBuilder.newClient()
                .target("http://localhost:8080/api/notes/delete/" + noteId) // Endpoint for deletion
                .request()
                .delete();
        if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
            System.out.println("Note successfully deleted.");
        } else if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
            System.out.println("Note not found.");
        } else {
            System.out.println("Failed to delete note. Status: " + response.getStatus());
        }
        response.close();
    }

    /**
     * Undoes the last action
     */
    public void undo() throws JsonProcessingException {
        System.out.println("Undo");//Temporary for testing
        invoker.undoLastCommand();
    }


    /**
     * Edits the title of the currently selected note
     */
    public void titleEdit() {

        System.out.println("Title Edit");  //Temporary for testing

        Note selectedNote = notesListView.getSelectionModel().getSelectedItem();

        if (selectedNote != null) {
            Collection selectedCollection = selectedNote.getCollection();

            // Searching for title duplicates in the respective collection
            int ok = 1;
            for (int i = 0; i < selectedCollection.getNotes().size(); i++) {
                if (selectedCollection.getNotes().get(i).getTitle().equals(noteTitleF.getText())
                        && !selectedCollection.getNotes().get(i).equals(selectedNote)) {
                    ok = 0;
                }
            }

            if (ok == 1) {
                //saving the title
                selectedNote.setTitle(noteTitleF.getText());
                syncIfChanged();
            } else {
                //found a duplicate of the title
                Alert alert = new Alert(Alert.AlertType.WARNING);

                alert.setTitle("Duplicate Title");
                alert.setHeaderText("Title Already Exists");
                alert.setContentText("The title you have chosen already exists in this collection. Please choose a different one.");

                alert.showAndWait(); // Wait for the user to dismiss the alert

                noteTitleF.requestFocus(); // Set focus back to the title field for convenience

                System.out.println("Note title already exists"); // Temporary for testing
            }
        }

    }

    /**
     * Refreshes the notes list by re-fetching the notes from the server.
     */
    public void refresh() {
        Platform.runLater(() -> {
            System.out.println("Refreshing all notes...");
            try {
                loadNotesFromServer(); // Re-fetches all notes from the server and updates the ObservableList
                System.out.println("All notes refreshed successfully!");
            } catch (Exception e) {
                System.err.println("Error refreshing notes: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Fetches a specific note from the server by its ID
     *
     * @param noteId The ID of the note to fetch
     * @return The fetched Note object or null if it was not found or there was an error
     */
    private Note fetchNoteById(long noteId) {
        try {
            var response = ClientBuilder.newClient()
                    .target("http://localhost:8080/api/notes/" + noteId) // Replace with actual API endpoint for fetching a single note
                    .request(MediaType.APPLICATION_JSON)
                    .get();

            if (response.getStatus() == 200) {
                String json = response.readEntity(String.class);
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(json, Note.class);
            } else {
                System.err.println("Failed to fetch note with ID " + noteId + ". Status code: " + response.getStatus());
                return null;
            }
        } catch (Exception e) {
            System.err.println("Error fetching note with ID " + noteId + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Searches for a note based on text field input
     */
    public void searchNote() { //make sure this remains like this after merge.
        String search_text = searchNoteF.textProperty().getValue();
        note_match_indices = current_note.getMatchIndices(search_text);
        if (search_text.isEmpty()){
            current_search_index=0;
        }
        String titleHighlighted = current_note.getTitle();
        String bodyHighlighted = current_note.getBody();
        if (!note_match_indices.isEmpty()){
            if (note_match_indices.getFirst()==-1L && note_match_indices.size()==1L){
                System.out.println("Not found in \""+current_note.getTitle()+"\"");
            } else{ //parse in special way such that the found results are highlighted
                for (int i=note_match_indices.size()-1; i>=0; i--){//iterating from the back to not have to consider changes in index due to additions
                    if (note_match_indices.get(i)<titleHighlighted.length()){
                        if (i==current_search_index) {
                            titleHighlighted = titleHighlighted.substring(0, Math.toIntExact(note_match_indices.get(i)))
                                    + "<mark style=\"background: #E1C16E\">"
                                    + search_text
                                    + "</mark>"
                                    + titleHighlighted.substring((int) (note_match_indices.get(i) + search_text.length()));
                        } else {
                            titleHighlighted = titleHighlighted.substring(0, Math.toIntExact(note_match_indices.get(i)))
                                    + "<mark>"
                                    + search_text
                                    + "</mark>"
                                    + titleHighlighted.substring((int) (note_match_indices.get(i) + search_text.length()));
                        }
                    } else {
                        if (i==current_search_index){
                            bodyHighlighted = bodyHighlighted.substring(0, (int) (note_match_indices.get(i) - titleHighlighted.length()))
                                    + "<mark style=\"background: #E1C16E\">"
                                    + search_text
                                    + "</mark>"
                                    + bodyHighlighted.substring((int) (note_match_indices.get(i) - titleHighlighted.length() + search_text.length()));
                        } else {
                            bodyHighlighted = bodyHighlighted.substring(0, (int) (note_match_indices.get(i) - titleHighlighted.length()))
                                    + "<mark>"
                                    + search_text
                                    + "</mark>"
                                    + bodyHighlighted.substring((int) (note_match_indices.get(i) - titleHighlighted.length() + search_text.length()));
                        }
                    }
                }
            }
        }
        titleHighlighted  = "<h1>" + renderer.render(parser.parse(titleHighlighted)) + "</h1>";
        bodyHighlighted = renderer.render(parser.parse(bodyHighlighted));
        String total_content = titleHighlighted + bodyHighlighted;
        markDownOutput.getEngine().loadContent(total_content);


    }

    /**
     * Searches through collection and displays notes that match search
     */
    public void searchCollection(){//todo - finish
        String search_text = searchCollectionF.textProperty().getValue();
        ArrayList<ArrayList<Long>> collection_match_indices = current_collection.getSearch(search_text); //todo -check if collection gets updated, or only fetchedNotes
        ObservableList<Note> display_notes = FXCollections.observableArrayList();
        if (!collection_match_indices.isEmpty()){
            if (collection_match_indices.getFirst().getFirst()==-1) {
                System.out.println("There are no matches for " + search_text);
                display_notes.clear(); //gives an empty display
            } else{
                for (int i = 0; i< collection_match_indices.size();i++) {
                    display_notes.add(current_collection.getNotes().get(Math.toIntExact(collection_match_indices.get(i).getFirst())));
                }
            }
        } else{
            display_notes=notes;
        }
        notesListView.setItems(display_notes);
    }


    public void setUpLanguages() {
        selectLangBox.getItems().forEach(lang -> System.out.println("Language: " + lang.getAbbr()));
        selectLangBox.getItems().setAll(LanguageOptions.getInstance().getLanguages());
        selectLangBox.setValue(selectLangBox.getItems().getFirst());
        // How to do this gotten from stack overflow (https://stackoverflow.com/questions/32334137/javafx-choicebox-with-image-and-text)
        selectLangBox.setCellFactory(new Callback<ListView<Language>, ListCell<Language>>() {
            @Override
            public ListCell<Language> call(ListView<Language> listView) {
                return new ListCell<Language>() {
                    @Override
                    protected void updateItem(Language item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            String iconPath = item.getImg_path();
                            Image icon = new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
                            ImageView iconImageView = new ImageView(icon);
                            double height = 15;
                            double width = 30;
                            iconImageView.setFitHeight(height);
                            iconImageView.setFitWidth(width);
                            iconImageView.setPreserveRatio(false);
                            setGraphic(iconImageView);

                        }
                    }
                };
            }
        });

        selectLangBox.setButtonCell(new ListCell<Language>() {
            @Override
            protected void updateItem(Language item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String iconPath = item.getImg_path(); //path described from client location
                    Image icon = new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
                    ImageView iconImageView = new ImageView(icon);
                    double height = 15;
                    double width = 30;
                    iconImageView.setFitHeight(height);
                    iconImageView.setFitWidth(width);
                    iconImageView.setPreserveRatio(false);
                    setGraphic(iconImageView); // only shows the flag
                }
            }
        });

        selectLangBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Language language) {
                return language.getAbbr();
            }

            @Override
            public Language fromString(String s) {
                Language lang;
                for (int i=0; i<selectLangBox.getItems().size();i++){
                    if (selectLangBox.getItems().get(i).getAbbr().equals(s)){
                        lang = selectLangBox.getItems().get(i);
                        return lang;
                    }
                }
                return selectLangBox.getItems().getFirst();
            }
        });

        selectLangBox.getSelectionModel().selectedItemProperty().addListener((obs, oldLang, newLang) -> {
            if (!newLang.equals(oldLang)) {
                System.out.println(selectLangBox.getValue().getAbbr());
                locale = switch (selectLangBox.getValue().getAbbr()) {
                    case "ES" -> new Locale("es", "ES");
                    case "NL" -> new Locale("nl", "NL");
                    case "ZZ" -> new Locale("zz", "ZZ");
                    default -> new Locale("en", "US");
                };
                bundle = ResourceBundle.getBundle("MyBundle", locale);

                editCollectionsB.setText(bundle.getString("edit_collection"));
                searchCollectionF.setPromptText(bundle.getString("Search"));
                searchNoteF.setPromptText(bundle.getString("Search"));
                collection_text.setText(bundle.getString("Collection"));
                language_text.setText(bundle.getString("Language"));
                noteTitleF.setPromptText(bundle.getString("Untitled"));
                noteBodyF.setPromptText(bundle.getString("Text_Area"));
                undoB.setText(bundle.getString("Undo"));
                refreshB.setText(bundle.getString("Refresh"));

            }
        });

    }

    public void setUpCollections() {
        selectCollectionBox.getItems().setAll(
                new Collection(current_server, "Default")
                //todo - add all collections
        );

        selectCollectionBox.setValue(selectCollectionBox.getItems().getFirst());
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

        selectCollectionBox.getSelectionModel().selectedItemProperty().addListener((obs, oldCollection, newCollection) -> {
            if (!newCollection.equals(oldCollection)) {
                System.out.println(selectCollectionBox.getValue().getCollectionTitle());
            }
        });
    }

    public void prevMatchB() {
        if (note_match_indices == null || note_match_indices.isEmpty()) {
            System.out.println("No text been searched");
        } else if (note_match_indices.getFirst() == -1) {
            System.out.println("No matches; no previous instance");
        } else if (current_search_index > 0) {
            current_search_index--;
            searchNote();
        } else {
            current_search_index = Math.toIntExact(note_match_indices.size()-1);
            searchNote();
        }
    }
    public void nextMatchB() {
        System.out.println("");
        if (note_match_indices == null || note_match_indices.isEmpty()) {
            System.out.println("No text been searched");
        } else if (note_match_indices.getFirst() == -1) {
            System.out.println("No matches; no next instance");
        } else if (current_search_index < note_match_indices.size() - 1) {
            current_search_index++;
            searchNote();
        } else {
            current_search_index = 0;
            searchNote();
        }
    }
}
