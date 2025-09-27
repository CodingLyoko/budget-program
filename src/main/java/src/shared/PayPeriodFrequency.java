package src.shared;

import java.sql.Timestamp;

import src.handlers.AppUserHandler;

public enum PayPeriodFrequency {
    WEEKLY("Weekly"),
    BIWEEKLY("Biweekly"),
    MONTHLY("Monthly"),
    BIANNUALLY("Biannually"),
    ANNUALLY("Annually");

    private final String payPeriodFrequencyValue;

    private PayPeriodFrequency(String payPeriodFrequency) {
        this.payPeriodFrequencyValue = payPeriodFrequency;
    }

    @Override
    public String toString() {
        return payPeriodFrequencyValue;
    }

    /**
     * Returns the end date of a pay period based on the current pay period
     * frequency
     * 
     * @param startDate - the beginning of the pay period
     * @return A Timestamp representing the end date of a pay period
     */
    public static Timestamp getEndDate(Timestamp startDate) {

        switch (AppUserHandler.getAppUserInstance().getPayPeriodFrequency()) {
            case WEEKLY:
                return Timestamp.valueOf(startDate.toLocalDateTime().plusWeeks(1));
            case BIWEEKLY:
                return Timestamp.valueOf(startDate.toLocalDateTime().plusWeeks(2));
            case MONTHLY:
                return Timestamp.valueOf(startDate.toLocalDateTime().plusMonths(1));
            case BIANNUALLY:
                return Timestamp.valueOf(startDate.toLocalDateTime().plusMonths(6));
            case ANNUALLY:
                return Timestamp.valueOf(startDate.toLocalDateTime().plusYears(1));
            default:
                return Timestamp.valueOf(startDate.toLocalDateTime().plusWeeks(2));
        }
    }
}
