package src.backend.model;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import src.shared.EnumClassnames;

public class ModelTemplate {

    @SuppressWarnings("rawtypes")
    private static final Map<String, Class> enumTypes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

    protected ModelTemplate() {
        setObjectAttributes(this.getClass());
    }

    /**
     * Creates an Object based on a ResultSet returned from an SQL database. This
     * constructor utilizes Reflection to automatically retrieve Object attribute
     * names (and their associated values retreived from the database).
     * 
     * @param resultSet - Results from an SQL query to retrieve an associated Object
     */
    protected ModelTemplate(ResultSet resultSet) {

        populateEnumTypes();

        setObjectAttributes(this.getClass(), resultSet);
    }

    /**
     * Creates an Object by retrieving it's fields and setting its attributes to a
     * default value.
     * 
     * @param clazz - the Class of the Object being instantiated
     */
    private void setObjectAttributes(Class<?> clazz) {

        // Gets each field for the Object being instantiated
        Field[] fields = clazz.getDeclaredFields();

        String methodName = "";

        // Set the value for each attribute/field in the Object
        for (int i = 0; i < fields.length; i++) {

            methodName = "set" + fields[i].getName().substring(0, 1).toUpperCase()
                    + fields[i].getName().substring(1);

            // Each variable type needs its own special method invokation
            try {
                // Enums
                if (enumTypes.get(fields[i].getName()) != null) {
                    clazz.getDeclaredMethod(methodName, enumTypes.get(fields[i].getName())).invoke(
                            this, getEnumValue(fields[i].getName(), "0"));
                    // Strings
                } else if (fields[i].getType() == String.class) {
                    clazz.getDeclaredMethod(methodName, String.class).invoke(this, "");
                    // Timestamps
                } else if (fields[i].getType() == Timestamp.class) {
                    clazz.getDeclaredMethod(methodName, Timestamp.class).invoke(this,
                            new Timestamp(System.currentTimeMillis()));
                    // ints
                } else if (fields[i].getType() == int.class) {
                    clazz.getDeclaredMethod(methodName, int.class).invoke(this, 0);
                    // Doubles
                } else if (fields[i].getType() == Double.class) {
                    clazz.getDeclaredMethod(methodName, Double.class).invoke(this, 0.0);
                    // Booleans
                } else if (fields[i].getType() == Boolean.class) {
                    clazz.getDeclaredMethod(methodName, Boolean.class).invoke(this, false);
                }
            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException | SecurityException e) {
                e.printStackTrace();
            }
        }

        // If there are any Superclasses (that aren't a custom template Class), get the
        // attributes for that Superclass
        if (clazz.getSuperclass() != ModelTemplate.class && clazz.getSuperclass() != null) {
            setObjectAttributes(clazz.getSuperclass());
        }
    }

    /**
     * Creates an Object by retrieving it's fields and setting the values of those
     * fields based on data retrieved from a Database.
     * 
     * @param clazz     - the Class of the Object being instantiated
     * @param resultSet - the data for that Object retrieved from the Database
     */
    private void setObjectAttributes(Class<?> clazz, ResultSet resultSet) {
        // Gets each field for the Object being instantiated
        Field[] fields = clazz.getDeclaredFields();

        String methodName = "";
        String columnName = "";

        // Set the value for each attribute/field in the Object
        for (int i = 0; i < fields.length; i++) {
            columnName = getColumnName(fields[i].getName());

            methodName = "set" + fields[i].getName().substring(0, 1).toUpperCase()
                    + fields[i].getName().substring(1);

            // Each variable type needs its own special method invokation
            try {
                // Enums
                if (enumTypes.get(fields[i].getName()) != null) {
                    clazz.getDeclaredMethod(methodName, enumTypes.get(fields[i].getName())).invoke(
                            this,
                            getEnumValue(fields[i].getName(), resultSet.getString(columnName)));
                    // Strings
                } else if (fields[i].getType() == String.class) {
                    clazz.getDeclaredMethod(methodName, String.class).invoke(this,
                            resultSet.getString(columnName));
                    // Timestamps
                } else if (fields[i].getType() == Timestamp.class) {
                    clazz.getDeclaredMethod(methodName, Timestamp.class).invoke(this,
                            resultSet.getTimestamp(columnName));
                    // ints
                } else if (fields[i].getType() == int.class) {
                    clazz.getDeclaredMethod(methodName, int.class).invoke(this,
                            resultSet.getInt(columnName));
                    // Doubles
                } else if (fields[i].getType() == Double.class) {
                    clazz.getDeclaredMethod(methodName, Double.class).invoke(this,
                            resultSet.getDouble(columnName));
                    // Booleans
                } else if (fields[i].getType() == Boolean.class) {
                    clazz.getDeclaredMethod(methodName, Boolean.class).invoke(this, resultSet.getBoolean(columnName));
                    // UUIDs
                } else if (fields[i].getType() == UUID.class) {
                    clazz.getDeclaredMethod(methodName, UUID.class).invoke(this,
                            resultSet.getObject(columnName, java.util.UUID.class));
                }
            } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException
                    | NoSuchMethodException | SQLException | SecurityException e) {
                e.printStackTrace();
            }
        }

