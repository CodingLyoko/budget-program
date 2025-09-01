package src.controllers;

import java.net.URISyntaxException;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import javafx.util.converter.IntegerStringConverter;
import src.handlers.WindowHandler;
import src.shared.FXMLFilenames;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;

public class FXMLControllerTemplate {

    /* ==================== WINDOW/SCENE FUNCTIONS ==================== */
    @FXML
    private void openPopup(Event e) {
        WindowHandler.showPopupWindow(FXMLFilenames.valueOf(getFXMLFilename(e)));
    }

    protected void openPopup(FXMLFilenames fxmlName) {
        WindowHandler.showPopupWindow(fxmlName);
    }

    @FXML
    protected void changeScene(Event e) {
        WindowHandler.changeScene(FXMLFilenames.valueOf(getFXMLFilename(e)), ((ImageView) e.getSource()).getScene());
    }

    protected void changeScene(FXMLFilenames fxmlName) {
        WindowHandler.changeScene(fxmlName, WindowHandler.getCurrentScene());
    }

    @FXML
    protected void closeWindowOnClick(Event e) {
        ((Stage) ((Node) e.getSource()).getScene().getWindow()).close();
    }

    private String getFXMLFilename(Event object) {
        String imageUrl = ((ImageView) object.getSource()).getImage().getUrl();

        Pattern pattern = Pattern.compile("\\w*(?=_icon)");
        Matcher matcher = pattern.matcher(imageUrl);
        matcher.find();

        return matcher.group(0).toUpperCase();
    }

    /* ==================== IMAGE MANIPULATION FUNCTIONS ==================== */
    /**
     * Changes an image to it's highlighted version when the mouse enters the image.
     * Highlighted image MUST adhere to naming conventions.
     * 
     * @param e
     * @throws URISyntaxException
     */
    @FXML
    protected void highlightOnMouseEntered(Event e) throws URISyntaxException {
        ((ImageView) e.getSource())
                .setImage(new Image(getClass().getResource(getHighlightedImageFilename(e)).toURI().toString()));
    }

    @FXML
    protected void highlightOnMouseExit(Event e) throws URISyntaxException {
        ((ImageView) e.getSource())
                .setImage(new Image(getClass().getResource(getImageFilename(e)).toURI().toString()));
    }

    protected String getHighlightedImageFilename(Event object) {
        String imageUrl = ((ImageView) object.getSource()).getImage().getUrl();

        Pattern pattern = Pattern.compile("/images/\\w*");
        Matcher matcher = pattern.matcher(imageUrl);
        matcher.find();

        return matcher.group(0) + "_highlighted.png";
    }

    protected String getImageFilename(Event object) {
        String imageUrl = ((ImageView) object.getSource()).getImage().getUrl();

        Pattern pattern = Pattern.compile("/images/\\w*_");
        Matcher matcher = pattern.matcher(imageUrl);
        matcher.find();

        return matcher.group(0).substring(0, matcher.group(0).length() - 1) + ".png";
    }

    @FXML
    protected void highlightLabelOnMouseEntered(Event e) throws URISyntaxException {
        Label label = (Label) e.getSource();
        label.setGraphic(new ImageView(getClass().getResource(getNodeHighlightImageName(label)).toURI().toString()));

        // Sets the image's dimensions to that of the Label's
        ((ImageView) label.getGraphic()).setFitHeight(label.getHeight());
        ((ImageView) label.getGraphic()).setFitWidth(label.getWidth());
    }

    @FXML
    protected void highlightLabelOnMouseExit(Event e) throws URISyntaxException {
        Label label = (Label) e.getSource();
        label.setGraphic(new ImageView(getClass().getResource(getNodeImageName(label)).toURI().toString()));

        // Sets the image's dimensions to that of the Label's
        ((ImageView) label.getGraphic()).setFitHeight(label.getHeight());
        ((ImageView) label.getGraphic()).setFitWidth(label.getWidth());
    }

    @FXML
    protected void highlightMenuButtonOnMouseEntered(Event e) throws URISyntaxException {
        MenuButton menuButton = (MenuButton) e.getSource();

        double imageHeight = menuButton.getGraphic().getBoundsInParent().getHeight();
        double imageWidth = menuButton.getGraphic().getBoundsInParent().getWidth();

        menuButton.setGraphic(
                new ImageView(getClass().getResource(getNodeHighlightImageName(menuButton)).toURI().toString()));

        // Sets the image's dimensions to that of the Label's
        ((ImageView) menuButton.getGraphic()).setFitHeight(imageHeight);
        ((ImageView) menuButton.getGraphic()).setFitWidth(imageWidth);
    }

