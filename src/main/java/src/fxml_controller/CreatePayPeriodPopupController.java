package src.fxml_controller;

import java.sql.Timestamp;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import src.backend.controller.PayPeriodController;
import src.handlers.FXMLHandler;
import src.shared.FXMLFilenames;

public class CreatePayPeriodPopupController extends FXMLControllerTemplate {

    PayPeriodController payPeriodController = new PayPeriodController();

    @FXML
    private DatePicker startDateInput;

    @FXML
    private Button submitButton;

    @FXML
    private void submitButtonOnClick(Event e) {
        // Creats a new Pay Period entry in the database
        payPeriodController.createPayPeriod(Timestamp.valueOf(startDateInput.getValue().atStartOfDay()));

        // Disables the menu item used to create an initial pay period
        ((ExpensesPageController) FXMLHandler.getFxmlController(FXMLFilenames.EXPENSES_PAGE))
                .setDisableCreatePayPeriodMenuItem(true);

        super.closeWindowOnClick(e);
    }
}
