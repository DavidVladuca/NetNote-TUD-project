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
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

import java.util.Optional;

public class EditCollectionsViewCtrl {
    /**
     * The main screen to be shown.
     */
    private ScreenCtrl sc;

    private final HomeScreenCtrl homeScreenCtrl;

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

    /**
     * This method initialises the EditCollectionsView
     */
    public void initialise(){
        collections.setAll(homeScreenCtrl.currentServer.getCollections());
        setupCollectionsListView();
    }

    /**
     * Sets up the ListView in the front-end
     */
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
                        serverTextF.setText(newCollection.getServer().getURL());
                        // TODO: if we want multiple servers -> make it not hard-coded
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

//        homeScreenCtrl.loadCollectionsFromServer();
//        homeScreenCtrl.setUpCollections();
        System.out.println("Saving");

        sc.showHome();
    }
    /**
     * Sets a collection to be the default.
     */
    public void makeDefault() {
        String collectionTitle = titleTextF.getText();
        if (collectionTitle == null || collectionTitle.isEmpty()) {
            System.err.println("No collection selected to set as default.");
            // TODO: implement pop-up
        } else {
            // TODO: implement functionality
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
        Collection selectedCollection = collectionsListView.getSelectionModel().getSelectedItem();

        if (selectedCollection == null) {
            showAlert(AlertType.WARNING, "No Selection", "Please select a collection to delete.");
            System.err.println("No collection selected to delete."); //testing
            return;
        }

        // Pop-up for the confirmation of deletion
        Alert confirmationAlert = new Alert(AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Delete Confirmation");
        confirmationAlert.setHeaderText("Are you sure?");
        confirmationAlert.setContentText("Are you sure you want to delete the collection: "
                + selectedCollection.getCollectionTitle() + "?");

        // Wait for the user's response
        Optional<ButtonType> result = confirmationAlert.showAndWait();

        // If the user accepts the pop-up
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                HomeScreenCtrl.deleteCollectionFromServer(selectedCollection.getCollectionId());

                Platform.runLater(() -> {
                    collections.remove(selectedCollection);
                    collectionsListView.getSelectionModel().clearSelection();
                });

                showAlert(AlertType.INFORMATION,
                        "Delete Successful",
                        "Collection deleted successfully.");
            } catch (Exception e) {
                System.err.println("Error while deleting collection: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // If the user cancels, no action is taken
            System.out.println("Deletion cancelled by the user.");
        }
    }

    /**
     * Shows an alert dialog.
     * @param alertType the type of alert (e.g., INFORMATION, ERROR)
     * @param title     the title of the alert
     * @param message   the message to display
     */
    private void showAlert(AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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
