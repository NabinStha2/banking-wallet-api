-- Add foreign key on transactions.counterparty_account_id referencing accounts(id)
-- This ensures referential integrity for the counterparty in TRANSFER_IN/OUT records.

ALTER TABLE transactions
    ADD CONSTRAINT fk_tx_counterparty FOREIGN KEY (counterparty_account_id) REFERENCES accounts (id);