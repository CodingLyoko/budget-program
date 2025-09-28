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

    public UUID createExpense(Expense expense) throws SQLException {
        
        UUID result = UUID.randomUUID();

        connectToDatabase();

        // Sets ID and saves Expense to the database
        expense.setId(result);
        insertDatabaseEntry(TABLE_NAME, expense);

        // Modifies AppUser funding values based on Expense Type
        switch (expense.getExpenseType()) {
            case ExpenseType.EXPENSE:
                AppUserHandler.updateAvailableFunds(expense.getSpendingLimit() * -1.0);
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

        closeDatabaseConnections();

        return result;
    }
}
