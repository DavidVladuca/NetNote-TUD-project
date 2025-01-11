package client;

import client.scenes.EditCollectionsViewCtrl;
import client.scenes.HomeScreenCtrl;
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

        FXMLLoader homeLoader = new FXMLLoader(HomeScreenCtrl.class.getResource("/client/homeScreen.fxml"), bundle);
        HomeScreenCtrl sc = INJECTOR.getInstance(HomeScreenCtrl.class);
        homeLoader.setController(sc);
        Parent home = homeLoader.load();
        HomeScreenCtrl homeController = homeLoader.getController();

        // Load the edit collections screen FXML with the resource bundle
        FXMLLoader editCollectionsLoader = new FXMLLoader(EditCollectionsViewCtrl.class.getResource("/client/editCollectionsScreen.fxml"), bundle);
        EditCollectionsViewCtrl sc_screen = INJECTOR.getInstance(EditCollectionsViewCtrl.class);
        editCollectionsLoader.setController(sc_screen);
        Parent editCollections = editCollectionsLoader.load();
        EditCollectionsViewCtrl editCollectionsController = editCollectionsLoader.getController();

        // Get an instance of ScreenCtrl and initialize it with the loaded screens

        sc.init(primaryStage,
                new Pair<>(homeController, home),
                new Pair<>(editCollectionsController, editCollections));
    }




    private static URL getLocation(String path) {return Scene.class.getClassLoader().getResource(path);}
}
