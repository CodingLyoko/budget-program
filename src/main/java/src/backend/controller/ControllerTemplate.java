package src.backend.controller;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.tinylog.Logger;

import src.backend.service.ServiceTemplate;

public class ControllerTemplate {

    InnerServiceTemplate serviceTemplate = new InnerServiceTemplate();

    protected static final String SQL_ERROR_TEXT = "An SQL error occurred: {}";
    protected static final String NULL_POINTER_ERROR_TEXT = "A Null Pointer Exception occurred: {}";
    protected static final String GENERAL_ERROR_TEXT = "General Exception Catch: {}";

    protected ControllerTemplate() {
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
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getAllEntries(Class<T> clazz) {

        List<T> result = new ArrayList<>();

        try {

            result = (List<T>) serviceTemplate.getAllEntries(clazz);

            Logger.info("Successfully retrieved all {}s", clazz.getSimpleName());
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.error(SQL_ERROR_TEXT, Arrays.toString(e.getStackTrace()));
        } catch (Throwable e) {
            e.printStackTrace();
            Logger.error(GENERAL_ERROR_TEXT, Arrays.toString(e.getStackTrace()));
        }

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
     */
    public Object getEntryById(UUID entryId, Class<?> clazz) {

        Object result = null;

        try {
            result = serviceTemplate.getEntryById(entryId, clazz);

            Logger.info("Successfully retrieved {} with ID: {}", clazz.getSimpleName(), entryId);
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.error(SQL_ERROR_TEXT, Arrays.toString(e.getStackTrace()));
        } catch (Throwable e) {
            e.printStackTrace();
            Logger.error(GENERAL_ERROR_TEXT, Arrays.toString(e.getStackTrace()));
        }

        return result;
    }

    /**
     * Get a single Database entry based on ID value and the Class of the Object
     * being retrieved. Only works if the name of the Class is the same as the name
     * of the Database table.
     * 
     * @param entryKey - the Primary Key of the Object in the Database, as a String
     * @param clazz   - the Class of the Object whose data is being retrieved from
     *                the Database
     * @return An Object representing the entry retrieved from the Database
     */
    public Object getEntryByEnum(Enum<?> entryKey, Class<?> clazz) {

        Object result = null;

        try {
            result = serviceTemplate.getEntryByEnum(entryKey, clazz);

            Logger.info("Successfully retrieved {} with Primary Key: {}", clazz.getSimpleName(), entryKey);
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.error(SQL_ERROR_TEXT, Arrays.toString(e.getStackTrace()));
        } catch (Throwable e) {
            e.printStackTrace();
            Logger.error(GENERAL_ERROR_TEXT, Arrays.toString(e.getStackTrace()));
        }

        return result;
    }

    /**
     * Saves an Object representation of a Database entry into a specified table in
     * the database.
     * 
     * @param entryToSave - An Object with fields that match the columns of the
     *                    table being written to
     * @return The UUID of the newly-created Database entry
     */
    public UUID saveEntry(Object entryToSave) {

        UUID result = null;

        try {
            result = serviceTemplate.saveEntry(entryToSave);

            Logger.info("Successfully saved {} with ID: {}", entryToSave.getClass().getSimpleName(), result);
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.error(SQL_ERROR_TEXT, Arrays.toString(e.getStackTrace()));
        } catch (Throwable e) {
            e.printStackTrace();
            Logger.error(GENERAL_ERROR_TEXT, Arrays.toString(e.getStackTrace()));
        }

        return result;
    }

    /**
     * Updates an entry in the Database based on the Object being passed as a
     * parameter. The ID of the Object must match an ID of an entry in the Database
     * 
     * @param entryToUpdate - an Object representation of the Database entry being
     *                      updated.
     * @return The UUID of the Database entry that was updated
     */
    public UUID updateEntry(Object entryToUpdate) {

        UUID result = null;

        try {
            result = serviceTemplate.updateEntry(entryToUpdate);

            Logger.info("Successfully updated {} with ID: {}", entryToUpdate.getClass().getSimpleName(), result);
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.error(SQL_ERROR_TEXT, Arrays.toString(e.getStackTrace()));
        } catch (Throwable e) {
            e.printStackTrace();
            Logger.error(GENERAL_ERROR_TEXT, Arrays.toString(e.getStackTrace()));
        }

        return result;
    }

    /**
     * Delete an entry in the Database based on th Object being passed as a
     * parameter. The ID of the Object must match an ID of an entry in the Database
     * 
     * @param entryToDelete - an Object representation of the Database entry being
     *                      deleted.
     * @return The UUID of the deleted Database entry
     */
    public UUID deleteEntry(Object entryToDelete) {

        UUID result = null;

        try {
            result = serviceTemplate.deleteEntry(entryToDelete);

            Logger.info("Successfully deleted {} with ID: {}", entryToDelete.getClass().getSimpleName(), result);
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.error(SQL_ERROR_TEXT, Arrays.toString(e.getStackTrace()));
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            Logger.error(NULL_POINTER_ERROR_TEXT, Arrays.toString(npe.getStackTrace()));
        } catch (Throwable e) {
            e.printStackTrace();
            Logger.error(GENERAL_ERROR_TEXT, Arrays.toString(e.getStackTrace()));
        }

        return result;
    }

    private class InnerServiceTemplate extends ServiceTemplate {

        protected <T> List<T> getAllEntries(Class<?> clazz)
                throws InstantiationException, IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, SQLException {
            return super.getAllEntries(clazz);
        }

        protected Object getEntryById(UUID entryId, Class<?> clazz)
                throws InstantiationException, IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, SQLException {
            return super.getEntryById(entryId, clazz);
        }

        protected Object getEntryByEnum(Enum<?> enumValue, Class<?> clazz)
                throws InstantiationException, IllegalAccessException, IllegalArgumentException,
                InvocationTargetException, NoSuchMethodException, SecurityException, SQLException {
            return super.getEntryByEnum(enumValue, clazz);
        }

        protected UUID saveEntry(Object entryToSave) throws IllegalAccessException, InvocationTargetException,
                NoSuchMethodException, SecurityException, SQLException {
            return super.saveEntry(entryToSave);
        }

        protected UUID updateEntry(Object entryToUpdate) throws SQLException {
            return super.updateEntry(entryToUpdate);
        }

        protected UUID deleteEntry(Object entryToDelete) throws IllegalAccessException, InvocationTargetException,
                NoSuchMethodException, SecurityException, SQLException {
            return super.deleteEntry(entryToDelete);
        }
    }
}
