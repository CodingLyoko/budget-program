package src.backend.controller;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.tinylog.Logger;

import src.backend.model.PayPeriod;
import src.backend.service.PayPeriodService;

public class PayPeriodController extends ControllerTemplate {

    private PayPeriodService payPeriodService = new PayPeriodService();

    public List<Integer> getExpenseYears() {

        List<Integer> result = new ArrayList<>();

        try {
            result = payPeriodService.getExpenseYears();

            Logger.info("Successfully retrieved expense years.");
        } catch (SQLException sqle) {
            Logger.error(SQL_ERROR_TEXT, sqle.getMessage());
        } catch (Exception e) {
            Logger.error(GENERAL_ERROR_TEXT, e.getMessage());
        }

        return result;
    }

    public Integer getNumPayPeriods() {

        Integer result = 0;

        try {
            result = payPeriodService.getNumPayPeriods();

            Logger.info("Successfully retrieved number of pay periods.");
        } catch (SQLException sqle) {
            Logger.error(SQL_ERROR_TEXT, sqle.getMessage());
        } catch (Exception e) {
            Logger.error(GENERAL_ERROR_TEXT, e.getMessage());
        }

        return result;
    }

    public PayPeriod getCurrentPayPeriod() {

        PayPeriod result = new PayPeriod();

        try {
            result = payPeriodService.getCurrentPayPeriod();

            Logger.info("Successfully retrieved current pay period.");
        } catch (SQLException sqle) {
            Logger.error(SQL_ERROR_TEXT, sqle.getMessage());
        } catch (Exception e) {
            Logger.error(GENERAL_ERROR_TEXT, e.getMessage());
        }

        return result;
    }

    public List<PayPeriod> getPayPeriodsForExpenseYear(Integer expenseYear) {
        List<PayPeriod> result = new ArrayList<>();

        try {
            result = payPeriodService.getPayPeriodsForExpenseYear(expenseYear);

            Logger.info("Successfully retrieved pay periods for Expense Year: {}.", expenseYear);
        } catch (SQLException sqle) {
            Logger.error(SQL_ERROR_TEXT, sqle.getMessage());
        } catch (Exception e) {
            Logger.error(GENERAL_ERROR_TEXT, e.getMessage());
        }

        return result;
    }

    /**
     * Creates a new Pay Period entry in the database. Returns the newly-created Pay
     * Period entry as an object on successful creation, otherwise returns NULL.
     * 
     * @param startDate - the start date of the new Pay Period
     * @return An object representing the newly-created Pay Period entry; on
     *         error, returns NULL
     */
    public PayPeriod createPayPeriod(Timestamp startDate) {

        PayPeriod result = null;

        try {
            result = payPeriodService.createPayPeriod(startDate);

            Logger.info("Successfully created new pay period.");
        } catch (SQLException sqle) {
            Logger.error(SQL_ERROR_TEXT, sqle.getMessage());
        } catch (Exception e) {
            Logger.error(GENERAL_ERROR_TEXT, e.getMessage());
        }

        return result;
    }

    public void udpateCurrentPayPeriod(PayPeriod updatedPayPeriod) {
        try {
            payPeriodService.updateCurrentPayPeriod(updatedPayPeriod);

            Logger.info("Successfully updated pay period with ID: {}.", updatedPayPeriod.getId());
        } catch (SQLException sqle) {
            Logger.error(SQL_ERROR_TEXT, sqle.getMessage());
        } catch (Exception e) {
            Logger.error(GENERAL_ERROR_TEXT, e.getMessage());
        }
    }
}
