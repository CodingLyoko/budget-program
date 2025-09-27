package src.fxml_controller;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import src.backend.controller.ExpenseController;
import src.backend.controller.PayPeriodController;
import src.backend.model.Expense;
import src.backend.model.PayPeriod;
import src.handlers.AppUserHandler;
import src.shared.ExpenseType;
import src.shared.FXMLFilenames;
import src.shared.PayPeriodFrequency;

public class ExpensesPageController extends FXMLControllerTemplate {

    PayPeriodController payPeriodController = new PayPeriodController();
    ExpenseController expenseController = new ExpenseController();

    Tab currentSelectedTab = new Tab();
    TableView<Expense> currentPayPeriodTableView = new TableView<>();

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

    @FXML
    private Label totalFundsLabel;

    @FXML
    private Label savingsLabel;

    /********** CENTER PANEL **********/
    @FXML
    private TabPane payPeriodTabPane;

    /********** BOTTOM PANEL **********/
    @FXML
    private Button addExpense;
    @FXML
    private Button editExpense;
    @FXML
    private Button deleteExpense;

    @FXML
    public void initialize() {
        setFundsAvailableLabel();
        setSavingsLabel();

        setExpenseYears();

        // Allows code to be executed when the expense year value changes
        expenseYearInput.getSelectionModel().selectedIndexProperty().addListener((_, _, newValue) -> {
            onExpenseYearValueChange(newValue);
        });

        createPayPeriodOnAppStart();

        createPayPeriodTabs(expenseYearInput.getValue());

        payPeriodTabPane.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            currentSelectedTab = payPeriodTabPane.getSelectionModel().getSelectedItem();

            currentPayPeriodTableView.getSelectionModel().clearSelection();

            if (newValue != null && ((HBox) newValue.getContent()).getChildren().get(0).getClass().equals(TableView.class)) {
                currentPayPeriodTableView = ((TableView<Expense>) ((HBox) newValue.getContent()).getChildren().get(0));
            }
        });
    }

    private void setFundsAvailableLabel() {
        totalFundsLabel
                .setText("Total Funds: $" + String.format("%.2f", AppUserHandler.getAppUserInstance().getTotalFunds()));
    }

    private void setSavingsLabel() {
        savingsLabel
                .setText("Total Savings: $" + String.format("%.2f", AppUserHandler.getAppUserInstance().getSavings()));
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

    public void addPayPeriodTab(PayPeriod newPayPeriod) {
        Tab newTab = new Tab("Pay Period " + (payPeriodTabPane.getTabs().size() + 1));
        newTab.setUserData(newPayPeriod);

        HBox tabContent = new HBox();
        tabContent.setSpacing(10);

        // Fake data until real data is coded
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

        tabContent.getChildren().add(tableView);

        VBox payPeriodData = new VBox();
        payPeriodData.setSpacing(10);
        payPeriodData.getChildren().add(new Label("Start Date: " + newPayPeriod.getStartDate().toString().substring(0,
                newPayPeriod.getStartDate().toString().length() - 11)));
        payPeriodData.getChildren().add(new Label("End Date: " + newPayPeriod.getEndDate().toString().substring(0,
                newPayPeriod.getEndDate().toString().length() - 11)));
        payPeriodData.getChildren().add(new Label("Current? " + newPayPeriod.getIsCurrent()));

        tabContent.getChildren().add(payPeriodData);

        tabContent.setHgrow(tableView, Priority.ALWAYS);
        // tabContent.setHgrow(payPeriodData, Priority.ALWAYS);

        for (Expense expense : expenseController.getExpensesByPayPeriod(newPayPeriod.getId())) {
            // Only show expenses that are of type "Expense" or "Income"
            if (expense.getExpenseType().equals(ExpenseType.EXPENSE)
                    || expense.getExpenseType().equals(ExpenseType.INCOME)) {
                tableView.getItems().add(expense);
            }
        }

        currentPayPeriodTableView = tableView;

        tableView.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            deleteExpense.setDisable(true);
            editExpense.setDisable(true);

            if (newValue != null) {
                deleteExpense.setDisable(false);
                editExpense.setDisable(false);
            }
        });

        newTab.setContent(tabContent);
        
        currentSelectedTab = newTab;

        payPeriodTabPane.getTabs().add(newTab);
    }

    /**
     * Add an Expense object to the TableView for the most recent Pay Period Tab
     * 
     * @param expense
     */
    public void addExpenseToTable(Expense expense) {
        currentPayPeriodTableView.getItems().add(expense);
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
        setFundsAvailableLabel();
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
        openPopup(FXMLFilenames.CREATE_EXPENSE_POPUP);
    }

    @FXML
    private void editExpenseOnClick() {
        
    }

    @FXML
    private void deleteExpenseOnClick() {

        Expense selectedExpense = currentPayPeriodTableView.getSelectionModel().getSelectedItem();

        // Deletes the Expense from the database
        expenseController.deleteEntry(selectedExpense);

        // Delete the Expense from the TableView
        currentPayPeriodTableView.getItems().remove(selectedExpense);
    }
}
