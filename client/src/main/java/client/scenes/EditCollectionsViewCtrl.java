package client.scenes;

import client.utils.ServerUtils;
import com.google.inject.Inject;
import commons.Collection;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.util.Optional;

public class EditCollectionsViewCtrl {
    /**
     * The main screen to be shown.
     */
    private ScreenCtrl sc;

    private final HomeScreenCtrl homeScreenCtrl;

    private final ServerUtils localServerUtils;

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
     * @param localServerUtils the localServerUtils to access its methods
     */
    @Inject
    public EditCollectionsViewCtrl(ScreenCtrl screenCtrl,
                                   HomeScreenCtrl homeScreenCtrl,
                                   ServerUtils localServerUtils) {
        this.sc = screenCtrl;
        this.homeScreenCtrl = homeScreenCtrl;
        this.localServerUtils = localServerUtils;
    }

    /**
     * This method initialises the EditCollectionsView
     */
    public void initialise(){
        collections.setAll(homeScreenCtrl.currentServer.getCollections());
        setupCollectionsListView();
        collectionKeyboardShortcuts();

        Platform.runLater(() -> {
            clearFields();
            if (serverTextF != null && (serverTextF.getText() == null ||
                    serverTextF.getText().trim().isEmpty())) {
                serverTextF.setText("http://localhost:8080");
            }
        });
    }

