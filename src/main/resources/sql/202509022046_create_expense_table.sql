CREATE TABLE IF NOT EXISTS expense (
    id UUID PRIMARY KEY,
    expense_name VARCHAR(256),
    spending_limit DOUBLE,
    current_amount_spent DOUBLE,
    expense_type VARCHAR(32), -- Enum
    pay_period UUID,

    CONSTRAINT fk_pay_period FOREIGN KEY (pay_period) REFERENCES pay_period(id)
);

--INSERT INTO expense VALUES(UUID(), 'test expense 1', 38000.78, 0, 'EXPENSE', '6d828e03-ee00-4b98-b58d-af957f5cf6ef');
--INSERT INTO expense VALUES(UUID(), 'test savings 1', 38000.78, 0, 'SAVINGS', null);