-- Widen idempotency_keys.response_body from VARCHAR(4000) to TEXT/CLOB
-- to accommodate larger serialized response DTOs.

ALTER TABLE idempotency_keys
    MODIFY response_body TEXT;