    /**
     * Clears the fields in EditCollectionsView scene +
     * (except the server field - it's standard)
     */
    private void clearFields() {
        titleTextF.clear();
        collectionTextF.clear();
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
                        serverTextF.setText("http://localhost:8080");
                        collectionTextF.setText(newCollection.getCollectionPath());
                        Platform.runLater(() -> collectionsListView.getSelectionModel());
                    }
                });
    }

    /**
     * Saves all collections.
     */
    public void save() {
        Collection selectedCollection = collectionsListView.getSelectionModel().getSelectedItem();

        if (selectedCollection != null) {
            String newTitle = titleTextF.getText().trim();
            String newPath = collectionTextF.getText().trim();

            boolean changes = false; //check if changes were made
            if (!selectedCollection.getCollectionTitle().equals(newTitle)) {
                selectedCollection.setCollectionTitle(newTitle);
                changes = true;
            }
            if (!selectedCollection.getCollectionPath().equals(newPath)) {
                selectedCollection.setCollectionPath(newPath);
                changes = true;
            }
            if(!serverTextF.getText().trim().equals("http://localhost:8080")){
                showAlert(AlertType.ERROR, "Server not found",
                        "The server you selected can't be found," +
                                " please choose the default sever");
                return;
            }

            if(changes == true) {
                // Save the updated collection to the server
                syncCollectionWithServer(selectedCollection);
                showAlert(Alert.AlertType.INFORMATION, "Changes",
                        "Changes to the Collection: " +
                                selectedCollection.getCollectionTitle() + " have been saved.");
            }
        } else {
            showAlert(AlertType.INFORMATION, "Status",
                    "No collection was selected for changes....Saving....");
        }

        System.out.println("Saving");
        sc.showHome();
    }

    /**
     * Syncs (updates) a specific collection with the server to ensure consistency
     * @param collection - the collection that needs to be synced(updated) in the server
     */
    private void syncCollectionWithServer(Collection collection) {
        try {
            Collection updatedCollection = localServerUtils.syncCollectionWithServer(collection);
            if (updatedCollection != null) {
                // Replacing the collection in place to maintain order
                for (int i = 0; i < homeScreenCtrl.currentServer.getCollections().size(); i++) {
                    if (homeScreenCtrl.currentServer.getCollections().get(i).getCollectionId()
                            == updatedCollection.getCollectionId()) {
                        homeScreenCtrl.currentServer.getCollections().set(i, updatedCollection);
                        break;
                    }
                }

                // Refreshing the UI collections
                Platform.runLater(() -> {
                    homeScreenCtrl.loadCollectionsFromServer();
                    homeScreenCtrl.setUpCollections();
                });
            } else {
                System.err.println("Failed to sync collection " +
                        collection.getCollectionTitle() + ".");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "An error occurred while saving: " + e.getMessage());
        }
    }

    /**
     * Sets a collection to be the default_collection.
     */
    public void makeDefault() {
        Collection selectedCollection = collectionsListView.getSelectionModel().getSelectedItem();

        if (selectedCollection == null) {
            showAlert(Alert.AlertType.WARNING, "No collection selected",
                    "Please select a collection to make default.");
            return;
        }

        // Updating the previous default_collection in the server
        homeScreenCtrl.default_collection.setDefaultCollection(false);
        syncCollectionWithServer(homeScreenCtrl.default_collection);

        // Updating the new default_collection
        homeScreenCtrl.default_collection = selectedCollection;
        homeScreenCtrl.default_collection.setDefaultCollection(true);
        syncCollectionWithServer(selectedCollection);

        // Refresh UI
        Platform.runLater(() -> {
            collectionsListView.refresh();
            homeScreenCtrl.loadCollectionsFromServer();
            homeScreenCtrl.setUpCollections();
        });

        showAlert(Alert.AlertType.INFORMATION, "Default Collection Set",
                "The default collection has been set to: " +
                        selectedCollection.getCollectionTitle() + ".");
    }

    /**
     * Adds a collection.
     */
    @FXML
    public void addCollection() {
        // Check if any of the fields are empty
        if (titleTextF.getText().trim().isEmpty()) {
            showAlert(AlertType.WARNING, "Missing field",  "The Title is missing.");
            return;
        }
        if (serverTextF.getText().trim().isEmpty()) {
            showAlert(AlertType.WARNING, "Missing Field", "The Server is missing.");
            return;
        }
        if (collectionTextF.getText().trim().isEmpty()) {
            showAlert(AlertType.WARNING, "Missing Field", "The Collection Path is missing.");
            return;
        }
        if (!serverTextF.getText().trim().equals("http://localhost:8080")) {
            showAlert(AlertType.ERROR, "Server not found",
                    "The server you selected can't be found," +
                            " please choose the default sever");
            return;
        }

        // If all fields are filled, we proceed with collection creation
        String collectionTitle = titleTextF.getText().trim();
        String collectionPath = collectionTextF.getText().trim();

        Collection newCollection = new Collection(
                homeScreenCtrl.currentServer, collectionTitle,
                collectionPath, false
        );

        // Saving the collection to the server
        saveCollectiontoServer(newCollection);
    }

    /**
     * Saves the collection given as a param to the server
     * - connector between frontend and backend
     * @param collection - the collection that needs to be saved
     */
    private void saveCollectiontoServer(Collection collection) {
        try {
            Collection savedCollection = localServerUtils.saveCollectionToServer(collection);
            if (savedCollection != null) {
                homeScreenCtrl.loadCollectionsFromServer();
                homeScreenCtrl.setUpCollections();

                // Show an information pop-up
                Platform.runLater(() -> {
                    showAlert(Alert.AlertType.INFORMATION,
                            "Success", "Collection saved successfully.");
                    collections.add(savedCollection);
                });

                System.out.println("Collection " + savedCollection.getCollectionTitle()
                        + " has been added to the server.");
            } else {
                System.out.println("Failed to add collection. " +
                        "The server was unable to save the new collection->returned empty");
            }
        } catch (IOException e) {
            System.err.println("An error occurred while saving the collection: "
                    + e.getMessage());
        }
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

        if(selectedCollection.equals(homeScreenCtrl.default_collection)){
            showAlert(AlertType.ERROR, "Deleting the default collection", "The default collection "+
                    "can't be deleted, please select a different collection!");
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
                    homeScreenCtrl.currentServer.getCollections().remove(selectedCollection);
                    homeScreenCtrl.loadCollectionsFromServer();
                    homeScreenCtrl.setUpCollections();
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
     * Sets the ScreenCtrl instance for managing scene transitions.
     * @param screenCtrl - the ScreenCtrl instance to be assigned
     */
    public void setScreenCtrl(ScreenCtrl screenCtrl) {
        this.sc = screenCtrl;
    }

    /**
     * This method calls the keyboard shortcuts, separately when noteListView is
     * in focus because it did not work normally
     */
    public void collectionKeyboardShortcuts() {
        Platform.runLater(() -> {
            Scene currentScene = addB.getScene();
            if (currentScene == null) {
                System.out.println("Scene is not set!");
                return;
            }
            // Normal shortcuts
            currentScene.setOnKeyPressed(event -> handleKeyboardShortcuts(event));
            // Special case if focus is on the collections list view
            collectionsListView.setOnKeyPressed(event -> handleKeyboardShortcuts(event));
        });
    }

    /**
     * This method handles the keyboard shortcuts in edit collections view
     * @param event - the keys pressed
     */
    private void handleKeyboardShortcuts(KeyEvent event) {
        // Control + A for add
        if(event.isControlDown() && event.getCode() == KeyCode.A) {
            addCollection();
            event.consume();
        }
        // Control + Delete for delete
        if(event.isControlDown() && event.getCode() == KeyCode.DELETE) {
            deleteCollection();
            event.consume();
        }
        // Control + D for make default
        if(event.isControlDown() && event.getCode() == KeyCode.D) {
            makeDefault();
            event.consume();
        }
        // Control + S for save
        if(event.isControlDown() && event.getCode() == KeyCode.S) {
            save();
            event.consume();
        }
    }
}
