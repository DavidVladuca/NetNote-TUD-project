package client;

import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Application;

public class HomeScreen extends Application {
    private static final Injector INJECTOR = Guice.createInjector( new MyModule());
    private static final MyFXML FXML = new MyFXML ( INJECTOR );
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
}
