package client.scenes;


import client.HomeScreen;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.inject.Inject;
import commons.Language;
import commons.LanguageOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import commons.Note;
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
import javafx.scene.control.*;
import javafx.scene.web.WebView;
import javafx.util.StringConverter;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import commons.Collection;
import commons.Server;

public class HomeScreenCtrl {
    //todo - for all methods, change strings title and body to getting them from the note instead
    private final ScreenCtrl sc;

    @Inject
    public HomeScreenCtrl(ScreenCtrl sc) {this.sc = sc;}

    @FXML
    public Button addB;
    public Button deleteB;
    public Button undoB;
    public Button dropDownSearchNoteB;
    public Button prevMatchB;
    public Button nextMatchB;

    public Button refreshB;
    public Button editCollectionsB;
    public ChoiceBox<Language> selectLangBox = new ChoiceBox<Language>();
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
    private Note lastDeletedNote = null;

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
        scheduler.scheduleAtFixedRate(this::syncIfChanged, 0, 5, TimeUnit.SECONDS);
        setUpLanguages();
        setUpCollections();
        markDownTitle();
        markDownContent();
        loadNotesFromServer();
        setupNotesListView();
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
        // Immediately send the new note to the server to get a valid noteID that is NOT 0
        try {
            Note savedNote = saveNoteToServer(newNote); // Calls the create endpoint - see the method

            // Update the collection and UI with the saved note
            current_collection.addNote(savedNote);  // Add to the collection
            notes.add(savedNote);                   // Add to the ObservableList

            // Select the new note in the ListView
            notesListView.getSelectionModel().select(savedNote);

            // Update UI fields
            current_note = savedNote;
            noteTitleF.setText(savedNote.getTitle());
            noteBodyF.setText(savedNote.getBody());

        } catch (Exception e) {
            System.err.println("Failed to save the note: " + e.getMessage());
        }
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
     * Removes a selected note
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
                // Proceed with deletion
                lastDeletedNote = selectedNote;
                notes.remove(selectedNote); //Remove from client
                deleteRequest(selectedNote.getNoteId()); // Remove from server database
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
     * Restores the last deleted note
     */
    public void undo() throws JsonProcessingException {
        System.out.println("Undo");//Temporary for testing
        if (lastDeletedNote != null) {
            // Add the deleted note back to the list and the server
            notes.add(lastDeletedNote); // Adds note to the client
            addRequest(lastDeletedNote); // Adds note to the database
            lastDeletedNote = null; //Reset the lastDeletedNote attribute
            //Alert informing the user about the restored note - Could be deleted later
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Undo Successful");
            alert.setHeaderText(null);
            alert.setContentText("The deleted note has been restored.");
            alert.showAndWait();
        } else {
            // If there is no deleted note show an alert
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Nothing to Undo");
            alert.setHeaderText("No deleted note to restore");
            alert.setContentText("Please delete a note first if you want to undo");
            alert.showAndWait();
        }
    }


    /**
     * Edits the title of currently selected note
     */
    public void titleEdit() {

        System.out.println("Title Edit");  //Temporary for testing

        Note selectedNote = notesListView.getSelectionModel().getSelectedItem();

        if (selectedNote != null) {
            Collection selectedCollection = selectedNote.getCollection();

            // Searching for title duplicates in the respective collection
            int ok=1;
            for(int i=0; i<selectedCollection.getNotes().size(); i++) {
                if(selectedCollection.getNotes().get(i).getTitle().equals(noteTitleF.getText())
                        && !selectedCollection.getNotes().get(i).equals(selectedNote)) {
                    ok=0;
                }
            }

            if(ok==1) {
                //saving the title
                selectedNote.setTitle(noteTitleF.getText());
                syncIfChanged();
            }else{
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
                for (int i=0; i< collection_match_indices.size();i++) {
                    display_notes.add(current_collection.getNotes().get(Math.toIntExact(collection_match_indices.get(i).getFirst())));
                }
            }
        } else{
            display_notes=notes;
        }
        notesListView.setItems(display_notes);
    }

    /**
     * Sets up the languages - from all languages available, sets English as Default
     * Listens for changes in language.
     */
    public void setUpLanguages(){ //todo - check if there is a way to store user language preference
        selectLangBox.getItems().setAll(LanguageOptions.getInstance().getLanguages());
        selectLangBox.setValue(selectLangBox.getItems().getFirst());
        selectLangBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Language language) {
                return language.getAbbr();
            }

            @Override
            public Language fromString(String s) {
                return null; //todo - implement (make for loop until the right option is found in the options list)
            }
        });

        selectLangBox.getSelectionModel().selectedItemProperty().addListener((obs, oldLang, newLang) -> {
            if (!newLang.equals(oldLang)) {
                System.out.println(selectLangBox.getValue().getAbbr());
            }});

    }

    public void setUpCollections() {
        selectCollectionBox.getItems().setAll(
                new Collection(current_server, "Default")
                //todo - add all collections
        );

        selectCollectionBox.setValue(selectCollectionBox.getItems().getFirst());
        selectCollectionBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Collection collection) {return collection.getCollectionTitle();}

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

    public void prevMatchB(){
        if (note_match_indices == null || note_match_indices.isEmpty()){
            System.out.println("No text been searched");
        }else if (note_match_indices.getFirst()==-1){
            System.out.println("No matches; no previous instance");
        }else if(current_search_index>0){
            current_search_index--;
            searchNote();
        }else{
            current_search_index= Math.toIntExact(note_match_indices.size()-1);
            searchNote();
        }
    }
    public void nextMatchB(){
        System.out.println("");
        if (note_match_indices ==null || note_match_indices.isEmpty()){
            System.out.println("No text been searched");
        }else if (note_match_indices.getFirst()==-1){
            System.out.println("No matches; no next instance");
        }else if(current_search_index<note_match_indices.size()-1){
            current_search_index++;
            searchNote();
        }else{
            current_search_index=0;
            searchNote();
        }
    }
}
