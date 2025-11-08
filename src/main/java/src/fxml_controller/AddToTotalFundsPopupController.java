package src.fxml_controller;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import src.handlers.AppUserHandler;

public class AddToTotalFundsPopupController extends FXMLControllerTemplate {

    @FXML
    private TextField amountToAddInput;

    @FXML
    private Button submitButton;

    @FXML
    public void initialize() {
        allowOnlyDoublesInTextField(amountToAddInput);

        // Enable/Disable Submit button based on input value
        amountToAddInput.textProperty().addListener((_, _, newValue) -> {
            amountToAddTextInputOnKeyTyped();
        });
    }

    @FXML
    private void amountToAddTextInputOnKeyTyped() {
        submitButton
                .setDisable(amountToAddInput.getText().equals("") || amountToAddInput.getText() == null);
    }

    @FXML
    private void submitButtonOnClick(Event e) {

        AppUserHandler.updateTotalFunds(Double.parseDouble(amountToAddInput.getText()));

        resetInputValues();

        super.closeWindowOnClick(e);
    }

    /**
     * Resets the inputs to default values
     */
    private void resetInputValues() {
        amountToAddInput.clear();
    }
}
