-- Run this script once against your Igirepay database to create the loans table.
-- Connect as postgres user on port 2000, database "Igirepay", then execute this file.

CREATE TABLE IF NOT EXISTS loans (
    id               SERIAL PRIMARY KEY,
    account_id       INT NOT NULL,
    customer_id      INT NOT NULL,
    requested_amount DECIMAL(15,2) NOT NULL,
    approved_amount  DECIMAL(15,2) NOT NULL DEFAULT 0,
    interest_rate    DECIMAL(5,2)  NOT NULL DEFAULT 5.00,
    repaid_amount    DECIMAL(15,2) NOT NULL DEFAULT 0,
    status           VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
    reference_id     VARCHAR(100)  UNIQUE NOT NULL,
    requested_at     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_loan_account
        FOREIGN KEY (account_id)
            REFERENCES accounts(id)
            ON DELETE CASCADE,

    CONSTRAINT fk_loan_customer
        FOREIGN KEY (customer_id)
            REFERENCES customers(id)
            ON DELETE CASCADE
);
