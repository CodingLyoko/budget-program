package src.fxml_controller;

import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import lombok.Getter;
import lombok.Setter;
import src.backend.controller.PayPeriodController;
import src.backend.model.PayPeriod;
import src.handlers.AppUserHandler;
import src.shared.PayPeriodFrequency;

public class PayPeriodFrequencySelectionPopupController extends FXMLControllerTemplate {

    private PayPeriodController payPeriodController = new PayPeriodController();

    @Getter
    @Setter
    public Boolean submitSuccess = false;

    @FXML
    private ChoiceBox<PayPeriodFrequency> payPeriodFreqChoiceBoxInput;

    @FXML
    private Button submitButton;

    @FXML
    public void initialize() {
        payPeriodFreqChoiceBoxInput.setItems(FXCollections.observableArrayList(PayPeriodFrequency.values()));
        payPeriodFreqChoiceBoxInput.setValue(AppUserHandler.getAppUserInstance().getPayPeriodFrequency());
    }

    @FXML
    private void submitButtonOnClick(Event e) {
        AppUserHandler.updatePayPeriodFreq(payPeriodFreqChoiceBoxInput.getValue());

        // Checks if there are any pay periods to update
        if (payPeriodController.getNumPayPeriods() != 0) {
            PayPeriod currentPayPeriod = payPeriodController.getCurrentPayPeriod();
            currentPayPeriod.setEndDate(PayPeriodFrequency.getEndDate(currentPayPeriod.getStartDate()));

            payPeriodController.udpateCurrentPayPeriod(currentPayPeriod);
        }

        submitSuccess = true;

        super.closeWindowOnClick(e);
    }
}
