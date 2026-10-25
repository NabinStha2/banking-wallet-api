ALTER TABLE email_verification_tokens
    DROP INDEX idx_email_verification_token_user,
    ADD CONSTRAINT uk_email_verification_token_user UNIQUE (user_id);
