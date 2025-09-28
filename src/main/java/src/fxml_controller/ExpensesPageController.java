package src.fxml_controller;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import src.backend.controller.ExpenseController;
import src.backend.controller.PayPeriodController;
import src.backend.model.Expense;
import src.backend.model.PayPeriod;
import src.handlers.AppUserHandler;
import src.handlers.FXMLHandler;
import src.shared.ExpenseType;
import src.shared.FXMLFilenames;
import src.shared.PayPeriodFrequency;

public class ExpensesPageController extends FXMLControllerTemplate {

    PayPeriodController payPeriodController = new PayPeriodController();
    ExpenseController expenseController = new ExpenseController();

    Tab currentSelectedPayPeriodTab = new Tab();
    Tab currentSelectedExpenseTab = new Tab();
    TableView<Expense> currentTableView = new TableView<>();

    private TabPane payPeriodTabPane = new TabPane();

    /********** TOP PANEL **********/
    // File Menu
    @FXML
    private MenuItem setTotalFunds;
    @FXML
    private MenuItem setPayPeriodFreq;
    @FXML
    private MenuItem createPayPeriod;

    @FXML
    private ChoiceBox<Integer> expenseYearInput;

    /********** CENTER PANEL **********/
    @FXML
    private TabPane expenseTypeTabPane;

    /********** RIGHT PANEL **********/
    @FXML
    private Label availableFundsLabel;
    @FXML
    private Label totalFundsLabel;
    @FXML
    private Label reservedFundsLabel;
    @FXML
    private Label savingsLabel;

    /********** BOTTOM PANEL **********/
    @FXML
    private Button addExpense;
    @FXML
    private Button editExpense;
    @FXML
    private Button deleteExpense;

    @FXML
    public void initialize() {
        setAvailableFundsLabel();
        setTotalFundsLabel();
        setReservedFundsLabel();
        setSavingsLabel();

        setExpenseYears();

        // Allows code to be executed when the expense year value changes
        expenseYearInput.getSelectionModel().selectedIndexProperty().addListener((_, _, newValue) -> {
            onExpenseYearValueChange(newValue);
        });

        createPayPeriodOnAppStart();

        // Configures and creates Tabs for the relevant Expense Types
        configureExpenseTypeTabPane();
        createExpenseTypeTabs();

        currentSelectedExpenseTab = expenseTypeTabPane.getSelectionModel().getSelectedItem();

        // Configures and creates Tabs for the Pay Periods in the given Expense Year
        configurePayPeriodTabPane();
        createPayPeriodTabs(expenseYearInput.getValue());
    }

    private void setAvailableFundsLabel() {
        availableFundsLabel
                .setText("Available Funds: $"
                        + String.format("%.2f", AppUserHandler.getAppUserInstance().getAvailableFunds()));
    }

    private void setTotalFundsLabel() {
        totalFundsLabel
                .setText("Total Funds: $" + String.format("%.2f", AppUserHandler.getAppUserInstance().getTotalFunds()));
    }

    private void setReservedFundsLabel() {
        reservedFundsLabel
                .setText("Reserved Funds: $"
                        + String.format("%.2f", AppUserHandler.getAppUserInstance().getReservedFunds()));
    }

    private void setSavingsLabel() {
        savingsLabel
                .setText("Total Savings: $"
                        + String.format("%.2f", AppUserHandler.getAppUserInstance().getSavingFunds()));
    }

    /**
     * Adds the expense years for each pay period into the dropdown selector. An
     * expense year is the year that a pay period is active (using the start date of
     * the pay period; the end date is not used)
     */
    private void setExpenseYears() {
        for (Integer expenseYear : payPeriodController.getExpenseYears()) {

            // Only add an Expense Year once
            if (!expenseYearInput.getItems().contains(expenseYear)) {
                expenseYearInput.getItems().add(expenseYear);
            }
        }

        FXCollections.sort(expenseYearInput.getItems(), Comparator.reverseOrder());
        expenseYearInput.setValue(expenseYearInput.getItems().get(0));
    }

