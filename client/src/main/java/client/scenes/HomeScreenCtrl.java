package client.scenes;

import client.HomeScreen;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import org.checkerframework.checker.units.qual.C;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.net.URL;
import java.util.ArrayList;
import commons.Note;
import commons.Collection;
import commons.Server;

public class HomeScreenCtrl {
    //todo - for all methods, change strings title and body to getting them from the note instead
    @FXML
    public Button addB;
    public Button minusB;
    public Button undoB;
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

        markDownTitle();

        markDownContent();

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
     * @param path
     * @return
     */
    private static URL getLocation(String path) {
        return HomeScreen.class.getClassLoader().getResource(path);
    }

    /**
     * Adds a new note
     */
    public void add() {
        System.out.println("Add"); //Temporary for testing
    }

    /**
     * Removes a selected note
     */
    public void minus() {
        System.out.println("Minus");  //Temporary for testing
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

}
