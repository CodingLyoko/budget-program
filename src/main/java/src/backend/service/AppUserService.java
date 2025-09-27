package src.backend.service;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

import src.backend.model.AppUser;

public class AppUserService extends ServiceTemplate {

    private static final String TABLE_NAME = "app_user";

    public AppUser getAppUser() throws SQLException, IllegalAccessException, InvocationTargetException,
            NoSuchMethodException, SecurityException {

        connectToDatabase();

        ResultSet resultSet = getQueryResults("SELECT * FROM " + TABLE_NAME);

        AppUser appUser;

        // Checks if there is an App User entry in the database
        // If not, create one
        if (resultSet.getRow() != 0) {
            appUser = new AppUser(resultSet);
        } else {
            appUser = new AppUser();
            appUser.setId(UUID.randomUUID());

            saveEntry(appUser);
        }

        closeDatabaseConnections();

        return appUser;
    }
}
