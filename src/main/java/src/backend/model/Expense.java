package src.backend.model;

import java.sql.ResultSet;
import java.util.UUID;

import org.tinylog.Logger;

import lombok.Getter;
import lombok.Setter;
import src.handlers.AppUserHandler;
import src.shared.ExpenseType;

@Setter
@Getter
public class Expense extends ModelTemplate {

    public Expense() {
        id = UUID.randomUUID();
        expenseName = "Default Expense Name";
        spendingLimit = 0.0;
        currentAmountSpent = 0.0;
        expenseType = ExpenseType.EXPENSE;
        isFavorite = false;
    }

    public Expense(ResultSet resultSet) {
        super(resultSet);
    }

    private UUID id;
    private String expenseName;
    private Double spendingLimit;
    private Double currentAmountSpent;
    private ExpenseType expenseType;
    private UUID payPeriod;
    private Boolean isFavorite;

    /**
     * Updates the ExpenseType for this Expense. Updates funding values in the
     * database to reflect the new value.
     * 
     * @param newExpenseType - the new ExpenseType value
     * @return
     */
    public Boolean updateExpenseType(ExpenseType newExpenseType) {

        Boolean result = false;

        // Initial Funding Values
        Double initialTotalFunds = AppUserHandler.getAppUserInstance().getTotalFunds();
        Double initialSavingsFunds = AppUserHandler.getAppUserInstance().getSavingFunds();
        Double initialReservedFunds = AppUserHandler.getAppUserInstance().getReservedFunds();
        Double initialAvailableFunds = AppUserHandler.getAppUserInstance().getAvailableFunds();

        // Updates funding values based on new ExpenseType value
        try {
            switch (newExpenseType) {
                case ExpenseType.EXPENSE:
                    AppUserHandler.updateAvailableFunds(this.spendingLimit * -1.0);
                    break;

                case ExpenseType.INCOME:
                    // Adds funds to Total Funds value (and updates Available Funds accordingly)
                    AppUserHandler.updateTotalFunds(this.spendingLimit * -1.0);
                    break;

                case ExpenseType.SAVINGS:

                    // Adds funds to the Savings value
                    AppUserHandler.updateSavingFunds(this.spendingLimit);
                    break;

                case ExpenseType.RESERVED:

                    // Adds funds to the Reserved value
                    AppUserHandler.updateReservedFunds(this.spendingLimit);
                    break;
                default:
                    break;
            }

            // Update funding values based on old ExpenseType value
            switch (this.expenseType) {
                case ExpenseType.EXPENSE:

                    // Adds back the allocated funds from the given Expense
                    AppUserHandler.updateAvailableFunds(this.spendingLimit - this.currentAmountSpent);
                    AppUserHandler.updateTotalFunds(this.currentAmountSpent);
                    break;

                case ExpenseType.INCOME:

                    // Removes funds from the Total Funds value (and updates Available Funds
                    // accordingly)
                    AppUserHandler.updateTotalFunds(this.spendingLimit * -1.0);
                    break;

                case ExpenseType.SAVINGS:

                    // Removes funds from the Savings value
                    AppUserHandler.updateSavingFunds(this.spendingLimit * -1.0);
                    break;

                case ExpenseType.RESERVED:

                    // Removes funds from the Reserved value
                    AppUserHandler.updateReservedFunds(this.spendingLimit * -1.0);
                    break;
                default:
                    break;
            }

            this.expenseType = newExpenseType;

            result = true;
        } catch (Exception e) {
            Logger.error("Something went wrong trying to update the ExpenseType for Expense with UUID: {}.\n {}",
                    this.id, e.getMessage());

            // Revert fundings values to their initial values
            AppUserHandler.getAppUserInstance().setTotalFunds(initialTotalFunds);
            AppUserHandler.getAppUserInstance().setSavingFunds(initialSavingsFunds);
            AppUserHandler.getAppUserInstance().setReservedFunds(initialReservedFunds);
            AppUserHandler.getAppUserInstance().setAvailableFunds(initialAvailableFunds);

            // Save reverted changes to the database
            AppUserHandler.updateAppUser();
        }

        return result;
    }
}
