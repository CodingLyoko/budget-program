package src.backend.controller;

import java.sql.SQLException;

import org.tinylog.Logger;

import src.backend.model.AppUser;
import src.backend.service.AppUserService;

public class AppUserController extends ControllerTemplate {

    private AppUserService appUserService = new AppUserService();

    public AppUser getAppUser() {

        AppUser result = null;

        try {
            result = appUserService.getAppUser();

            Logger.info("Successfully retrieved App User data!");
        } catch (SQLException sqle) {
            Logger.error(SQL_ERROR_TEXT, sqle.getMessage());
        } catch (Exception e) {
            Logger.error(GENERAL_ERROR_TEXT, e.getMessage());
        }

        return result;
    }
}
