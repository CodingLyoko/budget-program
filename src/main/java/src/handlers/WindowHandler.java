package src.handlers;

import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import src.shared.Constants;
import src.shared.FXMLFilenames;

public class WindowHandler {

    private static Scene mainScene;
    private static final int MAIN_WINDOW_WIDTH = 900;
    private static final int MAIN_WINDOW_HEIGHT = 1000;

    private static Scene popupScene;
    private static Stage popupStage = new Stage();

    private static Scene alertScene;
    private static Stage alertStage = new Stage();

    private static double gapX = 0;
    private static double gapY = 0;

    private WindowHandler() {
    }

    public static void initWindows(Stage mainStage) {
        // Main App Window
        mainScene = new Scene(FXMLHandler.getFxmlInstances().get(FXMLFilenames.EXAMPLE_FXML_FILE).root); // Default Scene
        mainScene.setFill(Color.TRANSPARENT);
        mainScene.getStylesheets().add(WindowHandler.class.getResource("/css/application.css").toExternalForm());

        mainStage.setTitle(Constants.APP_NAME);
        mainStage.setScene(mainScene);
        mainStage.setHeight(MAIN_WINDOW_HEIGHT);
        mainStage.setWidth(MAIN_WINDOW_WIDTH);

        makeWindowDraggable(mainScene, mainStage);

        // Popup Window
        popupScene = new Scene(FXMLHandler.getFxmlInstances().get(FXMLFilenames.EXAMPLE_FXML_FILE).root); // Default Scene
        popupScene.setFill(Color.TRANSPARENT);
        popupScene.getStylesheets().add(WindowHandler.class.getResource("/css/application.css").toExternalForm());

        popupStage.initOwner(mainStage); // Sets the Main Window as the Owner of this Stage
        popupStage.initModality(Modality.WINDOW_MODAL); // Disables the Main Window while this one is open
        popupStage.initStyle(StageStyle.TRANSPARENT);
        popupStage.setScene(popupScene);

        makeWindowDraggable(popupScene, popupStage);
        
        mainStage.centerOnScreen();
    }

    public static void showPopupWindow(FXMLFilenames fxmlFilename) {
        changeScene(fxmlFilename, popupScene);
        popupStage.showAndWait();
    }

    public static void showAlertPopup(FXMLFilenames fxmlFilename) {
        changeScene(fxmlFilename, alertScene);
        alertStage.showAndWait();
    }

    public static void changeScene(FXMLFilenames fxmlFilename, Scene scene) {
        scene.setRoot(FXMLHandler.getFXMLRoot(fxmlFilename));
    }

    private static void makeWindowDraggable(Scene scene, Stage stage) {

        scene.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - gapX);
            stage.setY(event.getScreenY() - gapY);
        });
        scene.setOnMouseMoved(event -> {
            gapX = event.getScreenX() - stage.getX();
            gapY = event.getScreenY() - stage.getY();
        });
    }

    public static Scene getCurrentScene() {
        return mainScene;
    }
}
