package client.scenes;

import client.HomeScreen;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.web.WebView;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

import java.net.URL;

public class HomeScreenCtrl {
    @FXML
    public Button addB;
    public Button minusB;
    public Button undoB;
    public TextField noteTitleF;
    public TextArea noteBodyF;
    public TextField searchF;
    public WebView markDownOutput;

    public final Parser parser = Parser.builder().build();
    public final HtmlRenderer renderer = HtmlRenderer.builder().build();

    /**
     * This method initializes the controller
     * and sets up the listener for the text area that the user types in.
     */
    @FXML
    public void initialize() {
        noteBodyF.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                // MD -> HTML
                String html = renderer.render(parser.parse(newValue));
                // WebView is updated based on the HTML file
                markDownOutput.getEngine().loadContent(html);
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
        System.out.println("Search");
    }
}
