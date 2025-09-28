package src.fxml_controller;

import java.util.HashMap;
import java.util.Map;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import src.backend.controller.ExpenseController;
import src.backend.model.Expense;
import src.backend.model.PayPeriod;
import src.handlers.FXMLHandler;
import src.shared.ExpenseType;
import src.shared.FXMLFilenames;

public class CreateExpensePopupController extends FXMLControllerTemplate {

    ExpenseController expenseController = new ExpenseController();

    Map<String, String> inputValues = new HashMap<>();

    @FXML
    private TextField expenseNameInput;

    @FXML
    private TextField spendingLimitInput;

    @FXML
    private ChoiceBox<ExpenseType> expenseTypeInput;

    @FXML
    private Button submitButton;

    @FXML
    public void initialize() {
        allowOnlyDoublesInTextField(spendingLimitInput);
        setInputListeners();
    }

    /**
     * Populates the options for the Expense Type input field.
     */
    public void setExpenseTypes(ExpenseType expenseType) {

        // Clears any previous options
        expenseTypeInput.getItems().clear();

        // Adds the given expense type to the list of options
        expenseTypeInput.getItems().add(expenseType);

        // Some Expense Type tabs are associated with multiple ExpenseTypes
        // This code adds thos additional ExpenseTypes as options
        if (expenseType.equals(ExpenseType.EXPENSE)) {
            expenseTypeInput.getItems().add(ExpenseType.INCOME);
        }

        // Sets the first option as the default value
        expenseTypeInput.setValue(expenseTypeInput.getItems().get(0));
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

        spendingLimitInput.textProperty().addListener((_, _, newValue) -> {
            inputValues.put("spendingLimitInput", newValue);
            checkInputsCompleted();
        });

        expenseTypeInput.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            inputValues.put("expenseTypeInput", newValue != null ? newValue.toString() : "");
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
        inputValues.put("spendingLimitInput", "");
        inputValues.put("expenseTypeInput", "");
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
        Expense newExpense = new Expense();
        newExpense.setExpenseName(expenseNameInput.getText());
        newExpense.setSpendingLimit(Double.parseDouble(spendingLimitInput.getText()));
        newExpense.setExpenseType(expenseTypeInput.getValue());

        // Sets the Pay Period of the Expense to whatever Pay Period the user had
        // selected when creating the Expense
        // If there is no Pay Period associated with the Expense Type, set this value to
        // NULL
        Tab currentSelectedPayPeriodTab = expensePageController.currentSelectedPayPeriodTab;
        newExpense.setPayPeriod(
                currentSelectedPayPeriodTab != null ? ((PayPeriod) currentSelectedPayPeriodTab.getUserData()).getId()
                        : null);

        expenseController.createExpense(newExpense);

        expensePageController.addExpenseToTable(newExpense);

        resetInputValues();

        super.closeWindowOnClick(e);
    }

    /**
     * Resets the inputs to default values
     */
    private void resetInputValues() {
        expenseNameInput.clear();
        spendingLimitInput.clear();
    }
}
