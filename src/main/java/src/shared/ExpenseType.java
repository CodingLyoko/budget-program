package src.shared;

public enum ExpenseType {
    EXPENSE("Expense"),
    INCOME("Income"),
    SAVINGS("Savings"),
    RESERVED("Reserved");

    private final String expenseTypeValue;

    private ExpenseType(String expenseType) {
        this.expenseTypeValue = expenseType;
    }

    @Override
    public String toString() {
        return expenseTypeValue;
    }
}
