CREATE TABLE IF NOT EXISTS pay_period (
    id UUID PRIMARY KEY,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    is_current BOOLEAN
);

--INSERT INTO pay_period VALUES(UUID(), '2024-01-01 00:00:01', '2024-01-02 00:00:01', 'false');