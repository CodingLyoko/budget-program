package src.backend.service;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.h2.jdbcx.JdbcConnectionPool;

import src.backend.ConnectH2;
import src.backend.model.ModelTemplate;

@SuppressWarnings("java:S3011") // Incurred by setAccessible when saving to DB

public class ServiceTemplate {

    JdbcConnectionPool connectionPool = JdbcConnectionPool.create(ConnectH2.JDBC_URL, "", "");

    protected Connection connection;
    protected Statement statement;

    protected ServiceTemplate() {
    }

    /**
     * Get all Database entries for a given Class. Only works if the name of the
     * Class is the same as the name of the Database table.
     * 
     * @param <T>
     * @param clazz - the Class of the Object whose data is being retrieved from the
     *              Database
     * @return A List of Objects representing the entries retrieved from the
     *         Database
     * @throws SQLException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    protected <T> List<T> getAllEntries(Class<?> clazz)
            throws SQLException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

        connectToDatabase();

        String tableName = convertStringToSQLSyntax(clazz.getSimpleName());

        // The name of the Class should be the same as the name of the Database table
        ResultSet resultSet = getQueryResults("SELECT * FROM " + tableName);

        List<T> result = getMultipleEntries(clazz, resultSet);

        closeDatabaseConnections();

        return result;
    }

    /**
     * Get a single Database entry based on ID value and the Class of the Object
     * being retrieved. Only works if the name of the Class is the same as the name
     * of the Database table.
     * 
     * @param entryId - the ID of the Object in the Database
     * @param clazz   - the Class of the Object whose data is being retrieved from
     *                the Database
     * @return An Object representing the entry retrieved from the Database
     * @throws SQLException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    protected Object getEntryById(UUID entryId, Class<?> clazz)
            throws SQLException, InstantiationException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException {

        connectToDatabase();

        ResultSet resultSet = getQueryResults(
                "SELECT * FROM " + clazz.getSimpleName().toLowerCase() + " WHERE id = '" + entryId + "'");
        Object result = clazz.getConstructor(ResultSet.class).newInstance(resultSet);

        closeDatabaseConnections();

        return result;
    }

    /**
     * Get a single Database entry based on Enum value and the Class of the Object
     * being retrieved. Only works if:
     * - the name of the Class is the same as the name of the Database table
     * - the name of the Enum matches a column in the Database table
     * 
     * @param enumValue - the Enum value, whose Class that matches a table column
     * @param clazz     - the Class of the Object whose data is being retrieved
     *                  from the Database
     * @return
     * @throws SQLException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    protected Object getEntryByEnum(Enum<?> enumValue, Class<?> clazz) throws SQLException, InstantiationException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {

        connectToDatabase();

        // Gets the Class name of the Enum (which should match the Datbase column name)
        String enumClassName = enumValue.getClass().getCanonicalName();

        ResultSet resultSet = getQueryResults("SELECT * FROM " + clazz.getSimpleName().toLowerCase() + " WHERE "
                + convertStringToSQLSyntax(enumClassName) + " = '" + enumValue.toString() + "'");
        Object result = clazz.getConstructor(ResultSet.class).newInstance(resultSet);

        closeDatabaseConnections();

        return result;
    }

    /**
     * Saves an Object representation of a Database entry into a specified table in
     * the database.
     * 
     * @param entryToSave - An Object with fields that match the columns of the
     *                    table being written to
     * @return The UUID of the newly-created Database entry
     * @throws SQLException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    protected UUID saveEntry(Object entryToSave) throws SQLException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, SecurityException {

        connectToDatabase();

        // Get the Class of the Object being saved to the Database
        Class<?> clazz = entryToSave.getClass();

        // Get the name of the Database table for this Object
        String tableName = clazz.getSimpleName().toLowerCase();

        // Sets a UUID for the Object
        UUID newId = UUID.randomUUID();

        // If there are any Superclasses (that aren't a custom template Class), call the
        // setId() method of the Superclass
        if (clazz.getSuperclass() != ModelTemplate.class && clazz.getSuperclass() != null) {
            clazz.getSuperclass().getDeclaredMethod("setId", UUID.class).invoke(entryToSave, newId);
        } else {
            clazz.getDeclaredMethod("setId", UUID.class).invoke(entryToSave, newId);
        }

        insertDatabaseEntry(tableName, entryToSave);

        return newId;
    }

    /**
     * Updates an entry in the Database based on the Object being passed as a
     * parameter. The ID of the Object must match an ID of an entry in the Database
     * 
     * @param entryToUpdate - an Object representation of the Database entry being
     *                      updated.
     * @return The UUID of the Database entry that was updated
     * @throws SQLException
     */
    protected UUID updateEntry(Object entryToUpdate) throws SQLException {

        connectToDatabase();

        // Get the name of the Database table for this Object
        String tableName = entryToUpdate.getClass().getSimpleName().toLowerCase();

        // Get the ID of the updated entry
        UUID result = updateDatabaseEntry(tableName, entryToUpdate);

        closeDatabaseConnections();

        return result;
    }

