CREATE TABLE account (
    id SERIAL PRIMARY KEY,
    customer_id BIGINT UNIQUE NOT NULL,
    country_code VARCHAR(2) NOT NULL
);

CREATE TABLE balance (
    id SERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    amount NUMERIC(20, 2) NOT NULL,

    CONSTRAINT balance_account_id_fkey FOREIGN KEY (account_id) REFERENCES account (id)
);

CREATE TABLE transaction (
    id SERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    amount NUMERIC(20, 2) NOT NULL,
    direction VARCHAR(255) NOT NULL,
    description VARCHAR(255) NOT NULL,

    CONSTRAINT transaction_account_id_fkey FOREIGN KEY (account_id) REFERENCES account (id)
)