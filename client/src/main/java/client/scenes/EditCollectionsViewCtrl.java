package client.scenes;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class EditCollectionsViewCtrl {
    private final ScreenCtrl sc;

    @FXML
    public Button addB;
    public Button deleteB;
    public Button makeDefaultB;
    public Button saveB;
    public TextField titleTextF;
    public TextField serverTextF;
    public TextField collectionTextF;

    public void save() {
        //todo - implement save
        System.out.println("Saving");
        sc.showHome();
    }
    public void makeDefault() {}
    public void addCollection() {}
    public void deleteCollection() {}
    public void titleEntry() {}
    public void serverEntry() {}
    public void collectionEntry() {}

    @Inject
    public EditCollectionsViewCtrl(ScreenCtrl sc) {this.sc = sc;}
}
