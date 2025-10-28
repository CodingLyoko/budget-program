package src.fxml_controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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

    ObservableList<Expense> unfilteredExpenses;
    FilteredList<Expense> filteredExpenses;
    ObservableList<String> unfilteredExpenseNames = FXCollections.observableArrayList();
    FilteredList<String> filteredExpenseNames;

    @FXML
    private ComboBox<String> expenseNameInput;

    @FXML
    private Label spendingLimitLabel;
    @FXML
    private TextField spendingLimitInput;

    @FXML
    private ChoiceBox<ExpenseType> expenseTypeInput;

    @FXML
    private CheckBox alreadyPaidCheckBox;

    @FXML
    private Button submitButton;

    @FXML
    public void initialize() {
        allowOnlyDoublesInTextField(spendingLimitInput);
        setInputListeners();
        populateExpenseOptions();
    }

    /**
     * Configures the layout/options of the popup based on the ExpenseType of the
     * Expense being created.
     * 
     * @param expenseType - the Expense Type of the expense being created
     */
    public void configurePopup(ExpenseType expenseType) {
        setExpenseTypes(expenseType);
        updateSpendingLimitLabel(expenseType);
    }

    /**
     * Populates the options for the Expense Type input field.
     */
    private void setExpenseTypes(ExpenseType expenseType) {

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
     * Modifies the Spending Limit input label based on Expense Type.
     * 
     * @param expenseType - the Expense Type used to update the label
     */
    private void updateSpendingLimitLabel(ExpenseType expenseType) {
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

    /**
     * Adds listeners to each required input field.
     */
    private void setInputListeners() {
        instantiateInputValueMap();

        // Filter selectable options based on current input text
        expenseNameInput.getEditor().textProperty().addListener((_, _, newValue) -> {

            // Maps the new value in the input field
            inputValues.put("expenseNameInput", newValue);

            final TextField editor = expenseNameInput.getEditor();
            final String selected = expenseNameInput.getSelectionModel().getSelectedItem();

            // If no item in the list is selected or the selected item
            // isn't equal to the current input, we refilter the list.
            if (selected == null || !selected.equals(editor.getText())) {

                // Filters both the selectable Expense name options AS WELL AS the related
                // Expense data
                filteredExpenses
                        .setPredicate(item -> item.getExpenseName().toUpperCase().startsWith(newValue.toUpperCase()));
                filteredExpenseNames.setPredicate(item -> item.toUpperCase().startsWith(newValue.toUpperCase()));
            }

            expenseNameInput.setItems(filteredExpenseNames);
            expenseNameInput.setUserData(filteredExpenses);

            checkInputsCompleted();
        });

        // If the user selected an existing Expense, populate the other input feilds
        // with that Expense's data
        expenseNameInput.getSelectionModel().selectedItemProperty().addListener((_, _, _) -> {
            if (expenseNameInput.getSelectionModel().getSelectedIndex() >= 0) {
                Expense selectedExpense = ((List<Expense>) expenseNameInput.getUserData())
                        .get(expenseNameInput.getSelectionModel().getSelectedIndex());

                spendingLimitInput.setText(selectedExpense.getSpendingLimit().toString());

                // Only update ExpenseType if the selected value is a selectable option
                if (expenseTypeInput.getItems().contains(selectedExpense.getExpenseType())) {
                    expenseTypeInput.getSelectionModel().select(selectedExpense.getExpenseType());
                }
            }

            checkInputsCompleted();
        });

        spendingLimitInput.textProperty().addListener((_, _, newValue) -> {
            inputValues.put("spendingLimitInput", newValue);
            checkInputsCompleted();
        });

        expenseTypeInput.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            inputValues.put("expenseTypeInput", newValue != null ? newValue.toString() : "");
            checkInputsCompleted();

            if (newValue != null) {
                updateSpendingLimitLabel(newValue);
                setAlreadyPaidCheckboxVisible(newValue);
            }
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
            if (value == null || value.isEmpty()) {
                submitButton.setDisable(true);
            }
        }
    }

    /**
     * Sets the visibility of the "Already Paid" checkbox based on the ExpenseType
     * of the expense being created
     * 
     * @param expenseType - the Expense Type of the expense being created
     */
    private void setAlreadyPaidCheckboxVisible(ExpenseType expenseType) {

        // Unselects and hides the checkbox
        alreadyPaidCheckBox.setSelected(false);
        alreadyPaidCheckBox.setVisible(false);

        // Only show the checkbox if creating an Expense of type "EXPENSE"
        if (expenseType.equals(ExpenseType.EXPENSE)) {
            alreadyPaidCheckBox.setVisible(true);
        }
    }

    private void populateExpenseOptions() {

        // Get Expenses/Expense names and store them in lists
        // FilteredLists will be used later (when the user is typing in an Expense name)
        unfilteredExpenses = FXCollections.observableArrayList(expenseController.getAllEntries(Expense.class));
        filteredExpenses = new FilteredList<>(unfilteredExpenses);

        for (Expense expense : unfilteredExpenses) {
            unfilteredExpenseNames.add(expense.getExpenseName());
        }
        filteredExpenseNames = new FilteredList<>(unfilteredExpenseNames);

        // Sets the selectable options for the Expense names and stores the related
        // Expense data
        expenseNameInput.setItems(unfilteredExpenseNames);
        expenseNameInput.setUserData(unfilteredExpenses);
    }

    @FXML
    private void submitButtonOnClick(Event e) {

        // Gets the Controller for the expenses page (so we can access its
        // variables/functions)
        ExpensesPageController expensePageController = ((ExpensesPageController) FXMLHandler
                .getFxmlController(FXMLFilenames.EXPENSES_PAGE));

        Double spendingLimitValue = Double.parseDouble(spendingLimitInput.getText());

        // Creates a new Expense object based on user input
        Expense newExpense = new Expense();
        newExpense.setExpenseName(expenseNameInput.getValue());
        newExpense.setSpendingLimit(spendingLimitValue);
        newExpense.setExpenseType(expenseTypeInput.getValue());

        if (alreadyPaidCheckBox.isSelected()) {
            newExpense.setCurrentAmountSpent(spendingLimitValue);
        }

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
        expenseNameInput.setValue("");
        spendingLimitInput.clear();
        alreadyPaidCheckBox.setSelected(false);
    }
}
