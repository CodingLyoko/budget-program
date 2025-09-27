package src.shared;

public enum EnumClassnames {
    EXPENSE_TYPES("src.shared.ExpenseType"),
    PAY_PERIOD_FREQUENCIES("src.shared.PayPeriodFrequency");

    private final String enumClassname;

    private EnumClassnames(String enumClassname) {
        this.enumClassname = enumClassname;
    }

    @Override
    public String toString() {
        return enumClassname;
    }
}