    /**
     * Delete an entry in the Database based on th Object being passed as a
     * parameter. The ID of the Object must match an ID of an entry in the Database
     * 
     * @param entryToDelete - an Object representation of the Database entry being
     *                      deleted.
     * @return The UUID of the deleted Database entry
     * @throws SQLException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws SecurityException
     */
    protected UUID deleteEntry(Object entryToDelete) throws SQLException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException, SecurityException {

        UUID entryId = null;

        if (entryToDelete != null) {
            connectToDatabase();

            // Get the name of the Database table for this Object
            String tableName = entryToDelete.getClass().getSimpleName().toLowerCase();

            // Gets the ID of the entry to be deleted from the Database
            entryId = (UUID) entryToDelete.getClass().getDeclaredMethod("getId").invoke(entryToDelete);

            deleteDatabaseEntry(tableName, entryId);

            closeDatabaseConnections();
        }

        return entryId;
    }

    /**
     * Executes a SELECT SQL query and returns the results of that query
     * 
     * @param sqlStatement - A String representation of the SQL query
     * @return ResultSet object, obtained from the executed query
     * @throws SQLException
     */
    protected ResultSet getQueryResults(String sqlStatement) throws SQLException {
        statement = connection.createStatement();

        ResultSet resultSet = statement.executeQuery(sqlStatement);
        resultSet.next(); // Sets the ResultSet to the first row retrieved from the SQL Statement

        return resultSet;

    }

    /**
     * Executes any SQL query other than SELECT
     * 
     * @param sqlStatement - A String representation of the SQL query
     * @throws SQLException
     */
    protected void executeQuery(String sqlStatement) throws SQLException {
        statement = connection.createStatement();
        statement.execute(sqlStatement);
    }

