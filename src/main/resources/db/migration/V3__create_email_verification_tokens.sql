CREATE TABLE email_verification_tokens
(
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    user_id    BIGINT      NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    expires_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    used_at    DATETIME(6) NULL,

    PRIMARY KEY (id),
    CONSTRAINT uk_email_verification_token_hash UNIQUE (token_hash),
    KEY idx_email_verification_token_user (user_id),
    CONSTRAINT fk_email_verification_token_user
        FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB;
