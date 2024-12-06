package client;

import com.google.inject.Guice;
import com.google.inject.Injector;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class EditCollectionsView extends Application {
    private static final Injector INJECTOR = Guice.createInjector(new MyModule());
    private static final MyFXML FXML = new MyFXML(INJECTOR);

    @Override
    public void start(Stage primaryStage) throws Exception {
        var editCollectionsView = FXML.load(EditCollectionsView.class, "client", "editCollectionsScreen.fxml");
        var scene = new Scene(editCollectionsView.getValue());
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
