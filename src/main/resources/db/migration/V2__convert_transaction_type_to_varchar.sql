-- V2: Convert transactions.type from PostgreSQL native enum to VARCHAR(20) + CHECK.
-- Motivation: native PostgreSQL enums can cause Hibernate mapping issues when the
-- enum type is referenced by name. VARCHAR(20) + CHECK constraints are equivalent
-- at the DB level and play well with @Enumerated(EnumType.STRING) in JPA.
--
-- Rules:
--   - Do NOT touch V1__init.sql.
--   - Do NOT use DROP TYPE ... CASCADE.
--   - Preserve all existing CHECK constraints and indexes.

-- 1. Drop existing CHECK constraints that reference the enum column (by name from V1).
ALTER TABLE transactions
DROP CONSTRAINT IF EXISTS ck_transaction_topup;

ALTER TABLE transactions
DROP CONSTRAINT IF EXISTS ck_transaction_payment;

ALTER TABLE transactions
DROP CONSTRAINT IF EXISTS ck_transaction_ref;

-- 2. Convert the column type to VARCHAR(20), casting existing enum values to text.
ALTER TABLE transactions
    ALTER COLUMN type TYPE VARCHAR(20) USING type::text;

-- 3. Restore CHECK constraints (now valid against the varchar column).
ALTER TABLE transactions
ADD CONSTRAINT ck_transaction_type CHECK (type IN ('TOP_UP', 'PAYMENT'));

ALTER TABLE transactions
ADD CONSTRAINT ck_transaction_topup CHECK (
    (
        type = 'TOP_UP'
        AND amount > 0.00
    )
    OR type <> 'TOP_UP'
);

ALTER TABLE transactions
ADD CONSTRAINT ck_transaction_payment CHECK (
    (
        type = 'PAYMENT'
        AND amount < 0.00
    )
    OR type <> 'PAYMENT'
);

ALTER TABLE transactions
ADD CONSTRAINT ck_transaction_ref CHECK (
    (
        type = 'PAYMENT'
        AND reference_payment_id IS NOT NULL
    )
    OR (
        type = 'TOP_UP'
        AND reference_payment_id IS NULL
    )
);

-- 4. Drop the now-unused PostgreSQL enum type.
--    NOT CASCADE — all columns were already migrated before this line.
DROP TYPE IF EXISTS transaction_type;