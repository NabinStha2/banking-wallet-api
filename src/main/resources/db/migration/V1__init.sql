create TABLE users
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    full_name     VARCHAR(120) NOT NULL,
    user_role     VARCHAR(20)  NOT NULL,
    enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    DATETIME(6)  NOT NULL,
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE = InnoDB;

create TABLE accounts
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT         NOT NULL,
    account_number VARCHAR(20)    NOT NULL,
    currency       VARCHAR(3)     NOT NULL DEFAULT 'USD',
    balance        DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    status         VARCHAR(20)    NOT NULL,
    daily_limit    DECIMAL(19, 2) NOT NULL DEFAULT 5000.00,
    version        BIGINT         NOT NULL DEFAULT 0,
    created_at     DATETIME(6)    NOT NULL,
    CONSTRAINT uk_accounts_number UNIQUE (account_number),
    CONSTRAINT chk_accounts_balance CHECK (balance >= 0),
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB;

create index idx_accounts_user on accounts (user_id);

create TABLE transactions
(
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id              BIGINT         NOT NULL,
    type                    VARCHAR(20)    NOT NULL,
    amount                  DECIMAL(19, 2) NOT NULL,
    balance_after           DECIMAL(19, 2) NOT NULL,
    counterparty_account_id BIGINT         NULL,
    reference_id            VARCHAR(36)    NOT NULL,
    description             VARCHAR(255)   NULL,
    created_at              DATETIME(6)    NOT NULL,
    CONSTRAINT fk_tx_account FOREIGN KEY (account_id) REFERENCES accounts (id)
) ENGINE = InnoDB;

create index idx_tx_account_created on transactions (account_id, created_at);
create index idx_tx_reference on transactions (reference_id);
create index idx_tx_type on transactions (type);

create TABLE idempotency_keys
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id         BIGINT        NOT NULL,
    key_value       VARCHAR(100)  NOT NULL,
    request_hash    VARCHAR(64)   NOT NULL,
    status          VARCHAR(20)   NOT NULL,
    response_status INT           NULL,
    response_body   VARCHAR(4000) NULL,
    created_at      DATETIME(6)   NOT NULL,
    CONSTRAINT uk_idem_user_key UNIQUE (user_id, key_value),
    CONSTRAINT fk_idem_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB;

create TABLE audit_logs
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_user_id BIGINT        NULL,
    action        VARCHAR(50)   NOT NULL,
    entity_type   VARCHAR(30)   NULL,
    entity_id     BIGINT        NULL,
    details       VARCHAR(4000) NULL,
    ip_address    VARCHAR(45)   NULL,
    created_at    DATETIME(6)   NOT NULL
) ENGINE = InnoDB;

create index idx_audit_actor on audit_logs (actor_user_id);
create index idx_audit_action_created on audit_logs (action, created_at);
