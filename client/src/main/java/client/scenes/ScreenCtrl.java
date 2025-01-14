package client.scenes;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Pair;

public class ScreenCtrl {
    private Stage primaryStage;
    private Scene homeScene;
    private Scene editCollectionScene;
    private EditCollectionsViewCtrl editCollectionsViewCtrl;

    public void init(Stage primaryStage, Pair<HomeScreenCtrl, Parent> home, Pair<EditCollectionsViewCtrl, Parent> editCollection) {
        this.primaryStage = primaryStage;
        this.homeScene = new Scene(home.getValue());
        this.editCollectionScene = new Scene(editCollection.getValue());
        this.editCollectionsViewCtrl = editCollection.getKey();
        showHome();
        primaryStage.show();
    }

    public void showHome() {
        if(primaryStage == null) {throw new IllegalStateException("Primary stage is not initialized");}
        primaryStage.setScene(homeScene);
    }

    public void showEditCollection() {
        if(primaryStage == null) {throw new IllegalStateException("Primary stage is not initialized");}

        if (editCollectionsViewCtrl != null) {
            this.editCollectionsViewCtrl.initialise();
        } else {
            System.err.println("EditCollectionsViewCtrl is not set!");
        }

        primaryStage.setScene(editCollectionScene);
    }
}
