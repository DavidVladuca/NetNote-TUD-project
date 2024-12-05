package client.scenes;


import client.HomeScreen;
import commons.Language;
import commons.LanguageOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import commons.Note;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
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
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import commons.Collection;
import commons.Server;


public class HomeScreenCtrl {
    //todo - for all methods, change strings title and body to getting them from the note instead
    @FXML
    public Button addB;
    public Button deleteB;
    public Button undoB;
    public ChoiceBox<Language> selectLangBox = new ChoiceBox<Language>();
    public TextField noteTitleF;
    public TextArea noteBodyF;
    public TextField searchF;
    public WebView markDownOutput;
    public ChoiceBox<Collection> selectCollectionBox = new ChoiceBox<>();

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

    public final Parser parser = Parser.builder().build();
    public final HtmlRenderer renderer = HtmlRenderer.builder().build();

    /**
     * This method initializes the controller
     * and sets up the listener for the text area that the user types in. - based on other methods it calls
     */
    @FXML
    public void initialize() {
        setUpLanguages();

        setUpCollections();
        
        markDownTitle();

        markDownContent();

        loadNotesFromServer();

        setupNotesListView();

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
                //change the output in the front-end for title and body
                noteTitleF.setText(newNote.getTitle());
                noteBodyF.setText(newNote.getBody());

                Platform.runLater(() -> notesListView.getSelectionModel().clearSelection());
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
    public void add() throws IOException, InterruptedException {
        //Creates a note with text from the fields
        Note newNote = new Note(noteTitleF.getText(), noteBodyF.getText(), current_collection);
        var json = new ObjectMapper().writeValueAsString(newNote);  //JSON with the new note
        System.out.println(json);//Temporary for testing
       //Request body containing the created note
       var requestBody = Entity.entity(json, MediaType.APPLICATION_JSON);
       // Send the POST request
       var response = ClientBuilder.newClient()
               .target("http://localhost:8080/api/notes/create")
               .request(MediaType.APPLICATION_JSON)
               .post(requestBody);
        //Add notes to List<View>
        notes.add(newNote);
        //Clear the fields
        noteTitleF.clear();
        noteBodyF.clear();
        System.out.println("Add"); // Temporary for testing
    }

    /**
     * Removes a selected note
     */
    public void delete() {
        Note selectedNote = notesListView.getSelectionModel().getSelectedItem();
        if (selectedNote != null) {
            notes.remove(selectedNote);
        }

        System.out.println("Delete");  //Temporary for testing
    }

    /**
     * IDK yet
     */
    public void undo() {
        System.out.println("Undo");  //Temporary for testing
    }

    /**
     * Edits the title of currently selected note
     */
    public void titleEdit() {
        System.out.println("Title Edit");  //Temporary for testing
    }

    /**
     * Searches for a note based on text field input
     */
    public void search() {
        String search_text = searchF.textProperty().getValue();
        ArrayList<Long> match_indices = current_note.getMatchIndices(search_text);
        String titleHighlighted = current_note.getTitle();
        String bodyHighlighted = current_note.getBody();
        if (!match_indices.isEmpty()){
            if (match_indices.getFirst()==-1L && match_indices.size()==1L){
                System.out.println("Not found in \""+current_note.getTitle()+"\"");
            } else{ //parse in special way such that the found results are highlighted
                for (int i=match_indices.size()-1; i>=0; i--){//iterating from the back to not have to consider changes in index due to additions
                    System.out.println(match_indices.get(i));
                    if (match_indices.get(i)<titleHighlighted.length()){
                        titleHighlighted = titleHighlighted.substring(0, Math.toIntExact(match_indices.get(i)))
                                + "<mark>"
                                + search_text
                                + "</mark>"
                                + titleHighlighted.substring((int) (match_indices.get(i) + search_text.length()));
                    } else {
                        bodyHighlighted = bodyHighlighted.substring(0, (int) (match_indices.get(i)-titleHighlighted.length()))
                                + "<mark>"
                                + search_text
                                + "</mark>"
                                + bodyHighlighted.substring((int) (match_indices.get(i) -titleHighlighted.length() + search_text.length()));
                    }
                }
            }
        }
        titleHighlighted  = "<h1>" + renderer.render(parser.parse(titleHighlighted)) + "</h1>";
        bodyHighlighted = renderer.render(parser.parse(bodyHighlighted));
        String total_content = titleHighlighted + bodyHighlighted;
        markDownOutput.getEngine().loadContent(total_content);


    }

    public void setUpLanguages(){
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
        //todo - implement properly once database MR is sent
        selectCollectionBox.getItems().addAll(new Collection(current_server, "Default"));
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

        selectCollectionBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            System.out.println("Selected: " + newValue);
        });

    }

}
