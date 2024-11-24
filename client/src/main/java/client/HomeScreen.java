package client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.application.Application;
import java.net.URL;

public class HomeScreen extends Application {
    /**
     * Launches the screen
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Creates the screen based on fxml file HomeScreen
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        var fxml = new FXMLLoader();
        fxml.setLocation(HomeScreen.class.getResource("/client/homeScreen.fxml"));
        var scene = new Scene(fxml.load());
        primaryStage.setScene(scene);
        primaryStage.show();
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
        System.out.println("Search");  //Temporary for testing
    }

    @FXML
    public Button addB;
    public Button minusB;
    public Button undoB;
    public TextField noteTitleF;
    public TextArea noteBodyF;
    public TextField searchF;
}
