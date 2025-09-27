package src.backend.controller;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.tinylog.Logger;

import src.backend.model.Expense;
import src.backend.service.ExpenseService;

public class ExpenseController extends ControllerTemplate {

    private ExpenseService expenseService = new ExpenseService();

    /**
     * Returns all the Expenses tied to a given Pay Period ID.
     * 
     * @param payPeriodId - the UUID of the Pay Period whose Expenses you with to retrieve
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
}