    /**
     * Saves an Object representation of a database entry into a specified table in
     * the database
     * 
     * @param tableName   - String representation of the table name where the entry
     *                    is being saved
     * @param entryToSave - An Object with fields that match the columns of the
     *                    table being written to
     * @throws SQLException
     */
    protected void insertDatabaseEntry(String tableName, Object entryToSave) throws SQLException {

        StringBuilder sqlQuery = new StringBuilder("INSERT INTO " + tableName + "(");

        List<String> columnNames = getColumnNames(tableName);

        for (int i = 0; i < columnNames.size(); i++) {
            sqlQuery.append(columnNames.get(i));

            // Don't want a ',' for the last entry
            if (i < columnNames.size() - 1) {
                sqlQuery.append(",");
            }
        }

        sqlQuery.append(") VALUES (");

        // Gets each field for the Object being saved to the Database
        List<Field> attributes = new ArrayList<>();
        getAllFields(entryToSave.getClass(), attributes);
        Object attributeValue;

        for (int i = 0; i < attributes.size(); i++) {

            // Required because an Object's fields are usually private
            attributes.get(i).setAccessible(true);

            try {

                // Gets the value for the specified field of the Object
                attributeValue = attributes.get(i).get(entryToSave);

                if (attributeValue == null) {
                    sqlQuery.append("NULL");
                    // Strings, Enums, UUIDs
                } else if (attributeValue.getClass().equals(String.class) || attributeValue.getClass().isEnum()
                        || attributeValue.getClass().equals(UUID.class)) {
                    sqlQuery.append("'" + sanitizeInputString(attributeValue.toString()) + "'");
                    // Timestamps
                } else if (attributeValue.getClass().equals(Timestamp.class)) {
                    sqlQuery.append(formatTimestamp((Timestamp) attributeValue));
                } else {
                    sqlQuery.append(sanitizeInputString(attributeValue.toString()));
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }

            // Don't want a ',' for the last entry
            if (i < attributes.size() - 1) {
                sqlQuery.append(",");
            }
        }

        sqlQuery.append(")");

        statement = connection.createStatement();
        statement.executeUpdate(sqlQuery.toString());
    }

    protected UUID updateDatabaseEntry(String tableName, Object entryToSave) throws SQLException {

        StringBuilder sqlQuery = new StringBuilder("UPDATE " + tableName + " SET ");

        List<String> columnNames = getColumnNames(tableName);

        // Gets each field for the Object being saved to the Database
        Field[] attributes = entryToSave.getClass().getDeclaredFields();
        Object attributeValue;
        UUID id = null; // Need to get ID so we know which entry to update

        for (int i = 0; i < attributes.length; i++) {

            // Required because an Object's fields are usually private
            attributes[i].setAccessible(true);

            // Tells database what values to set for each column
            // Number of columns should match number of attributes in the entry Object
            // being passed as a parameter
            sqlQuery.append(columnNames.get(i) + "=");

            try {

                // Gets the value for the specified field of the Object
                attributeValue = attributes[i].get(entryToSave);

                if (attributeValue == null) {
                    sqlQuery.append("NULL");
                    // Strings, Enums, UUIDs
                } else if (attributeValue.getClass().equals(String.class) || attributeValue.getClass().isEnum()
                        || attributeValue.getClass().equals(UUID.class)) {
                    sqlQuery.append("'" + sanitizeInputString(attributeValue.toString()) + "'");
                    // Timestamps
                } else if (attributeValue.getClass().equals(Timestamp.class)) {
                    sqlQuery.append(formatTimestamp((Timestamp) attributeValue));
                } else {
                    sqlQuery.append(sanitizeInputString(attributeValue.toString()));
                }

                // If the current attribute is the ID, save this value
                if (attributes[i].getName().equals("id") && attributeValue != null) {
                    id = (UUID) attributeValue;
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }

            // Don't want a ',' for the last entry
            if (i < attributes.length - 1) {
                sqlQuery.append(",");
            }
        }

        sqlQuery.append(" WHERE id='" + id + "';");

        statement = connection.createStatement();
        statement.executeUpdate(sqlQuery.toString());

        return id;
    }

    protected void deleteDatabaseEntry(String tableName, UUID id) throws SQLException {
        String sqlQuery = "DELETE FROM " + tableName + " WHERE id = '" + id + "'";
        executeQuery(sqlQuery);
    }

    /**
     * Converts a given String to it's SQL equivalent (i.e., it removes
     * capitalization and inserts underscores where appropriate).
     * 
     * @param stringBeingConverted - The String to convert
     * @return The parameter String in the syntax of an SQL column/table
     */
    private String convertStringToSQLSyntax(String stringBeingConverted) {

        StringBuilder columnName = new StringBuilder(stringBeingConverted);
        int charIndexToLowerCase = findFirstCapitalLetterIndex(0, columnName.toString());
        int underscoresInserted = charIndexToLowerCase == -1 ? 0 : 1;

        // If there are multiple Uppercase Characters, insert a '_' for each
        do {
            if (charIndexToLowerCase > 0) {
                columnName.insert(charIndexToLowerCase, "_");
                underscoresInserted++;
            }

            charIndexToLowerCase = findFirstCapitalLetterIndex(charIndexToLowerCase + underscoresInserted,
                    columnName.toString());
        } while (charIndexToLowerCase > 0);

        return columnName.toString().toLowerCase();
    }

    /**
     * Finds the index of the first instance of a capital letter within a given
     * String, starting from the given startingIndex
     * 
     * @param startingIndex - The index of the String to begin the search
     * @param s             - The String that is being searched
     * @return Index of the first instance of a capital letter, or Negative One (-1)
     *         if no
     *         capital letter was found
     */
    private int findFirstCapitalLetterIndex(int startingIndex, String s) {

        for (int i = startingIndex; i < s.length(); i++) {
            if (Character.isUpperCase(s.codePointAt(i))) {
                return i;
            }
        }

        // No instance of a capital character found
        return -1;
    }

    /**
     * Return the names of each column for a given SQL database table
     * 
     * @param tableName - Name of SQL database table, as a String
     * @return Names of table columns, as a List<String>
     * @throws SQLException
     */
    private List<String> getColumnNames(String tableName) throws SQLException {

        statement = connection.createStatement();

        ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName);
        ResultSetMetaData rsmd = resultSet.getMetaData();

        List<String> columnNames = new ArrayList<>();

        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            columnNames.add(rsmd.getColumnName(i));
        }

        return columnNames;
    }

