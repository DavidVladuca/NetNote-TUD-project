package client;

import client.scenes.EditCollectionsViewCtrl;
import client.scenes.HomeScreenCtrl;
import client.scenes.ScreenCtrl;
import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class Scene extends Application {
    private static final Injector INJECTOR = Guice.createInjector(new MyModule());

    Locale locale;


    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) throws Exception {
        locale = Locale.of("en", "US");
        ResourceBundle bundle = ResourceBundle.getBundle("MyBundle", locale);

        // Load the home screen FXML
        FXMLLoader homeLoader = new FXMLLoader(HomeScreenCtrl.class.getResource("/client/homeScreen.fxml"), bundle);
        HomeScreenCtrl homeCtrl = INJECTOR.getInstance(HomeScreenCtrl.class);
        homeLoader.setController(homeCtrl);
        Parent home = homeLoader.load();

        // Load the edit collections screen FXML
        FXMLLoader editCollectionsLoader = new FXMLLoader(EditCollectionsViewCtrl.class.getResource("/client/editCollectionsScreen.fxml"), bundle);
        EditCollectionsViewCtrl editCtrl = INJECTOR.getInstance(EditCollectionsViewCtrl.class);
        editCollectionsLoader.setController(editCtrl);
        Parent editCollections = editCollectionsLoader.load();

        // Initialize ScreenCtrl and set up scenes
        ScreenCtrl screenCtrl = INJECTOR.getInstance(ScreenCtrl.class);
        screenCtrl.init(primaryStage, new Pair<>(homeCtrl, home), new Pair<>(editCtrl, editCollections));

        // Pass ScreenCtrl to controllers for navigation
        homeCtrl.setScreenCtrl(screenCtrl);
        editCtrl.setScreenCtrl(screenCtrl);

        // Show the primary stage
        primaryStage.show();
    }





    private static URL getLocation(String path) {return Scene.class.getClassLoader().getResource(path);}
}
