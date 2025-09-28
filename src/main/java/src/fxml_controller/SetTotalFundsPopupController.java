package src.fxml_controller;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import src.handlers.AppUserHandler;

public class SetTotalFundsPopupController extends FXMLControllerTemplate {

    @FXML
    private TextField newTotalFundsTextInput;

    @FXML
    private Button submitButton;

    @FXML
    private Button clearButton;

    @FXML
    public void initialize() {
        newTotalFundsTextInput.setText(""); // Clears any previous text
        allowOnlyDoublesInTextField(newTotalFundsTextInput);
        newTotalFundsTextInputOnKeyTyped();
    }

    @FXML
    private void newTotalFundsTextInputOnKeyTyped() {
        submitButton
                .setDisable(newTotalFundsTextInput.getText().equals("") || newTotalFundsTextInput.getText() == null);
    }

    @FXML
    private void submitButtonOnClick(Event e) {
        AppUserHandler.setTotalFunds(Double.parseDouble(newTotalFundsTextInput.getText()));
        super.closeWindowOnClick(e);
    }

    @FXML
    private void clearButtonOnClick() {
        newTotalFundsTextInput.clear();
    }
}
