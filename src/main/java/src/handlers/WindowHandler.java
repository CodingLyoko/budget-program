package src.handlers;

import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;
import src.shared.Constants;
import src.shared.FXMLFilenames;

public class WindowHandler {

    private static Scene mainScene;

    private static Scene popupScene;
    private static Stage popupStage = new Stage();

    private static Scene alertScene;
    private static Stage alertStage = new Stage();

    private static double initialX = 0;
    private static double initialY = 0;

    private WindowHandler() {
    }

    public static void initWindows(Stage mainStage) {
        // Main App Window
        mainScene = new Scene(FXMLHandler.getFxmlInstances().get(FXMLFilenames.EXPENSES_PAGE).root); // Default Scene
        mainScene.getStylesheets().add(WindowHandler.class.getResource("/css/application.css").toExternalForm());

        mainStage.setTitle(Constants.APP_NAME);
        mainStage.setScene(mainScene);

        makeWindowDraggable(mainScene, mainStage);

        // Popup Window
        popupScene = new Scene(FXMLHandler.getFxmlInstances().get(FXMLFilenames.SET_TOTAL_FUNDS_POPUP).root);
        // Default Scene
        popupScene.getStylesheets().add(WindowHandler.class.getResource("/css/application.css").toExternalForm());

        popupStage.initOwner(mainStage); // Sets the Main Window as the Owner of this Stage
        popupStage.initModality(Modality.WINDOW_MODAL); // Disables the Main Window while this one is open
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

        scene.setOnMousePressed(m -> {
            if (m.getButton() == MouseButton.PRIMARY) {
                scene.setCursor(Cursor.MOVE);
                initialX = (int) (stage.getX() - m.getScreenX());
                initialY = (int) (stage.getY() - m.getScreenY());
            }
        });

        scene.setOnMouseDragged(m -> {
            if (m.getButton() == MouseButton.PRIMARY) {
                stage.setX(m.getScreenX() + initialX);
                stage.setY(m.getScreenY() + initialY);
            }
        });

        scene.setOnMouseReleased(_ -> scene.setCursor(Cursor.DEFAULT));
    }

    public static Scene getCurrentScene() {
        return mainScene;
    }
}
