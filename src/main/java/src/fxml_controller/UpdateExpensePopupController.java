package src.fxml_controller;

import java.util.HashMap;
import java.util.Map;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import src.backend.controller.ExpenseController;
import src.backend.model.Expense;
import src.handlers.FXMLHandler;
import src.shared.ExpenseType;
import src.shared.FXMLFilenames;

public class UpdateExpensePopupController extends FXMLControllerTemplate {

    ExpenseController expenseController = new ExpenseController();

    Map<String, String> inputValues = new HashMap<>();

    Expense updatedExpense = new Expense();

    @FXML
    private TextField expenseNameInput;

    @FXML
    private Label currentAmountSpentLabel;
    @FXML
    private TextField currentAmountSpentInput;

    @FXML
    private Label spendingLimitLabel;
    @FXML
    private TextField spendingLimitInput;

    @FXML
    private Button submitButton;

    @FXML
    public void initialize() {
        allowOnlyDoublesInTextField(spendingLimitInput);
        setInputListeners();
    }

    /**
     * Modifies the Spending Limit input label based on Expense Type.
     * 
     * @param expenseType - the Expense Type used to update the label
     */
    public void updateSpendingLimitLabel(ExpenseType expenseType) {
        switch (expenseType) {
            case ExpenseType.EXPENSE:
                spendingLimitLabel.setText("Spending Limit:");
                break;
            case ExpenseType.INCOME:
                spendingLimitLabel.setText("Amount to Add:");
                break;
            case ExpenseType.RESERVED:
                spendingLimitLabel.setText("Amount to Reserve:");
                break;
            case ExpenseType.SAVINGS:
                spendingLimitLabel.setText("Amount to Save:");
                break;
            default:
                break;
        }
    }

    public void hideInputFields(ExpenseType expenseType) {
        switch (expenseType) {
            case ExpenseType.EXPENSE:
                currentAmountSpentLabel.setVisible(true);
                currentAmountSpentLabel.setManaged(true);
                currentAmountSpentInput.setVisible(true);
                currentAmountSpentInput.setManaged(true);
                break;
            default:
                currentAmountSpentLabel.setVisible(false);
                currentAmountSpentLabel.setManaged(false);
                currentAmountSpentInput.setVisible(false);
                currentAmountSpentInput.setManaged(false);
                break;
        }
    }

    /**
     * Adds listeners to each required input field to check if they are are
     * complete.
     */
    private void setInputListeners() {
        instantiateInputValueMap();

        expenseNameInput.textProperty().addListener((_, _, newValue) -> {
            inputValues.put("expenseNameInput", newValue);
            checkInputsCompleted();
        });

        currentAmountSpentInput.textProperty().addListener((_, _, newValue) -> {
            inputValues.put("currentAmountSpent", newValue);
            checkInputsCompleted();
        });

        spendingLimitInput.textProperty().addListener((_, _, newValue) -> {
            inputValues.put("spendingLimitInput", newValue);
            checkInputsCompleted();
        });
    }

    /**
     * Populates a Map to contain the following:
     * - A Key representing an input field
     * - A value corresponding to the value in the input field
     */
    private void instantiateInputValueMap() {
        inputValues.put("expenseNameInput", "");
        inputValues.put("currentAmountSpent", "");
        inputValues.put("spendingLimitInput", "");
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

        // Creates a new Expense object based on user input
        updatedExpense.setExpenseName(expenseNameInput.getText());
        updatedExpense.setCurrentAmountSpent(Double.parseDouble(currentAmountSpentInput.getText()));
        updatedExpense.setSpendingLimit(Double.parseDouble(spendingLimitInput.getText()));

        expenseController.updateExpense(updatedExpense);

        resetInputValues();

        expensePageController.updateSelectedExpense(updatedExpense);

        super.closeWindowOnClick(e);
    }

    /**
     * Resets the inputs to default values
     */
    private void resetInputValues() {
        expenseNameInput.clear();
        spendingLimitInput.clear();
    }

    public void updateDefaultValues(Expense expenseToUpdate) {
        updatedExpense = expenseToUpdate;
        expenseNameInput.setText(expenseToUpdate.getExpenseName());
        currentAmountSpentInput.setText(expenseToUpdate.getCurrentAmountSpent().toString());
        spendingLimitInput.setText(expenseToUpdate.getSpendingLimit().toString());
    }
}
