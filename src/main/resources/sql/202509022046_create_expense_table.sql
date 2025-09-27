CREATE TABLE IF NOT EXISTS expense (
    id UUID PRIMARY KEY,
    expense_name VARCHAR(256),
    spending_limit DOUBLE,
    current_amount_spent DOUBLE,
    expense_type VARCHAR(32), -- Enum
    pay_period UUID,

    CONSTRAINT fk_pay_period FOREIGN KEY (pay_period) REFERENCES pay_period(id)
);