package src;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import src.backend.ConnectH2;
import src.handlers.FXMLHandler;
import src.handlers.FXMLHandler.FXMLInstance;
import src.handlers.SoundHandler;
import src.handlers.WindowHandler;
import src.shared.FXMLFilenames;

public class App extends Application {

    @Override
    public void start(Stage stage) throws SQLException, IOException, URISyntaxException {

        // Create/Update the Database
        ConnectH2.executeSqlScripts();

        initFXML();
        WindowHandler.initWindows(stage);
        SoundHandler.initialize();

        stage.initStyle(StageStyle.TRANSPARENT);

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

     /**
     * Initializes the FXML files/controllers for the app. Each FXML is tracked in
     * the FXMLFilenames enum.
     * 
     * @throws IOException
     */
    public void initFXML() throws IOException {
        for (FXMLFilenames fxmlFilename : FXMLHandler.getFXML_FILENAMES()) {
            addFXMLInstance(fxmlFilename);
        }
    }

    /**
     * Creates an FXMLInstance for the specified FXML file, then stores in in a Map
     * (that relates FXML filenames to FXMLInstances).
     * 
     * @param fxmlFilename - the name of the FXML file used to create the
     *                     FXMLInstance
     * @throws IOException
     */
    public void addFXMLInstance(FXMLFilenames fxmlFilename) throws IOException {
        // Only create an FXMLInstance if it does not already exist
        if (!FXMLHandler.getFxmlInstances().containsKey(fxmlFilename)) {
            FXMLHandler.getFxmlInstances().put(fxmlFilename, new FXMLInstance(fxmlFilename.toString()));
        }
    }
}