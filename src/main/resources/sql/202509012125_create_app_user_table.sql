CREATE TABLE IF NOT EXISTS app_user (
    id UUID PRIMARY KEY,
    total_funds DOUBLE,
    savings DOUBLE, -- TODO: REMOVE
    pay_period_frequency VARCHAR(64) -- ENUM
);

--INSERT INTO app_user VALUES(UUID(), 50000.00, 38000.78, 'Bi-weekly');