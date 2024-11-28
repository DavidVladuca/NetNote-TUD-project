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

    public String title = "";
    public String content = "";

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
        System.out.println("Search");
    }
}
