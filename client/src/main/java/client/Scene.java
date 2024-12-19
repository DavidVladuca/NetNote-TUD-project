package client;

import client.scenes.EditCollectionsViewCtrl;
import client.scenes.HomeScreenCtrl;
import client.scenes.ScreenCtrl;
import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.stage.Stage;

import java.net.URL;

public class Scene extends Application {
    private static final Injector INJECTOR = Guice.createInjector(new MyModule());
    private static final MyFXML FXML = new MyFXML(INJECTOR);

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) throws Exception {
        var home = FXML.load(HomeScreenCtrl.class, "client", "homeScreen.fxml");
        var editCollections = FXML.load(EditCollectionsViewCtrl.class, "client", "editCollectionsScreen.fxml");
        var sc = INJECTOR.getInstance(ScreenCtrl.class);
        sc.init(primaryStage, home, editCollections);
    }

    private static URL getLocation(String path) {return Scene.class.getClassLoader().getResource(path);}
}
