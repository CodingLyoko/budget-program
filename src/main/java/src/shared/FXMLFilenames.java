package src.shared;

public enum FXMLFilenames {
    PAY_PERIOD_FREQUENCY_SELECTION_POPUP("pay_period_frequency_selection_popup"),
    EXPENSES_PAGE("expenses_page"),
    SET_TOTAL_FUNDS_POPUP("set_total_funds_popup"),
    ADD_TO_TOTAL_FUNDS_POPUP("add_to_total_funds_popup"),
    CREATE_PAY_PERIOD_POPUP("create_pay_period_popup"),
    CREATE_EXPENSE_POPUP("create_expense_popup"),
    UPDATE_EXPENSE_POPUP("update_expense_popup"),
    ADD_AMOUNT_SPENT_POPUP("add_amount_spent_popup");
    

    private final String fxmlFilename;

    private FXMLFilenames(String fxmlFilename) {
        this.fxmlFilename = fxmlFilename;
    }

    @Override
    public String toString() {
        return fxmlFilename;
    }
}
