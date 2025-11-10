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
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
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
    private VBox payPeriodInfoVBox = new VBox();

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
    private VBox infoVBox;
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
    private Button addExpenseButton;

    @FXML
    private MenuButton editExpenseMenuButton;
    @FXML
    private MenuItem addToCurrentSpentAmountMenuItem;
    @FXML
    private MenuItem toggleFavoriteExpenseMenuItem;

    @FXML
    private Button deleteExpenseButton;

    @FXML
    public void initialize() {
        setAvailableFundsLabel();
        setTotalFundsLabel();
        setReservedFundsLabel();
        setSavingsLabel();

        setExpenseYears();

        // Allows code to be executed when the expense year value changes
        expenseYearInput.getSelectionModel().selectedIndexProperty()
                .addListener((_, _, newValue) -> onExpenseYearValueChange(newValue));

        createPayPeriodOnAppStart();

        // Configures and creates Tabs for the relevant Expense Types
        configureExpenseTypeTabPane();
        createExpenseTypeTabs();

        currentSelectedExpenseTab = expenseTypeTabPane.getSelectionModel().getSelectedItem();

        // Configures and creates Tabs for the Pay Periods in the given Expense Year
        configurePayPeriodTabPane();
        createPayPeriodTabs(expenseYearInput.getValue());

        payPeriodInfoVBox.setSpacing(10);
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

        // Adds a listener to the TabPane that does the following:
        // - Changes the currently selected Tab
        // - Changes the currently selected TableView
        // - Updates info about the Pay Period
        payPeriodTabPane.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {
            currentSelectedPayPeriodTab = payPeriodTabPane.getSelectionModel().getSelectedItem();

            currentTableView.getSelectionModel().clearSelection();

            // Disable the button by default
            addExpenseButton.setDisable(true);

            if (newValue != null
                    && ((HBox) newValue.getContent()).getChildren().get(0).getClass().equals(TableView.class)) {
                currentTableView = ((TableView<Expense>) ((HBox) newValue.getContent()).getChildren().get(0));

                // If a TableView exists, enable the button
                addExpenseButton.setDisable(false);

                // Display info about the given Pay Period
                ((Label) payPeriodInfoVBox.getChildren().get(0)).setText(
                        "Start Date: " + ((PayPeriod) newValue.getUserData()).getStartDate().toString().substring(0,
                                ((PayPeriod) newValue.getUserData()).getStartDate().toString().length() - 11));
                ((Label) payPeriodInfoVBox.getChildren().get(1)).setText(
                        "End Date: " + ((PayPeriod) newValue.getUserData()).getEndDate().toString().substring(0,
                                ((PayPeriod) newValue.getUserData()).getEndDate().toString().length() - 11));
                ((Label) payPeriodInfoVBox.getChildren().get(2)).setText(
                        "Is Current: " + ((PayPeriod) newValue.getUserData()).getIsCurrent());
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

                // Updates the spending limits of every Expense in the previous pay period to
                // reflect the current amount spent for that Expense
                expenseController.updateSpendingLimitsOnNewPayPeriodCreation();

                updateFundingLabels();

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
            addExpenseButton.setDisable(true);

            // Switching to ExpenseType tab that is NOT Expense
            if (newValue != null && newValue.getContent().getClass().equals(HBox.class)) {
                currentTableView = ((TableView<Expense>) ((HBox) newValue.getContent()).getChildren().get(0));

                // If a TableView exists, enable the button
                addExpenseButton.setDisable(false);

                // Hides Pay Period specific information
                infoVBox.getChildren().getLast().setVisible(false);

                // Switching to the Expense ExpenseType tab
                // Check if there is a TableView in the Tab
            } else if (newValue != null && newValue.getContent().getClass().equals(TabPane.class)
                    && ((TabPane) newValue.getContent()).getSelectionModel().getSelectedItem() != null) {
                currentTableView = ((TableView<Expense>) ((HBox) ((TabPane) newValue.getContent()).getSelectionModel()
                        .getSelectedItem().getContent()).getChildren().get(0));

                // If a TableView exists, enable the button
                addExpenseButton.setDisable(false);

                // Shows Pay Period specific information for this ExpenseType tab
                infoVBox.getChildren().getLast().setVisible(true);
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
        TableView<Expense> tableView = createExpenseTableView(false);
        tabContent.getChildren().add(tableView);
        HBox.setHgrow(tableView, Priority.ALWAYS);

        // Sets the currently-selected TableView to this object
        currentTableView = tableView;

        // Add Expenses to the TableView for the given ExpenseType
        for (Expense expense : expenseController.getExpensesByExpenseType(expenseType)) {
            addExpenseToTable(expense);
        }

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
        TableView<Expense> tableView = createExpenseTableView(true);
        tabContent.getChildren().add(tableView);
        HBox.setHgrow(tableView, Priority.ALWAYS);

        // Sets the currently-selected TableView to this object
        currentTableView = tableView;

        // Add the info for this Pay Period to the appropriate area
        // Only need to add these labels once; the value of the labels will change on
        // Tab change
        if (payPeriodTabPane.getTabs().isEmpty()) {

            // Display info about the given Pay Period
            payPeriodInfoVBox.getChildren()
                    .add(new Label("Start Date: " + payPeriod.getStartDate().toString().substring(0,
                            payPeriod.getStartDate().toString().length() - 11)));
            payPeriodInfoVBox.getChildren().add(new Label("End Date: " + payPeriod.getEndDate().toString().substring(0,
                    payPeriod.getEndDate().toString().length() - 11)));
            payPeriodInfoVBox.getChildren().add(new Label("Is Current: " + payPeriod.getIsCurrent()));

            infoVBox.getChildren().add(payPeriodInfoVBox);
        }

        // Add Expenses to the TableView for the given Pay Period
        for (Expense expense : expenseController.getExpensesByPayPeriod(payPeriod.getId())) {

            // Only show expenses that are of type "Expense" or "Income"
            if (expense.getExpenseType().equals(ExpenseType.EXPENSE)
                    || expense.getExpenseType().equals(ExpenseType.INCOME)) {
                addExpenseToTable(expense);
            }
        }

        newTab.setContent(tabContent);

        // Sets the currently-selected Pay Period tab to this Tab
        currentSelectedPayPeriodTab = newTab;

        // Adds the Tab to the Pay Period TabPane
        payPeriodTabPane.getTabs().add(newTab);
    }

    /**
     * Creates a TableView object to hold Expense objects.
     *
     * @param showCurrentAmountSpent - toggle to display the "Total Spent" column
     * @return The configured TableView object
     */
    private TableView<Expense> createExpenseTableView(Boolean showCurrentAmountSpent) {
        TableView<Expense> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Expense, String> expenseNameColumn = new TableColumn<>("Name");
        expenseNameColumn.setCellValueFactory(new PropertyValueFactory<>("expenseName"));
        tableView.getColumns().add(expenseNameColumn);

        if (showCurrentAmountSpent.equals(Boolean.TRUE)) {
            TableColumn<Expense, Double> totalSpentColumn = new TableColumn<>("Total Spent");
            configureTotalSpentColumn(totalSpentColumn);
            tableView.getColumns().add(totalSpentColumn);
        }

        TableColumn<Expense, Double> spendingLimitColumn = new TableColumn<>("Spending Limit");
        spendingLimitColumn.setCellValueFactory(new PropertyValueFactory<>("spendingLimit"));
        tableView.getColumns().add(spendingLimitColumn);

        // Adds a listener to enable/disable the Expense modification buttons based on
        // the selection of an Expense from the TableView
        tableView.getSelectionModel().selectedItemProperty().addListener((_, _, newValue) -> {

            // Disable the buttons by default
            deleteExpenseButton.setDisable(true);
            editExpenseMenuButton.setDisable(true);

            // If an Expense is selected, enable the buttons
            if (newValue != null) {
                deleteExpenseButton.setDisable(false);
                editExpenseMenuButton.setDisable(false);

                // Updates the Favorites MenuItem text baed on the Favorite status of the
                // selected Expense
                if (newValue.getIsFavorite().equals(Boolean.TRUE)) {
                    toggleFavoriteExpenseMenuItem.setText("Remove from Favorites");
                } else {
                    toggleFavoriteExpenseMenuItem.setText("Add to Favorites");
                }

                // Enables/Disables the given MenuItem based on the ExpenseType of the selected
                // Expense
                addToCurrentSpentAmountMenuItem.setDisable(!newValue.getExpenseType().equals(ExpenseType.EXPENSE));
            }
        });

        // Opens the edit popup when double-clicking an expense
        tableView.setRowFactory(_ -> {
            TableRow<Expense> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    editExpenseOnClick();
                }
            });
            return row;
        });

        return tableView;
    }

    /**
     * Changes the background color of the cells in the column based on the cell's
     * value.
     * 
     * @param totalSpentColumn - the column to configure
     */
    private void configureTotalSpentColumn(TableColumn<Expense, Double> totalSpentColumn) {

        totalSpentColumn.setCellValueFactory(new PropertyValueFactory<>("currentAmountSpent"));

        // Make changes based on cell value
        totalSpentColumn.setCellFactory(_ -> new TableCell<Expense, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {

                // Calls the super of this method so the item is modified normally
                super.updateItem(item, empty);

                // Set the text of the cell to the value of the cell
                setText(empty ? "" : getItem().toString());

                TableRow<Expense> currentRow = getTableRow();

                // Modify row color based on certain criteria
                if (!isEmpty()) {
                    if (currentRow.getItem().getExpenseType() == ExpenseType.INCOME) {
                        currentRow.setStyle("-fx-background-color:lightgreen");

                        // Amount spent is GREATER THAN the given limit
                    } else if (currentRow.getItem().getCurrentAmountSpent() > currentRow.getItem().getSpendingLimit()) {
                        currentRow.setStyle("-fx-background-color:lightcoral");
                    } else {
                        currentRow.setStyle(null);
                    }
                }
            }
        });
    }

    /**
     * Add an Expense object to the currently-selected TableView.
     * 
     * @param expense - the Expense to add
     */
    public void addExpenseToTable(Expense expense) {
        currentTableView.getItems().add(expense);
        autoResizeColumns(currentTableView);
    }

    /**
     * Resizes the columns in a given TableView based on cell text size
     * 
     * @param table - the TableView whose columns will be resized
     */
    public static void autoResizeColumns(TableView<?> table) {
        // Set the right policy
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        table.getColumns().stream().forEach(column -> {
            // Minimal width = columnheader
            Text t = new Text(column.getText());
            double max = t.getLayoutBounds().getWidth();
            for (int i = 0; i < table.getItems().size(); i++) {
                // cell must not be empty
                if (column.getCellData(i) != null) {
                    t = new Text(column.getCellData(i).toString());
                    double calcwidth = t.getLayoutBounds().getWidth();
                    // remember new max-width
                    if (calcwidth > max) {
                        max = calcwidth;
                    }
                }
            }
            // set the new max-widht with some extra space
            column.setPrefWidth(max + 10.0d);
        });
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

    /**
     * Display changes made to any funding values.
     */
    private void updateFundingLabels() {
        setTotalFundsLabel();
        setReservedFundsLabel();
        setSavingsLabel();
        setAvailableFundsLabel();
    }

    /**
     * Remove the given Expense from the current tab and move it to the correct
     * ExpenseType tab
     * 
     * @param expenseToMove - the Expense to be moved
     */
    @SuppressWarnings("unchecked")
    public void moveExpenseToNewTab(Expense expenseToMove) {

        // Adds the Expense to the appropriate TableView
        // EXPENSE & INCOME: ExpenseType Tabs --> Pay Period Tabs --> HBox --> TableView
        // SAVINGS & RESERVED: ExpenseType Tabs --> HBox --> TableView
        switch (expenseToMove.getExpenseType()) {
            case ExpenseType.EXPENSE:
                ((TableView<Expense>) ((HBox) (((TabPane) (expenseTypeTabPane.getTabs().get(0).getContent()))
                        .getTabs().getFirst()).getContent()).getChildren().get(0)).getItems().add(expenseToMove);
                break;
            case ExpenseType.INCOME:
                ((TableView<Expense>) ((HBox) (((TabPane) (expenseTypeTabPane.getTabs().get(0).getContent()))
                        .getTabs().getFirst()).getContent()).getChildren().get(0)).getItems().add(expenseToMove);
                break;
            case ExpenseType.SAVINGS:
                ((TableView<Expense>) ((HBox) (expenseTypeTabPane.getTabs().get(1).getContent())).getChildren().get(0))
                        .getItems().add(expenseToMove);
                break;
            case ExpenseType.RESERVED:
                ((TableView<Expense>) ((HBox) (expenseTypeTabPane.getTabs().get(2).getContent())).getChildren().get(0))
                        .getItems().add(expenseToMove);
                break;
            default:
                break;
        }

        // Removes the Expense from the current TableView
        currentTableView.getItems().remove(currentTableView.getSelectionModel().getSelectedIndex());
    }

    /****************************** FXML FUNCTIONS ******************************/
    @FXML
    /**
     * Opens a popup to update the total funds value. Once the popup closes, the
     * relevant funding labels are updated.
     */
    private void setTotalFundsOnClick() {
        openPopup(FXMLFilenames.SET_TOTAL_FUNDS_POPUP);
        setTotalFundsLabel();
        setAvailableFundsLabel();
    }

    @FXML
    /**
     * Opens a popup to add to the total funds amount. Once the popup closes, the
     * relevant funding labels are updated.
     */
    private void addToTotalFundsOnClick() {
        openPopup(FXMLFilenames.ADD_TO_TOTAL_FUNDS_POPUP);
        setTotalFundsLabel();
        setAvailableFundsLabel();
    }

    @FXML
    /**
     * Opens a popup to set the frequency that Pay Periods occur. Once the popup
     * closes, the end date of the current Pay Period is recalculated using the new
     * value selected in the popup.
     */
    private void setPayPeriodFreqOnClick() {
        openPopup(FXMLFilenames.PAY_PERIOD_FREQUENCY_SELECTION_POPUP);

        PayPeriodFrequencySelectionPopupController payPeriodPopupController = (PayPeriodFrequencySelectionPopupController) FXMLHandler
                .getFxmlController(FXMLFilenames.PAY_PERIOD_FREQUENCY_SELECTION_POPUP);

        if (payPeriodPopupController.getSubmitSuccess().equals(Boolean.TRUE)) {

            // Checks if there are Pay Periods to update
            if (payPeriodController.getNumPayPeriods() != 0) {

                // Updates the Tab for the current Pay Period (whether it was updated or not)
                payPeriodTabPane.getTabs().removeFirst();
                addPayPeriodTab(payPeriodController.getCurrentPayPeriod());
                sortPayPeriodTabs();
            }

            // Reset the value for the next time the popup is opened
            payPeriodPopupController.setSubmitSuccess(false);
        }
    }

    @FXML
    /**
     * Opens a popup to create a new Pay Period.
     * 
     * NOTE: This is only done ONCE (when there are no Pay Periods); each subsequent
     * Pay Period is created automatically.
     */
    private void createPayPeriodOnClick() {
        openPopup(FXMLFilenames.CREATE_PAY_PERIOD_POPUP);
        createPayPeriodTabs(expenseYearInput.getValue());
    }

    @FXML
    /**
     * Opens a popup to create a new Expense. Once the popup is closed, the funding
     * labels are updated.
     */
    private void addExpenseOnClick() {

        // Sets the options for the ExpenseType input based on the currently-selected
        // Expense Type tab
        ((CreateExpensePopupController) FXMLHandler.getFxmlController(FXMLFilenames.CREATE_EXPENSE_POPUP))
                .configurePopup((ExpenseType) currentSelectedExpenseTab.getUserData());

        openPopup(FXMLFilenames.CREATE_EXPENSE_POPUP);

        updateFundingLabels();
    }

    @FXML
    /**
     * Opens a popup to allow editing of the currently-selected Expense. Once the
     * popup is closed, the funding labels are updated.
     */
    private void editExpenseOnClick() {

        UpdateExpensePopupController expensePopupController = ((UpdateExpensePopupController) FXMLHandler
                .getFxmlController(FXMLFilenames.UPDATE_EXPENSE_POPUP));

        // Sets the options for the ExpenseType input based on the currently-selected
        // Expense Type tab
        expensePopupController.updateSpendingLimitLabel((ExpenseType) currentSelectedExpenseTab.getUserData());

        // Send over the current expense from the current TableView
        expensePopupController.updateDefaultValues(currentTableView.getSelectionModel().getSelectedItem());

        // Hides certain inputs based on Expense Type
        expensePopupController.hideInputFields(currentTableView.getSelectionModel().getSelectedItem().getExpenseType());

        openPopup(FXMLFilenames.UPDATE_EXPENSE_POPUP);

        updateFundingLabels();
    }

    @FXML
    /**
     * Opens a popup to modify the Current Amount Spent value of the
     * currently-selected Expense.
     */
    private void addSpentAmountOnClick() {

        AddAmountSpentPopupController addAmountSpentPopupController = ((AddAmountSpentPopupController) FXMLHandler
                .getFxmlController(FXMLFilenames.ADD_AMOUNT_SPENT_POPUP));

        // Send over the current expense from the current TableView
        addAmountSpentPopupController.getExpenseToUpdate(currentTableView.getSelectionModel().getSelectedItem());

        openPopup(FXMLFilenames.ADD_AMOUNT_SPENT_POPUP);

        updateFundingLabels();
    }

    @FXML
    /**
     * Toggles the isFavorite value for the selected Expense
     */
    private void toggleIsFavorite() {

        Expense selectedExpense = currentTableView.getSelectionModel().getSelectedItem();

        // If the value was not set, default to change the value to TRUE
        if (selectedExpense.getIsFavorite().equals(Boolean.TRUE)) {
            selectedExpense.setIsFavorite(false);
            expenseController.updateEntry(selectedExpense);

            toggleFavoriteExpenseMenuItem.setText("Add from Favorites");
        } else {
            selectedExpense.setIsFavorite(true);
            expenseController.updateEntry(selectedExpense);

            toggleFavoriteExpenseMenuItem.setText("Remove from Favorites");
        }
    }

    @FXML
    /**
     * Deletes the currently-selected Expense. Once the popu is clsoed, the Expense
     * is removed from the TableView, and the funding labels are updated.
     */
    private void deleteExpenseOnClick() {

        Expense selectedExpense = currentTableView.getSelectionModel().getSelectedItem();

        // Deletes the Expense from the database
        expenseController.deleteExpense(selectedExpense);

        // Delete the Expense from the TableView
        currentTableView.getItems().remove(selectedExpense);

        updateFundingLabels();
    }
}
