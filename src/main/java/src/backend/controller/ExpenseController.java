package src.backend.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.tinylog.Logger;

import src.backend.model.Expense;
import src.backend.service.ExpenseService;
import src.shared.ExpenseType;

public class ExpenseController extends ControllerTemplate {

    private ExpenseService expenseService = new ExpenseService();

    public UUID createExpense(Expense expense) {

        UUID result = null;

        try {
            result = expenseService.createExpense(expense);

            Logger.info("Successfully created Expense with ID: {}.", result);
        } catch (SQLException sqle) {
            Logger.error(SQL_ERROR_TEXT, sqle.getMessage());
        } catch (Exception e) {
            Logger.error(GENERAL_ERROR_TEXT, e.getMessage());
        }

        return result;
    }

    /**
     * Returns all the Expenses tied to a given Pay Period ID.
     * 
     * @param payPeriodId - the UUID of the Pay Period whose Expenses you with to
     *                    retrieve
     * @return A List of Expenses tied to the given Pay Period
     */
    public List<Expense> getExpensesByPayPeriod(UUID payPeriodId) {

        List<Expense> result = new ArrayList<>();

        try {
            result = expenseService.getExpensesByPayPeriod(payPeriodId);

            Logger.info("Successfully retrieved expenses for Pay Period with ID: {}.", payPeriodId);
        } catch (SQLException sqle) {
            Logger.error(SQL_ERROR_TEXT, sqle.getMessage());
        } catch (Exception e) {
            Logger.error(GENERAL_ERROR_TEXT, e.getMessage());
        }

        return result;
    }

    public List<Expense> getExpensesByExpenseType(ExpenseType expenseType) {

        List<Expense> result = new ArrayList<>();

        try {
            result = expenseService.getExpensesByExpenseType(expenseType);

            Logger.info("Successfully retrieved expenses with Expense Type: {}.", expenseType);
        } catch (SQLException sqle) {
            Logger.error(SQL_ERROR_TEXT, sqle.getMessage());
        } catch (Exception e) {
            Logger.error(GENERAL_ERROR_TEXT, e.getMessage());
        }

        return result;
    }

    public UUID updateExpense(Expense expense) {

        UUID result = null;

        try {
            result = expenseService.updateExpense(expense);

            Logger.info("Successfully updated Expense with ID: {}.", result);
        } catch (SQLException sqle) {
            Logger.error(SQL_ERROR_TEXT, sqle.getMessage());
        } catch (Exception e) {
            Logger.error(GENERAL_ERROR_TEXT, e.getMessage());
        }

        return result;
    }

    public Boolean updateSpendingLimitsOnNewPayPeriodCreation() {

        Boolean result = false;

        try {
            result = expenseService.updateSpendingLimitsOnNewPayPeriodCreation();

            Logger.info("Successfully updated Expenses.");
        } catch (SQLException sqle) {
            Logger.error(SQL_ERROR_TEXT, sqle.getMessage());
        } catch (Exception e) {
            Logger.error(GENERAL_ERROR_TEXT, e.getMessage());
        }

        return result;
    }

    public UUID deleteExpense(Expense expense) {

        UUID result = null;

        try {
            result = expenseService.deleteExpense(expense);

            Logger.info("Successfully deleted Expense with ID: {}.", result);
        } catch (SQLException sqle) {
            Logger.error(SQL_ERROR_TEXT, sqle.getMessage());
        } catch (Exception e) {
            Logger.error(GENERAL_ERROR_TEXT, e.getMessage());
        }

        return result;
    }
}
