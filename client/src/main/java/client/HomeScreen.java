package client;

import client.scenes.AddQuoteCtrl;
import client.scenes.HomeScreenCtrl;
import client.scenes.HomeScreenOverviewCtrl;
import client.scenes.QuoteOverviewCtrl;
import client.utils.ServerUtils;
import com.google.inject.Injector;
import javafx.stage.Stage;
import javafx.application.Application;

import static com.google.inject.Guice.createInjector;

public class HomeScreen extends Application {
    private static final Injector INJECTOR = createInjector(new MyModule());
    private static final MyFXML FXML = new MyFXML(INJECTOR);

    /**
     * Launches the screen
     * @param args
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Creates the screen based on fxml file HomeScreen
     * @param primaryStage
     * @throws Exception
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        var serverUtils = INJECTOR.getInstance(ServerUtils.class);
        if (!serverUtils.isServerAvailable()) {
            var msg = "Server needs to be started before the client, but it does not seem to be available. Shutting down.";
            System.err.println(msg);
            return;
        }

        var overview = FXML.load(HomeScreenOverviewCtrl.class, "client", "scenes", "homeScreenOverview.fxml");
        var homeScreen = FXML.load(HomeScreenCtrl.class, "client", "scenes", "homeScreen.fxml");

        var HomeScreenCtrl = INJECTOR.getInstance(HomeScreenCtrl.class);
        HomeScreenCtrl.initialize();
    }
}
