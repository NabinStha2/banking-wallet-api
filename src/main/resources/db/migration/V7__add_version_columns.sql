-- Add optimistic-lock version columns to users and email_verification_tokens

ALTER TABLE users
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE email_verification_tokens
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;