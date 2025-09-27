package src.backend.model;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PayPeriod extends ModelTemplate {

    public PayPeriod() {
        id = UUID.randomUUID();
        isCurrent = true;
    }

    public PayPeriod(ResultSet resultSet) {
        super(resultSet);
    }

    private UUID id;
    private Timestamp startDate;
    private Timestamp endDate;
    private Boolean isCurrent;
}
