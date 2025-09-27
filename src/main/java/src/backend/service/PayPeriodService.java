package src.backend.service;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Year;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import src.backend.model.PayPeriod;
import src.shared.PayPeriodFrequency;

public class PayPeriodService extends ServiceTemplate {

    private static final String TABLE_NAME = "pay_period";

    public List<Integer> getExpenseYears() throws SQLException, SecurityException {

        List<Integer> result = new ArrayList<>();
        result.add(Year.now().getValue()); // Default value is the current year

        connectToDatabase();

        ResultSet resultSet = getQueryResults("SELECT start_date FROM " + TABLE_NAME);
        System.out.println("Retrieved start dates");

        Calendar calendar = Calendar.getInstance();

        // If there are any Pay Period, get the years they were/are active
        // Using a DO/WHILE because "getQueryResults" automatically calls
        // resultSet.next(); if we only used the WHILE part of the loop (and there was
        // only one pay period entry), then resultSet.next() would return false (and no
        // value would be added to the result)
        if (resultSet.isBeforeFirst()) {
            do {
                calendar.setTime(resultSet.getDate("start_date"));
                result.add(calendar.get(Calendar.YEAR));
            } while (resultSet.next());
        }

        closeDatabaseConnections();

        return result;
    }

    public Integer getNumPayPeriods() throws SQLException {

        Integer result = 0;

        connectToDatabase();

        ResultSet resultSet = getQueryResults("SELECT COUNT(*) FROM " + TABLE_NAME);

        result = resultSet.getInt("COUNT(*)");

        closeDatabaseConnections();

        return result;
    }

    public PayPeriod getCurrentPayPeriod() throws SQLException {

        PayPeriod result;

        connectToDatabase();

        ResultSet resultSet = getQueryResults("SELECT * FROM " + TABLE_NAME + " WHERE is_current = true");

        if (resultSet.getRow() != 0) {
            result = new PayPeriod(resultSet);
        } else {
            return null;
        }

        closeDatabaseConnections();

        return result;
    }

    public List<PayPeriod> getPayPeriodsForExpenseYear(Integer expenseYear)
            throws SQLException, InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException {

        List<PayPeriod> result;

        connectToDatabase();

        Timestamp expenseYearTimestamp = Timestamp.valueOf(expenseYear + "-01-01 00:00:01");
        Timestamp nextExpenseYear = Timestamp.valueOf((expenseYear + 1) + "-01-01 00:00:01");

        // Gets all pay periods that started within the given expense year
        ResultSet resultSet = getQueryResults("SELECT * FROM " + TABLE_NAME + " WHERE start_date BETWEEN '"
                + expenseYearTimestamp + "' AND '" + nextExpenseYear + "'");

        result = getMultipleEntries(PayPeriod.class, resultSet);

        closeDatabaseConnections();

        return result;
    }

    public PayPeriod createPayPeriod(Timestamp startDate)
            throws SQLException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, SecurityException {

        // Create new Pay Period and set values
        PayPeriod newPayPeriod = new PayPeriod();
        newPayPeriod.setStartDate(startDate);
        newPayPeriod.setEndDate(PayPeriodFrequency.getEndDate(startDate));

        connectToDatabase();

        // The previous entry will no longer be current
        PayPeriod previousCurrentPayPeriod = getCurrentPayPeriod();

        saveEntry(newPayPeriod);

        if (previousCurrentPayPeriod != null) {
            previousCurrentPayPeriod.setIsCurrent(false);

            // If we successfully create a new pay period, set the old one to not be current
            updateEntry(previousCurrentPayPeriod);
        }

        closeDatabaseConnections();

        return newPayPeriod;
    }

    public void updateCurrentPayPeriod(PayPeriod updatedPayPeriod) throws SQLException {
        connectToDatabase();

        updateDatabaseEntry(TABLE_NAME, updatedPayPeriod);

        closeDatabaseConnections();
    }
}