    /**
     * Gets the fields for a given Class and any of it's Superclasses (that aren't a
     * custom template Class)
     * 
     * @param clazz  - the Class whose fields are being retrieved
     * @param fields - the List to store all the Fields
     */
    private void getAllFields(Class<?> clazz, List<Field> fields) {
        fields.addAll(0, Arrays.asList(clazz.getDeclaredFields()));

        if (clazz.getSuperclass() != ModelTemplate.class && clazz.getSuperclass() != null) {
            getAllFields(clazz.getSuperclass(), fields);
        }
    }

    /**
     * Converts a Timestamp value into a String that can be read by an SQL query. In
     * this case, the SQL query is going to attempt to parse the Timestamp via the
     * 'parsedatetime' function. This function requires the parameters to be in a
     * specific format
     * 
     * @param timestamp - The Timestamp to be formatted
     * @return
     */
    private String formatTimestamp(Timestamp timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp.getTime());

        return "parsedatetime('"
                + cal.get(Calendar.YEAR)
                // Calendar is Zero-indexed, while parsedatetime starts at 1
                + (cal.get(Calendar.MONTH) + 1 < 10 ? "0" + (cal.get(Calendar.MONTH) + 1)
                        : cal.get(Calendar.MONTH) + 1)
                + (cal.get(Calendar.DAY_OF_MONTH) < 10 ? "0" + cal.get(Calendar.DAY_OF_MONTH)
                        : cal.get(Calendar.DAY_OF_MONTH))
                + (cal.get(Calendar.HOUR_OF_DAY) < 10 ? "0" + cal.get(Calendar.HOUR_OF_DAY)
                        : cal.get(Calendar.HOUR_OF_DAY))
                + (cal.get(Calendar.MINUTE) < 10 ? "0" + cal.get(Calendar.MINUTE)
                        : cal.get(Calendar.MINUTE))
                + (cal.get(Calendar.SECOND) < 10 ? "0" + cal.get(Calendar.SECOND)
                        : cal.get(Calendar.SECOND))
                + "', 'yyyyMMddHHmmss')";
    }

    /**
     * Converts multiple Database entries into java Objects. Only works if the
     * Database table being queried has a corresponding java Object.
     * 
     * @param <T>       Any
     * @param clazz     - The class of the Object for Database entry conversion
     * @param resultSet - The Database entries to be converted to Objects
     * @return - A List of Objects representing the Database entries
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws SecurityException
     * @throws SQLException
     */
    @SuppressWarnings("unchecked")
    protected <T> List<T> getMultipleEntries(Class<?> clazz, ResultSet resultSet)
            throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException, SQLException {

        List<T> models = new ArrayList<>();

        // Checks if results are not empty
        if (resultSet.isFirst()) {
            do {
                T object = (T) clazz.getConstructor(ResultSet.class).newInstance(resultSet);
                models.add(object);
            } while ((resultSet.next()));
        }

        return models;
    }

    protected void connectToDatabase() throws SQLException {
        connection = connectionPool.getConnection();
    }

    protected void closeDatabaseConnections() throws SQLException {
        connection.close();
        statement.close();
    }

    /**
     * Formats a String so that it can be used in an SQL statement (e.g., allow the
     * ' character)
     * 
     * @param inputString - the String to be formatted
     * @return - String that can be used in an SQL statement
     */
    private String sanitizeInputString(String inputString) {
        return inputString.replace("'", "''");
    }
}
