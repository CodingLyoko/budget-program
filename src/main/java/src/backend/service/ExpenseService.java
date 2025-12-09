package src.backend.service;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import src.backend.controller.PayPeriodController;
import src.backend.model.Expense;
import src.handlers.AppUserHandler;
import src.shared.ExpenseType;

public class ExpenseService extends ServiceTemplate {

    private static final String TABLE_NAME = "expense";

    PayPeriodController payPeriodController = new PayPeriodController();

    public UUID createExpense(Expense expense) throws SQLException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, SecurityException {

        UUID result = saveEntry(expense);

        // Modifies AppUser funding values based on Expense Type
        switch (expense.getExpenseType()) {
            case ExpenseType.EXPENSE:
                AppUserHandler.updateAvailableFunds(expense.getSpendingLimit() * -1.0);
                AppUserHandler.updateTotalFunds(expense.getCurrentAmountSpent() * -1.0, true);
                break;
            case ExpenseType.INCOME:
                AppUserHandler.updateTotalFunds(expense.getSpendingLimit());
                break;
            case ExpenseType.RESERVED:
                AppUserHandler.updateReservedFunds(expense.getSpendingLimit());
                break;
            case ExpenseType.SAVINGS:
                AppUserHandler.updateSavingFunds(expense.getSpendingLimit());
                break;
            default:
                break;
        }

        return result;
    }

    public List<Expense> getExpensesByPayPeriod(UUID payPeriodId)
            throws SQLException, InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException {

        List<Expense> result;

        connectToDatabase();

        ResultSet resultSet = getQueryResults(
                "SELECT * FROM " + TABLE_NAME + " WHERE pay_period = '" + payPeriodId + "';");

        result = getMultipleEntries(Expense.class, resultSet);

        closeDatabaseConnections();

        return result;
    }

    public List<Expense> getExpensesByExpenseType(ExpenseType expenseType)
            throws SQLException, InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException {

        List<Expense> result;

        connectToDatabase();

        ResultSet resultSet = getQueryResults(
                "SELECT * FROM " + TABLE_NAME + " WHERE expense_type = '" + expenseType + "';");

        result = getMultipleEntries(Expense.class, resultSet);

        closeDatabaseConnections();

        return result;
    }

    public List<Expense> getFavoriteExpenses() throws SQLException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

        List<Expense> result;

        connectToDatabase();

        ResultSet resultSet = getQueryResults("SELECT * FROM " + TABLE_NAME + " WHERE is_favorite = TRUE;");

        result = getMultipleEntries(Expense.class, resultSet);

        closeDatabaseConnections();

        return result;
    }

    public UUID updateExpense(Expense expense) throws SQLException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

        Expense oldExpense = (Expense) getEntryById(expense.getId(), Expense.class);

        updateEntry(expense);

        // Modifies AppUser funding values based on Expense Type
        switch (expense.getExpenseType()) {
            case ExpenseType.EXPENSE:
                AppUserHandler.updateTotalFunds(oldExpense.getCurrentAmountSpent() - expense.getCurrentAmountSpent(),
                        true);

                if (expense.getCurrentAmountSpent() > oldExpense.getSpendingLimit()) {

                    if (oldExpense.getSpendingLimit() > oldExpense.getCurrentAmountSpent()) {
                        AppUserHandler
                                .updateAvailableFunds(oldExpense.getSpendingLimit() - expense.getCurrentAmountSpent());
                    } else if (expense.getSpendingLimit() > oldExpense.getCurrentAmountSpent()) {
                        AppUserHandler
                                .updateAvailableFunds(oldExpense.getCurrentAmountSpent() - expense.getSpendingLimit());
                    } else {
                        AppUserHandler.updateAvailableFunds(
                                oldExpense.getCurrentAmountSpent() - expense.getCurrentAmountSpent());
                    }
                } else if (expense.getCurrentAmountSpent() < oldExpense.getCurrentAmountSpent()
                        && oldExpense.getSpendingLimit() < oldExpense.getCurrentAmountSpent()) {
                    AppUserHandler
                            .updateAvailableFunds(oldExpense.getCurrentAmountSpent() - expense.getSpendingLimit());
                } else if (expense.getSpendingLimit() < oldExpense.getSpendingLimit()
                        && expense.getCurrentAmountSpent() > expense.getSpendingLimit()) {
                    AppUserHandler
                            .updateAvailableFunds(oldExpense.getSpendingLimit() - expense.getCurrentAmountSpent());
                } else {
                    AppUserHandler.updateAvailableFunds(oldExpense.getSpendingLimit() - expense.getSpendingLimit());
                }

                break;
            case ExpenseType.INCOME:
                AppUserHandler.updateTotalFunds(expense.getSpendingLimit() - oldExpense.getSpendingLimit());
                break;
            case ExpenseType.RESERVED:
                AppUserHandler.updateReservedFunds(expense.getSpendingLimit() - oldExpense.getSpendingLimit());
                break;
            case ExpenseType.SAVINGS:
                AppUserHandler.updateSavingFunds(expense.getSpendingLimit() - oldExpense.getSpendingLimit());
                break;
            default:
                break;
        }

        // Update funding values based on old ExpenseType value
        if (expense.getExpenseType() != oldExpense.getExpenseType()) {
            oldExpense.updateExpenseType(expense.getExpenseType());
        }

        return expense.getId();
    }

    public Boolean updateSpendingLimitsOnNewPayPeriodCreation()
            throws SQLException, InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException {

        connectToDatabase();

        ResultSet resultSet = getQueryResults("SELECT * FROM " + TABLE_NAME
                + " WHERE pay_period IN (SELECT id FROM pay_period WHERE is_current = true)");

        List<Expense> expensesInOldPayPeriod = getMultipleEntries(Expense.class, resultSet);

        closeDatabaseConnections();

        // Sets the Spending Limit for each Expense in the (soon to be) previous pay
        // period equal to the Current Amount Spent (if the current amount spent is LESS
        // THAN the spending limit). This is because we want to free up any funds
        // currently allocated but not utilized for that pay period.
        for (Expense expense : expensesInOldPayPeriod) {
            if (expense.getExpenseType().equals(ExpenseType.EXPENSE)
                    && expense.getSpendingLimit() > expense.getCurrentAmountSpent()) {

                expense.setSpendingLimit(expense.getCurrentAmountSpent());

                updateExpense(expense);
            }
        }

        return true;
    }

    public UUID deleteExpense(Expense expense) throws SQLException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, SecurityException {

        deleteEntry(expense);

        // Modifies AppUser funding values based on Expense Type
        switch (expense.getExpenseType()) {
            case ExpenseType.EXPENSE:
                AppUserHandler.updateAvailableFunds(expense.getSpendingLimit() - expense.getCurrentAmountSpent());
                AppUserHandler.updateTotalFunds(expense.getCurrentAmountSpent());
                break;
            case ExpenseType.INCOME:
                AppUserHandler.updateTotalFunds(expense.getSpendingLimit() * -1.0);
                break;
            case ExpenseType.RESERVED:
                AppUserHandler.updateReservedFunds(expense.getSpendingLimit() * -1.0);
                break;
            case ExpenseType.SAVINGS:
                AppUserHandler.updateSavingFunds(expense.getSpendingLimit() * -1.0);
                break;
            default:
                break;
        }

        return expense.getId();
    }
}
