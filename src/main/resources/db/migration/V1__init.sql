-- Extension required for gen_random_uuid()
CREATE
EXTENSION IF NOT EXISTS "pgcrypto";

-- ── Enum types ────────────────────────────────────────────────────────────────

CREATE TYPE transaction_type AS ENUM (
  'TOP_UP',
  'PAYMENT'
);

-- ── Tables ───────────────────────────────────────────────────────────────────

-- One row per user; user_id is the same UUID used in iam_db.users.
-- This is a logical cross-service reference, not a physical FK.
CREATE TABLE wallets
(
    user_id    UUID PRIMARY KEY,
    balance    NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    version    BIGINT         NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ    NOT NULL DEFAULT now(),

    CONSTRAINT ck_wallet_balance CHECK (balance >= 0.00)
);

-- Immutable wallet movement history.
-- Positive amount for TOP_UP; negative amount for PAYMENT.
CREATE TABLE transactions
(
    id                   UUID PRIMARY KEY          DEFAULT gen_random_uuid(),
    wallet_user_id       UUID             NOT NULL,
    type                 transaction_type NOT NULL,
    amount               NUMERIC(12, 2)   NOT NULL,
    transaction_date     DATE             NOT NULL,
    reference_payment_id UUID,
    created_by           UUID             NOT NULL,
    created_at           TIMESTAMPTZ      NOT NULL DEFAULT now(),

    CONSTRAINT ck_transaction_topup CHECK (
        (type = 'TOP_UP' AND amount > 0.00) OR type <> 'TOP_UP'
        ),
    CONSTRAINT ck_transaction_payment CHECK (
        (type = 'PAYMENT' AND amount < 0.00) OR type <> 'PAYMENT'
        ),
    CONSTRAINT ck_transaction_ref CHECK (
        (type = 'PAYMENT' AND reference_payment_id IS NOT NULL) OR
        (type = 'TOP_UP' AND reference_payment_id IS NULL)
        ),
    CONSTRAINT fk_transaction_wallet FOREIGN KEY (wallet_user_id)
        REFERENCES wallets (user_id) ON DELETE RESTRICT
);

CREATE INDEX idx_transaction_wallet ON transactions (wallet_user_id);
CREATE INDEX idx_transaction_date ON transactions (transaction_date);
CREATE INDEX idx_transaction_type ON transactions (type);

-- Immutable payment record.
-- congress_id and institution_id are logical references to conference-service.
-- user_id is a logical reference to iam-service.
CREATE TABLE payments
(
    id                          UUID PRIMARY KEY        DEFAULT gen_random_uuid(),
    user_id                     UUID           NOT NULL,
    congress_id                 UUID           NOT NULL,
    institution_id              UUID           NOT NULL,
    congress_name_snapshot      VARCHAR(255)   NOT NULL,
    institution_name_snapshot   VARCHAR(255)   NOT NULL,
    commission_percent_snapshot NUMERIC(5, 2)  NOT NULL,
    amount                      NUMERIC(12, 2) NOT NULL,
    commission_amount           NUMERIC(12, 2) NOT NULL,
    net_amount                  NUMERIC(12, 2) NOT NULL,
    payment_date                DATE           NOT NULL,
    idempotency_key             VARCHAR(255)   NOT NULL,
    created_by                  UUID           NOT NULL,
    created_at                  TIMESTAMPTZ    NOT NULL DEFAULT now(),

    CONSTRAINT uq_payment_idempotency UNIQUE (idempotency_key),
    CONSTRAINT ck_payment_amounts CHECK (
        amount > 0.00 AND
        commission_amount >= 0.00 AND
        net_amount >= 0.00 AND
        amount = commission_amount + net_amount
        ),
    CONSTRAINT ck_payment_commission_percent CHECK (
        commission_percent_snapshot >= 0.00 AND commission_percent_snapshot <= 100.00
        )
);

CREATE INDEX idx_payment_user ON payments (user_id);
CREATE INDEX idx_payment_congress ON payments (congress_id);
CREATE INDEX idx_payment_institution ON payments (institution_id);
CREATE INDEX idx_payment_date ON payments (payment_date);

-- Singleton configuration table; seeded by Flyway with a single row.
CREATE TABLE system_config
(
    id                 INT PRIMARY KEY        DEFAULT 1,
    commission_percent NUMERIC(5, 2) NOT NULL DEFAULT 10.00,
    updated_by         UUID          NOT NULL,
    updated_at         TIMESTAMPTZ   NOT NULL DEFAULT now(),

    CONSTRAINT ck_commission_range CHECK (
        commission_percent >= 0.00 AND commission_percent <= 100.00
        ),
    CONSTRAINT ck_singleton CHECK (id = 1)
);

INSERT INTO system_config (id,
                           commission_percent,
                           updated_by,
                           updated_at)
VALUES (1,
        10.00,
        '00000000-0000-0000-0000-000000000000',
        now());