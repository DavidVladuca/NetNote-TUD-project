package client.scenes;

import ch.qos.logback.core.net.server.Client;
import client.HomeScreen;
import commons.Language;
import commons.LanguageOptions;
import commons.Note;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.database.NoteRepository;
import jakarta.ws.rs.client.*;
import jakarta.ws.rs.core.MediaType;


import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import commons.Note;
import commons.Collection;
import commons.Server;


@Component
public class HomeScreenCtrl {
    //todo - for all methods, change strings title and body to getting them from the note instead
    @FXML
    public Button addB;
    public Button deleteB;
    public Button undoB;
    public LanguageOptions lang_ops= new LanguageOptions(); //todo - have this not be initialized every time
    public ChoiceBox<Language> selectLangB = new ChoiceBox<Language>();
    public TextField noteTitleF;
    public TextArea noteBodyF;
    public TextField searchF;
    public WebView markDownOutput;

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
        selectLangB.getItems().setAll(lang_ops.getLanguages());//TODO - check if this is the correct location
        System.out.println(selectLangB.getItems().getFirst().getName());
        selectLangB.setValue(selectLangB.getItems().getFirst());
        selectLangB.setConverter(new StringConverter<>() {
            @Override
            public String toString(Language language) {
                return language.getAbbr();
            }

            @Override
            public Language fromString(String s) {
                return null; //todo - implement (make for loop until the right option is found in the options list)
            }
        });


        markDownTitle();

        markDownContent();

        setupNotesListView();

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
     * Adds a new note
     */
    public void add() throws IOException, InterruptedException {
       //Temporary for testing
        System.out.println("Add");
        Note newNote = new Note(noteTitleF.getText(), noteBodyF.getText(), null);

            // Add the note to your list or process it

        //var om = new ObjectMapper ();
        var json = new ObjectMapper().writeValueAsString(newNote);
        //System.out.println(new ObjectMapper().readValue(json, Note.class));
        System.out.println(json);

//        var requestBody = Entity.entity(json, MediaType.APPLICATION_JSON);
//
//        // Send the POST request
//        var response = ClientBuilder.newClient()
//                .target("localhost:8080/api/notes/")
//                .request(MediaType.APPLICATION_JSON)
//                .post(requestBody);

        notes.add(newNote);
        //saveNoteToDatabase(newNote);
        noteTitleF.clear();
        noteBodyF.clear();

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
        ArrayList<Integer> match_indices = current_note.getMatchIndices(search_text);
        String title = current_note.getTitle();
        String titleAndContent = current_note.getTitle();
        String bodyHighlighted = current_note.getBody();
        if (!match_indices.isEmpty()){
            if (match_indices.getFirst()==-1 && match_indices.size()==1){
                System.out.println("Not found in "+title);
            } else{ //parse in special way such that the found results are highlighted
                for (int i=match_indices.size()-1; i>=0; i--){//iterating from the back to not have to consider changes in index due to additions
                    bodyHighlighted = bodyHighlighted.substring(0, match_indices.get(i))
                            +"<mark>"
                            +search_text
                            +"</mark>"
                            +bodyHighlighted.substring(match_indices.get(i)+search_text.length());
                }
            }
        }
        content = renderer.render(parser.parse(bodyHighlighted));
        titleAndContent += content;
        markDownOutput.getEngine().loadContent(titleAndContent);


    }

    /**
     * Allows for language selection
     */
    public void select_lang(){

        System.out.println(selectLangB.getItems().toString());
    }

}
