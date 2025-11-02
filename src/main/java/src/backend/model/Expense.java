package src.backend.model;

import java.sql.ResultSet;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
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
}
