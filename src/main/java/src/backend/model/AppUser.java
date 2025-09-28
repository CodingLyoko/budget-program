package src.backend.model;

import java.sql.ResultSet;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;
import src.shared.PayPeriodFrequency;

@Setter
@Getter
public class AppUser extends ModelTemplate {

    public AppUser() {
        this.totalFunds = 0.0;
        this.reservedFunds = 0.0;
        this.savingFunds = 0.0;
        this.availableFunds = 0.0;
        this.payPeriodFrequency = PayPeriodFrequency.BIWEEKLY;
    }

    public AppUser(ResultSet resultSet) {
        super(resultSet);
    }

    private UUID id;
    private Double totalFunds;
    private Double reservedFunds;
    private Double savingFunds;
    private Double availableFunds;
    private PayPeriodFrequency payPeriodFrequency;
}
