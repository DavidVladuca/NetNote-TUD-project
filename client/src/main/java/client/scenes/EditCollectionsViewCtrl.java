package client.scenes;

import com.google.inject.Inject;
import commons.Collection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class EditCollectionsViewCtrl {
    /**
     * The main screen to be shown.
     */
    private ScreenCtrl sc;

    private HomeScreenCtrl homeScreenCtrl;

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
     * List of all the collections in the application. Useful for the Observable list.
     */
    public ListView<Collection> collectionsListView;
    /**
     * Display of all the collections in the application.
     */
    private final ObservableList<Collection> collections = FXCollections
            .observableArrayList();

    private Collection currentCollection;

    /**
     * Constructor for EditCollectionsViewCtrl.
     * @param screenCtrl       the ScreenCtrl for navigation
     * @param homeScreenCtrl   the HomeScreenCtrl to access the current server
     */
    @Inject
    public EditCollectionsViewCtrl(ScreenCtrl screenCtrl, HomeScreenCtrl homeScreenCtrl) {
        this.sc = screenCtrl;
        this.homeScreenCtrl = homeScreenCtrl;
    }

    public void initialise(){
        collections.setAll(homeScreenCtrl.currentServer.getCollections());
        System.out.println("Collections retrieved: " + homeScreenCtrl.currentServer.getCollections());
        setupCollectionsListView();
    }

    private void setupCollectionsListView() {
        // Bind the ObservableList to the ListView
        collectionsListView.setItems(collections);

        // Set a custom cell factory to display the collection's title in each ListView cell
        collectionsListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Collection collection, boolean empty) {
                super.updateItem(collection, empty);
                setText(empty || collection == null ? null : collection.getCollectionTitle());
            }
        });

        // Add a listener to handle selection in the ListView
        collectionsListView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldCollection, newCollection) -> {
                    if (newCollection != null) {
                        titleTextF.setText(newCollection.getCollectionTitle());
                        serverTextF.setText(newCollection.getServer().getURL());// TODO: if we want multiple servers -> make it not hard-coded
                        collectionTextF.setText(newCollection.getCollectionPath());
                        Platform.runLater(() -> collectionsListView.getSelectionModel());
                    }
                });
    }

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
        String collectionTitle = titleTextF.getText();
        if (collectionTitle == null || collectionTitle.isEmpty()) {
            System.err.println("No collection selected to set as default."); // TODO: implement pop-up
        } else {

        }
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
     * Sets the ScreenCtrl instance for managing scene transitions.
     * @param screenCtrl - the ScreenCtrl instance to be assigned
     */
    public void setScreenCtrl(ScreenCtrl screenCtrl) {
        this.sc = screenCtrl;
    }
}