    private void onExpenseYearValueChange(Number newExpenseYearIndex) {
        payPeriodTabPane.getTabs().clear();
        createPayPeriodTabs(expenseYearInput.getItems().get(newExpenseYearIndex.intValue()));
    }

    private void configurePayPeriodTabPane() {

        // Configure specific properties of the pay period TabPane
        payPeriodTabPane.setSide(Side.LEFT);
        payPeriodTabPane.tabClosingPolicyProperty().set(TabClosingPolicy.UNAVAILABLE);

        // Adds a listener to the TabPane that changes the currently selected Tab and
        // TableView
        payPeriodTabPane.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            currentSelectedPayPeriodTab = payPeriodTabPane.getSelectionModel().getSelectedItem();

            currentTableView.getSelectionModel().clearSelection();

            // Disable the button by default
            addExpense.setDisable(true);

            if (newValue != null
                    && ((HBox) newValue.getContent()).getChildren().get(0).getClass().equals(TableView.class)) {
                currentTableView = ((TableView<Expense>) ((HBox) newValue.getContent()).getChildren().get(0));

                // If a TableView exists, enable the button
                addExpense.setDisable(false);
            }
        });
    }

    /**
     * Creates a new Pay Period if the current date (on app start) is greater than
     * the end date of the last Pay Period.
     */
    private void createPayPeriodOnAppStart() {

        // Check if there are pay period entries in the database
        if (payPeriodController.getNumPayPeriods() != 0) {
            createPayPeriod.setDisable(true);

            PayPeriod currentPayPeriod = payPeriodController.getCurrentPayPeriod();

            // Current date is GREATER THAN current pay period end date
            if (currentPayPeriod != null && currentPayPeriod.getEndDate().toLocalDateTime()
                    .compareTo(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS)) <= 0) {

                // Checks what the new End Date for the next pay period would be
                Timestamp newEndDate = PayPeriodFrequency.getEndDate(currentPayPeriod.getEndDate());
                Timestamp newStartDate = currentPayPeriod.getEndDate();

                // Keeps checking End Dates for new pay periods (based on currently-selected Pay
                // Period Frequency) until the current date is prior to the new End Date
                while (newEndDate.toLocalDateTime()
                        .compareTo(LocalDateTime.now().truncatedTo(ChronoUnit.DAYS)) <= 0) {
                    newStartDate = newEndDate;
                    newEndDate = PayPeriodFrequency.getEndDate(newEndDate);
                }

                // Creates a Pay Period that contains the current date within it
                payPeriodController.createPayPeriod(newStartDate);
            }
        }
    }

    private void configureExpenseTypeTabPane() {

        // Adds a listener to the Expense Type TabPane to change the currently selected
        // Tab and TableView
        expenseTypeTabPane.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            currentSelectedExpenseTab = expenseTypeTabPane.getSelectionModel().getSelectedItem();

            currentTableView.getSelectionModel().clearSelection();

            // Disable the button by default
            addExpense.setDisable(true);

            // Switching to ExpenseType tab that is NOT Expense
            if (newValue != null && newValue.getContent().getClass().equals(HBox.class)) {
                currentTableView = ((TableView<Expense>) ((HBox) newValue.getContent()).getChildren().get(0));

                // If a TableView exists, enable the button
                addExpense.setDisable(false);

                // Switching to the Expense ExpenseType tab
                // Check if there is a TableView in the Tab
            } else if (newValue != null && newValue.getContent().getClass().equals(TabPane.class)
                    && ((TabPane) newValue.getContent()).getSelectionModel().getSelectedItem() != null) {
                currentTableView = ((TableView<Expense>) ((HBox) ((TabPane) newValue.getContent()).getSelectionModel()
                        .getSelectedItem().getContent()).getChildren().get(0));

                // If a TableView exists, enable the button
                addExpense.setDisable(false);
            }
        });
    }

    /**
     * Adds Tabs for each Expense Type.
     */
    private void createExpenseTypeTabs() {
        addExpenseTypeTab(ExpenseType.EXPENSE);
        addExpenseTypeTab(ExpenseType.SAVINGS);
        addExpenseTypeTab(ExpenseType.RESERVED);
    }

    /**
     * Creates a Tab based on a given ExpenseType.
     * 
     * @param expenseType - the ExpenseType to base the Tab off of
     */
    public void addExpenseTypeTab(ExpenseType expenseType) {
        Tab newTab = new Tab(expenseType.toString());
        newTab.setUserData(expenseType);

        HBox tabContent = new HBox();
        tabContent.setSpacing(10);

        // Create a resizable TableView for the given Tab
        TableView<Expense> tableView = createExpenseTableView();
        tabContent.getChildren().add(tableView);
        HBox.setHgrow(tableView, Priority.ALWAYS);

        // Add Expenses to the TableView for the given ExpenseType
        for (Expense expense : expenseController.getExpensesByExpenseType(expenseType)) {
            tableView.getItems().add(expense);
        }

        // Sets the currently-selected TableView to this object
        currentTableView = tableView;

        if (expenseType.equals(ExpenseType.EXPENSE)) {
            newTab.setContent(payPeriodTabPane);
            currentSelectedPayPeriodTab = payPeriodTabPane.getSelectionModel().getSelectedItem();
        } else {
            newTab.setContent(tabContent);
            currentSelectedPayPeriodTab = null;
        }

        expenseTypeTabPane.getTabs().add(newTab);
    }

    /**
     * Adds Tabs for each Pay Period for a given expense year.
     * 
     * @param expenseYear - the year used to specify which Pay Period to create Tabs
     *                    for (i.e., if the Pay Period has a Start Date within the
     *                    given year, it will have a Tab)
     */
    private void createPayPeriodTabs(int expenseYear) {
        for (PayPeriod payPeriod : payPeriodController.getPayPeriodsForExpenseYear(expenseYear)) {
            addPayPeriodTab(payPeriod);
        }

        sortPayPeriodTabs();
    }

    public void addPayPeriodTab(PayPeriod payPeriod) {
        Tab newTab = new Tab("Pay Period " + (payPeriodTabPane.getTabs().size() + 1));
        newTab.setUserData(payPeriod);

        HBox tabContent = new HBox();
        tabContent.setSpacing(10);

        // Create a resizable TableView for the given Tab
        TableView<Expense> tableView = createExpenseTableView();
        tabContent.getChildren().add(tableView);
        HBox.setHgrow(tableView, Priority.ALWAYS);

        // Display info about the given Pay Period
        VBox payPeriodData = new VBox();
        payPeriodData.setSpacing(10);
        payPeriodData.getChildren().add(new Label("Start Date: " + payPeriod.getStartDate().toString().substring(0,
                payPeriod.getStartDate().toString().length() - 11)));
        payPeriodData.getChildren().add(new Label("End Date: " + payPeriod.getEndDate().toString().substring(0,
                payPeriod.getEndDate().toString().length() - 11)));
        payPeriodData.getChildren().add(new Label("Current? " + payPeriod.getIsCurrent()));

        // Add the info to the given Tab
        tabContent.getChildren().add(payPeriodData);

        // Add Expenses to the TableView for the given Pay Period
        for (Expense expense : expenseController.getExpensesByPayPeriod(payPeriod.getId())) {

            // Only show expenses that are of type "Expense" or "Income"
            if (expense.getExpenseType().equals(ExpenseType.EXPENSE)
                    || expense.getExpenseType().equals(ExpenseType.INCOME)) {
                tableView.getItems().add(expense);
            }
        }

        // Sets the currently-selected TableView to this object
        currentTableView = tableView;

        newTab.setContent(tabContent);

        // Sets the currently-selected Pay Period tab to this Tab
        currentSelectedPayPeriodTab = newTab;

        // Adds the Tab to the Pay Period TabPane
        payPeriodTabPane.getTabs().add(newTab);
    }

    /**
     * Creates a TableView object to hold Expense objects.
     * 
     * @return The configured TableView object
     */
    private TableView<Expense> createExpenseTableView() {
        TableView<Expense> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Expense, String> expenseNameColumn = new TableColumn<>("Name");
        expenseNameColumn.setCellValueFactory(new PropertyValueFactory<>("expenseName"));
        tableView.getColumns().add(expenseNameColumn);

        TableColumn<Expense, Double> totalSpentColumn = new TableColumn<>("Total Spent");
        totalSpentColumn.setCellValueFactory(new PropertyValueFactory<>("currentAmountSpent"));
        tableView.getColumns().add(totalSpentColumn);

        TableColumn<Expense, Double> spendingLimitColumn = new TableColumn<>("Spending Limit");
        spendingLimitColumn.setCellValueFactory(new PropertyValueFactory<>("spendingLimit"));
        tableView.getColumns().add(spendingLimitColumn);

        TableColumn<Expense, Double> expenseTypeColumn = new TableColumn<>("Expense Type");
        expenseTypeColumn.setCellValueFactory(new PropertyValueFactory<>("expenseType"));
        tableView.getColumns().add(expenseTypeColumn);

        // Adds a listener to enable/disable the Expense modification buttons based on
        // the selection of an Expense from the TableView
        tableView.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {

            // Disable the buttons by default
            deleteExpense.setDisable(true);
            editExpense.setDisable(true);

            // If an Expense is selected, enable the buttons
            if (newValue != null) {
                deleteExpense.setDisable(false);
                editExpense.setDisable(false);
            }
        });

        return tableView;
    }

    /**
     * Add an Expense object to the currently-selected TableView.
     * 
     * @param expense - the Expense to add
     */
    public void addExpenseToTable(Expense expense) {
        currentTableView.getItems().add(expense);
    }

    /**
     * Sorts tabs so that the most recent Pay Period is at the top
     */
    private void sortPayPeriodTabs() {
        // Need to create a temporary Tab list to work around a bug in TabPane (sorting
        // initially selects multiple tabs)
        List<Tab> tabs = new ArrayList<>(payPeriodTabPane.getTabs());
        tabs.sort((o1, o2) -> ((PayPeriod) o2.getUserData()).getEndDate()
                .compareTo(((PayPeriod) o1.getUserData()).getEndDate()));

        payPeriodTabPane.getTabs().clear();
        payPeriodTabPane.getTabs().setAll(tabs);
    }

    public void setDisableCreatePayPeriodMenuItem(Boolean isDisabled) {
        createPayPeriod.setDisable(isDisabled);
    }

    @FXML
    private void setTotalFundsOnClick() {
        openPopup(FXMLFilenames.SET_TOTAL_FUNDS_POPUP);
        setTotalFundsLabel();
    }

    @FXML
    private void setPayPeriodFreqOnClick() {
        openPopup(FXMLFilenames.PAY_PERIOD_FREQUENCY_SELECTION_POPUP);

        // Updates the Tab for the current Pay Period (whether it was updated or not)
        payPeriodTabPane.getTabs().removeFirst();
        addPayPeriodTab(payPeriodController.getCurrentPayPeriod());
        sortPayPeriodTabs();
    }

    @FXML
    private void createPayPeriodOnClick() {
        openPopup(FXMLFilenames.CREATE_PAY_PERIOD_POPUP);
        createPayPeriodTabs(expenseYearInput.getValue());
    }

    @FXML
    private void addExpenseOnClick() {

        // Sets the options for the ExpenseType input based on the currently-selected
        // Expense Type tab
        ((CreateExpensePopupController) FXMLHandler.getFxmlController(FXMLFilenames.CREATE_EXPENSE_POPUP))
                .setExpenseTypes((ExpenseType) currentSelectedExpenseTab.getUserData());

        openPopup(FXMLFilenames.CREATE_EXPENSE_POPUP);
    }

    @FXML
    private void editExpenseOnClick() {

    }

    @FXML
    private void deleteExpenseOnClick() {

        Expense selectedExpense = currentTableView.getSelectionModel().getSelectedItem();

        // Deletes the Expense from the database
        expenseController.deleteEntry(selectedExpense);

        // Delete the Expense from the TableView
        currentTableView.getItems().remove(selectedExpense);
    }
}
