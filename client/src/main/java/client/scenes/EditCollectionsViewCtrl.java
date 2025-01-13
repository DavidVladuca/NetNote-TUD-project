package client.scenes;

import com.google.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class EditCollectionsViewCtrl {
    /**
     * The main screen to be shown.
     */
    private ScreenCtrl sc;

    /**
     * Button to add a collection.
     */
    @FXML
    private Button addB;
    /**
     * Button to delete a collection.
     */
    private Button deleteB;
    /**
     * Button to make a collection the default.
     */
    private Button makeDefaultB;
    /**
     * Button to save the collections.
     */
    private Button saveB;
    /**
     * Text field to name a collection.
     */
    public TextField titleTextF;
    /**
     * Text field about server? todo.
     */
    public TextField serverTextF;
    /**
     * Text field to write about collections? todo.
     */
    public TextField collectionTextF;

    /**
     * Saves all collections.
     */
    public void save() {
        //todo - implement save
        System.out.println("Saving");
        sc.showHome();
    }
    /**
     * Sets a collection to be the default.
     */
    public void makeDefault() {

    }
    /**
     * Adds a collection.
     */
    public void addCollection() {

    }
    /**
     * Deletes a collection.
     */
    public void deleteCollection() {

    }
    /**
     * IDK - todo.
     */
    public void titleEntry() {

    }

    /**
     * IDK - todo.
     */
    public void serverEntry() {

    }

    /**
     * IDK - todo.
     */
    public void collectionEntry() {

    }

    /**
     * constructor.
     * @param screen - screen to be used
     */
    @Inject
    public EditCollectionsViewCtrl(final ScreenCtrl screen) {
        this.sc = screen;
    }

    /**
     * Sets the ScreenCtrl instance for managing scene transitions.
     * @param screenCtrl - the ScreenCtrl instance to be assigned
     */
    public void setScreenCtrl(ScreenCtrl screenCtrl) {
        this.sc = screenCtrl;
    }
}
