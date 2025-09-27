package src.backend.service;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import src.backend.controller.PayPeriodController;
import src.backend.model.Expense;

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
}