        // If there are any Superclasses (that aren't a custom template Class), get the
        // attributes for that Superclass
        if (clazz.getSuperclass() != ModelTemplate.class && clazz.getSuperclass() != null) {
            setObjectAttributes(clazz.getSuperclass(), resultSet);
        }
    }

    /**
     * Get the relevant SQL table column name for a given variable
     * 
     * @param fieldName - Name of the variable associated with an SQL table
     *                  column
     * @return The name of the associated SQL table column, as a String
     */
    private String getColumnName(String fieldName) {

        StringBuilder columnName = new StringBuilder(fieldName);
        int charIndexToLowerCase = findFirstCapitalLetterIndex(0, columnName.toString());
        int underscoresInserted = charIndexToLowerCase == 0 ? 0 : 1;

        // If there are multiple Uppercase Characters, insert a '_' for each
        do {
            if (charIndexToLowerCase != 0) {
                columnName.insert(charIndexToLowerCase, "_");
                underscoresInserted++;
            }

            charIndexToLowerCase = findFirstCapitalLetterIndex(charIndexToLowerCase + underscoresInserted,
                    columnName.toString());
        } while (charIndexToLowerCase != 0);

        return columnName.toString().toLowerCase();
    }

    /**
     * Finds the index of the first instance of a capital letter within a given
     * String, starting from the given startingIndex
     * 
     * @param startingIndex - The index of the String to begin the search
     * @param s             - The String that is being searched
     * @return Index of the first instance of a capital letter, or Zero (0) if no
     *         capital letter was found
     */
    private int findFirstCapitalLetterIndex(int startingIndex, String s) {

        // The +2 is to account for the insertion of the '_' as well as skipping any
        // previously-found capital characters
        for (int i = startingIndex; i < s.length(); i++) {
            if (Character.isUpperCase(s.codePointAt(i))) {
                return i;
            }
        }

        // No instance of a capital character found
        return 0;
    }

    /**
     * Populates a Map with a Key/Value pair of (Enum Name, Enum Class). This Map is
     * used in part to automagically get Enum values based on Non-Enum variable
     * names and values.
     * NOTE: A TreeMap is used to store this information; this is to make the
     * retrieval of map keys case-insensative (i.e., we do not need to worry about
     * case when retrieving a value from the Map).
     */
    private void populateEnumTypes() {
        try {
            for (EnumClassnames enumClassname : EnumClassnames.values()) {
                enumTypes.put(Class.forName(enumClassname.toString()).getSimpleName(),
                        Class.forName(enumClassname.toString()));
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the Enum value for any given Enum. The name of the attribute (whos
     * value is being passed to the Enum) needs to be the same as the Enum type
     * itself (e.g., to get a value from the OccurrenceType Enum, the variable
     * name being passed as a parameter must be OccurrenceType as well (case
     * insensative)).
     * 
     * @param fieldName  - Name of the Object variable/Enum type for that Object
     *                   variable, as a String
     * @param fieldValue - The Enum value, as a String
     * @return Enum-equivalent value of the value parameter
     */
    @SuppressWarnings("unchecked")
    private Enum<?> getEnumValue(String fieldName, String fieldValue) {

        Enum<?> returnValue;

        try {
            returnValue = Enum.valueOf(enumTypes.get(fieldName), fieldValue.toUpperCase());
        } catch (IllegalArgumentException _) {
            // If no existing Enum value, return null
            returnValue = null;
        }

        return returnValue;
    }
}