    @FXML
    protected void highlightMenuButtonOnMouseExit(Event e) throws URISyntaxException {
        MenuButton menuButton = (MenuButton) e.getSource();

        double imageHeight = menuButton.getGraphic().getBoundsInParent().getHeight();
        double imageWidth = menuButton.getGraphic().getBoundsInParent().getWidth();

        menuButton.setGraphic(new ImageView(getClass().getResource(getNodeImageName(menuButton)).toURI().toString()));

        // Sets the image's dimensions to that of the Label's
        ((ImageView) menuButton.getGraphic()).setFitHeight(imageHeight);
        ((ImageView) menuButton.getGraphic()).setFitWidth(imageWidth);
    }

    private String getNodeHighlightImageName(Node node) {
        StringBuilder imageName = new StringBuilder();
        imageName.append("/images/");
        imageName.append(getImageFilename(node.getId()));
        imageName.append("_highlighted.png");

        return imageName.toString();
    }

    private String getNodeImageName(Node node) {
        StringBuilder imageName = new StringBuilder();
        imageName.append("/images/");
        imageName.append(getImageFilename(node.getId()));
        imageName.append(".png");

        return imageName.toString();
    }

    /**
     * Get the relevant image filename for a given FXML Node
     * 
     * @param nodeId - ID of the Node (should be camelCase of Image filename)
     * @return The name of the associated image filename, as a String
     */
    private String getImageFilename(String nodeId) {

        StringBuilder imageFilename = new StringBuilder(nodeId);
        int charIndexToLowerCase = findFirstCapitalLetterIndex(0, imageFilename.toString());
        int underscoresInserted = charIndexToLowerCase == 0 ? 0 : 1;

        // If there are multiple Uppercase Characters, insert a '_' for each
        do {
            if (charIndexToLowerCase != 0) {
                imageFilename.insert(charIndexToLowerCase, "_");
                underscoresInserted++;
            }

            charIndexToLowerCase = findFirstCapitalLetterIndex(charIndexToLowerCase + underscoresInserted,
                    imageFilename.toString());
        } while (charIndexToLowerCase != 0);

        return imageFilename.toString().toLowerCase();
    }

    /**
     * Finds the index of the first instance of a capital letter within a given
     * String, starting from the given startingIndex
     * 
     * @param startingIndex - The index of the String to begin the search
     * @param s             - The String that is being searched
     * @return Index of the first instance of a capital letter, or Zero (0) if no
     *         capital letter was found
     */
    private int findFirstCapitalLetterIndex(int startingIndex, String s) {

        for (int i = startingIndex; i < s.length(); i++) {
            if (Character.isUpperCase(s.codePointAt(i))) {
                return i;
            }
        }

        // No instance of a capital character found
        return 0;
    }

    protected void disableIcon(Node icon) throws URISyntaxException {
        ((ImageView) icon)
                .setImage(new Image(getClass().getResource(getDisabledImageFilename(icon)).toURI().toString()));

        icon.setDisable(true);
    }

    private String getDisabledImageFilename(Node node) {
        String imageUrl = ((ImageView) node).getImage().getUrl();

        Pattern pattern = Pattern.compile("/images/\\w*");
        Matcher matcher = pattern.matcher(imageUrl);
        matcher.find();

        return matcher.group(0) + "_disabled.png";
    }

    /* ==================== INPUT FIELD HELPER FUNCTIONS ==================== */
    // Filter to ensure only Integer values are allowed in an FXML TextField
    protected static UnaryOperator<Change> integerFilter = change -> {
        String newValue = change.getControlNewText();

        return (Pattern.matches("\\d*", newValue) ? change : null);
    };

    /**
     * Filters an FXML TextField to allow only Integer values (e.g., 0-9)
     * 
     * @param textField - TextField that you want to apply the filter to
     */
    protected void allowOnlyIntegersInTextField(TextField textField) {
        textField.setTextFormatter(new TextFormatter<>(new IntegerStringConverter(), null, integerFilter));
    }

    /**
     * Limits the number of characters that can be entered into an FXML TextField
     * 
     * @param textField          - The TextField that is being limited in character
     *                           length
     * @param maxCharacterLength - The maximum number of characters allowed in the
     *                           FXML TextField
     */
    protected void limitMaxCharacterLength(TextField textField, int maxCharacterLength) {
        textField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (textField.getText().length() == maxCharacterLength) {
                event.consume();
            }
        });
    }

    /**
     * Limits the number of characters that can be entered into an FXML TextArea
     * 
     * @param textArea           - The TextArea that is being limited in character
     *                           length
     * @param maxCharacterLength - The maximum number of characters allowed in the
     *                           FXML TextArea
     */
    protected void limitMaxCharacterLength(TextArea textArea, int maxCharacterLength) {
        textArea.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            if (textArea.getText().length() == maxCharacterLength) {
                event.consume();
            }
        });
    }
}
