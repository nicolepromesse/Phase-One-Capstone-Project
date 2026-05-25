CREATE TABLE customers (
                           id SERIAL PRIMARY KEY,
                           full_name VARCHAR(100) NOT NULL,
                           email VARCHAR(100) UNIQUE NOT NULL,
                           phone_number VARCHAR(20) UNIQUE NOT NULL,
                           pin VARCHAR(10) NOT NULL
);


CREATE TABLE accounts (
                          id SERIAL PRIMARY KEY,
                          customer_id INT NOT NULL,
                          account_type VARCHAR(20) NOT NULL,
                          balance DECIMAL(15,2) DEFAULT 0,
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT fk_customer
                              FOREIGN KEY (customer_id)
                                  REFERENCES customers(id)
                                  ON DELETE CASCADE
);


CREATE TABLE transactions (
                              id SERIAL PRIMARY KEY,
                              account_id INT NOT NULL,
                              reference_id VARCHAR(100) UNIQUE NOT NULL,
                              transaction_type VARCHAR(30) NOT NULL,
                              amount DECIMAL(15,2) NOT NULL,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT fk_account
                                  FOREIGN KEY (account_id)
                                      REFERENCES accounts(id)
                                      ON DELETE CASCADE
);

CREATE TABLE processed_requests (
                                    id SERIAL PRIMARY KEY,
                                    reference_id VARCHAR(100) UNIQUE NOT NULL,
                                    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);