package src.fxml_controller;

import java.util.HashMap;
import java.util.Map;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import src.backend.controller.ExpenseController;
import src.backend.model.Expense;
import src.handlers.FXMLHandler;
import src.shared.FXMLFilenames;

public class AddAmountSpentPopupController extends FXMLControllerTemplate {

    ExpenseController expenseController = new ExpenseController();

    Map<String, String> inputValues = new HashMap<>();

    Expense updatedExpense = new Expense();

    @FXML
    private TextField amountToAddInput;

    @FXML
    private Button submitButton;

    @FXML
    public void initialize() {
        allowOnlyDoublesInTextField(amountToAddInput);
        setInputListeners();
    }

    /**
     * Adds listeners to each required input field to check if they are are
     * complete.
     */
    private void setInputListeners() {
        instantiateInputValueMap();

        amountToAddInput.textProperty().addListener((_, _, newValue) -> {
            inputValues.put("amountToSpend", newValue);
            checkInputsCompleted();
        });
    }

    /**
     * Populates a Map to contain the following:
     * - A Key representing an input field
     * - A value corresponding to the value in the input field
     */
    private void instantiateInputValueMap() {
        inputValues.put("amountToSpend", "");
    }

    /**
     * Checks that there is a value for every required input field.
     */
    private void checkInputsCompleted() {
        submitButton.setDisable(false);
        for (String value : inputValues.values()) {
            if (value.isEmpty()) {
                submitButton.setDisable(true);
            }
        }
    }

    @FXML
    private void submitButtonOnClick(Event e) {

        // Gets the Controller for the expenses page (so we can access its
        // variables/functions)
        ExpensesPageController expensePageController = ((ExpensesPageController) FXMLHandler
                .getFxmlController(FXMLFilenames.EXPENSES_PAGE));

        // Updates the Current Amount Spent value for the selected expense
        updatedExpense.setCurrentAmountSpent(updatedExpense.getCurrentAmountSpent() + Double.parseDouble(amountToAddInput.getText()));

        expenseController.updateExpense(updatedExpense);

        resetInputValues();

        expensePageController.updateSelectedExpense(updatedExpense);

        super.closeWindowOnClick(e);
    }

    /**
     * Resets the inputs to default values
     */
    private void resetInputValues() {
        amountToAddInput.clear();
    }

    public void getExpenseToUpdate(Expense expenseToUpdate) {
        updatedExpense = expenseToUpdate;
    }
}
