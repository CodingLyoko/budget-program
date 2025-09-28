package src.handlers;

import org.tinylog.Logger;

import src.backend.controller.AppUserController;
import src.backend.model.AppUser;
import src.shared.PayPeriodFrequency;

public class AppUserHandler {

    private AppUserHandler() {
    }

    private static final AppUserController appUserController = new AppUserController();
    private static AppUser appUser;

    public static AppUser getAppUserInstance() {
        if (appUser == null) {
            appUser = appUserController.getAppUser();
        }

        return appUser;
    }

    public static void setTotalFunds(Double newTotalFunds) {
        Double oldTotalFunds = appUser.getTotalFunds();

        updateTotalFunds(newTotalFunds - oldTotalFunds);
        updateAppUser();

        Logger.info("Successfully set total funds for the User.");
    }

    public static void updateTotalFunds(Double changeInFunds) {

        Double newTotalFunds = appUser.getTotalFunds();
        newTotalFunds += changeInFunds;

        Double newAvailableFunds = appUser.getAvailableFunds();
        newAvailableFunds += changeInFunds;

        appUser.setTotalFunds(newTotalFunds);
        appUser.setAvailableFunds(newAvailableFunds);

        updateAppUser();

        Logger.info("Successfully updated total funds for the User.");
    }

    public static void updateAvailableFunds(Double changeInFunds) {

        Double newAvailableFunds = appUser.getAvailableFunds();
        newAvailableFunds += changeInFunds;

        appUser.setAvailableFunds(newAvailableFunds);
        updateAppUser();

        Logger.info("Successfully updated available funds for the User.");
    }

    public static void updateReservedFunds(Double changeInFunds) {

        Double newReservedFunds = appUser.getReservedFunds();
        newReservedFunds += changeInFunds;

        Double newAvailableFunds = appUser.getAvailableFunds();
        newAvailableFunds -= changeInFunds;

        appUser.setReservedFunds(newReservedFunds);
        appUser.setAvailableFunds(newAvailableFunds);

        updateAppUser();

        Logger.info("Successfully updated reserved funds for the User.");
    }

    public static void updateSavingFunds(Double changeInFunds) {

        Double newSavingFunds = appUser.getSavingFunds();
        newSavingFunds += changeInFunds;

        Double newAvailableFunds = appUser.getAvailableFunds();
        newAvailableFunds -= changeInFunds;

        appUser.setSavingFunds(newSavingFunds);
        appUser.setAvailableFunds(newAvailableFunds);

        updateAppUser();

        Logger.info("Successfully updated saving funds for the User.");
    }

    public static void updatePayPeriodFreq(PayPeriodFrequency payPeriodFreq) {
        appUser.setPayPeriodFrequency(payPeriodFreq);
        updateAppUser();

        Logger.info("Successfully updated pay period frequency for the User.");
    }

    /**
     * Call this method whenever a change made to the App User should persist after
     * closing the application
     */
    private static void updateAppUser() {
        appUserController.updateEntry(appUser);
    }
}
