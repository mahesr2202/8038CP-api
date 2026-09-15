-- ─────────────────────────────────────────────────────────────
-- V2: Split monolithic form_submissions into per-section tables
-- ─────────────────────────────────────────────────────────────

-- 1. Part I — Entity Receiving Payment
CREATE TABLE IF NOT EXISTS form8038cp.form_part_i (
    id            BIGSERIAL PRIMARY KEY,
    submission_id BIGINT NOT NULL UNIQUE REFERENCES form8038cp.form_submissions(id) ON DELETE CASCADE,
    line1         VARCHAR(80),
    line2         VARCHAR(10),
    line3_street  VARCHAR(60),
    line3_room    VARCHAR(10),
    line4         VARCHAR(60),
    line5         VARCHAR(60),
    line6         VARCHAR(20)
);

-- 2. Part II — Reporting Authority
CREATE TABLE IF NOT EXISTS form8038cp.form_part_ii (
    id            BIGSERIAL PRIMARY KEY,
    submission_id BIGINT NOT NULL UNIQUE REFERENCES form8038cp.form_submissions(id) ON DELETE CASCADE,
    line7         VARCHAR(80),
    line8         VARCHAR(10),
    line9_street  VARCHAR(60),
    line9_room    VARCHAR(10),
    line10        VARCHAR(3),
    line11        VARCHAR(60),
    line12        VARCHAR(10),
    line13        VARCHAR(80),
    line14        VARCHAR(10),   -- EIN: XX-XXXXXXX = 10 chars
    line15        VARCHAR(60),
    line16        VARCHAR(20),
    line17a       VARCHAR(10),
    line17b       VARCHAR(20),
    line17c       VARCHAR(5)
);

-- 3. Part III — Payment of Credit
CREATE TABLE IF NOT EXISTS form8038cp.form_part_iii (
    id            BIGSERIAL PRIMARY KEY,
    submission_id BIGINT NOT NULL UNIQUE REFERENCES form8038cp.form_submissions(id) ON DELETE CASCADE,
    line18        VARCHAR(10),
    line19a       VARCHAR(20),
    line19b       VARCHAR(10),
    line19c       VARCHAR(20),
    line21a       VARCHAR(20),
    line21b       VARCHAR(20),
    line21c_code  VARCHAR(5),
    line21c_date  VARCHAR(10),
    line22        NUMERIC(15, 2),
    line23a       VARCHAR(3),
    line23b       VARCHAR(3),
    line24a       VARCHAR(3),
    line24b       VARCHAR(5),
    line25        VARCHAR(3)
);

-- 4. Direct Deposit
CREATE TABLE IF NOT EXISTS form8038cp.form_direct_deposit (
    id             BIGSERIAL PRIMARY KEY,
    submission_id  BIGINT NOT NULL UNIQUE REFERENCES form8038cp.form_submissions(id) ON DELETE CASCADE,
    routing_number VARCHAR(9),
    account_type   VARCHAR(10),
    account_number VARCHAR(17)
);

-- 5. Signature
CREATE TABLE IF NOT EXISTS form8038cp.form_signature (
    id             BIGSERIAL PRIMARY KEY,
    submission_id  BIGINT NOT NULL UNIQUE REFERENCES form8038cp.form_submissions(id) ON DELETE CASCADE,
    sig_signature  VARCHAR(100),
    sig_date       VARCHAR(10),
    sig_name_title VARCHAR(80)
);

-- 6. Paid Preparer
CREATE TABLE IF NOT EXISTS form8038cp.form_paid_preparer (
    id                BIGSERIAL PRIMARY KEY,
    submission_id     BIGINT NOT NULL UNIQUE REFERENCES form8038cp.form_submissions(id) ON DELETE CASCADE,
    prep_name         VARCHAR(60),
    prep_signature    VARCHAR(60),
    prep_date         VARCHAR(10),
    prep_self_employed BOOLEAN DEFAULT FALSE,
    prep_ptin         VARCHAR(11),
    prep_firm_name    VARCHAR(60),
    prep_firm_ein     VARCHAR(10),
    prep_phone        VARCHAR(20),
    prep_firm_address VARCHAR(80)
);

-- ─────────────────────────────────────────────────────────────
-- Drop columns moved to section tables from form_submissions
-- ─────────────────────────────────────────────────────────────
ALTER TABLE form8038cp.form_submissions
    DROP COLUMN IF EXISTS line1,
    DROP COLUMN IF EXISTS line2,
    DROP COLUMN IF EXISTS line3_street,
    DROP COLUMN IF EXISTS line3_room,
    DROP COLUMN IF EXISTS line4,
    DROP COLUMN IF EXISTS line5,
    DROP COLUMN IF EXISTS line6,
    DROP COLUMN IF EXISTS line7,
    DROP COLUMN IF EXISTS line8,
    DROP COLUMN IF EXISTS line9_street,
    DROP COLUMN IF EXISTS line9_room,
    DROP COLUMN IF EXISTS line10,
    DROP COLUMN IF EXISTS line11,
    DROP COLUMN IF EXISTS line12,
    DROP COLUMN IF EXISTS line13,
    DROP COLUMN IF EXISTS line14,
    DROP COLUMN IF EXISTS line15,
    DROP COLUMN IF EXISTS line16,
    DROP COLUMN IF EXISTS line17a,
    DROP COLUMN IF EXISTS line17b,
    DROP COLUMN IF EXISTS line17c,
    DROP COLUMN IF EXISTS line18,
    DROP COLUMN IF EXISTS line19a,
    DROP COLUMN IF EXISTS line19b,
    DROP COLUMN IF EXISTS line19c,
    DROP COLUMN IF EXISTS line21a,
    DROP COLUMN IF EXISTS line21b,
    DROP COLUMN IF EXISTS line21c_code,
    DROP COLUMN IF EXISTS line21c_date,
    DROP COLUMN IF EXISTS line22,
    DROP COLUMN IF EXISTS line23a,
    DROP COLUMN IF EXISTS line23b,
    DROP COLUMN IF EXISTS line24a,
    DROP COLUMN IF EXISTS line24b,
    DROP COLUMN IF EXISTS line25,
    DROP COLUMN IF EXISTS routing_number,
    DROP COLUMN IF EXISTS account_type,
    DROP COLUMN IF EXISTS account_number,
    DROP COLUMN IF EXISTS sig_signature,
    DROP COLUMN IF EXISTS sig_date,
    DROP COLUMN IF EXISTS sig_name_title,
    DROP COLUMN IF EXISTS prep_name,
    DROP COLUMN IF EXISTS prep_signature,
    DROP COLUMN IF EXISTS prep_date,
    DROP COLUMN IF EXISTS prep_self_employed,
    DROP COLUMN IF EXISTS prep_ptin,
    DROP COLUMN IF EXISTS prep_firm_name,
    DROP COLUMN IF EXISTS prep_firm_ein,
    DROP COLUMN IF EXISTS prep_phone,
    DROP COLUMN IF EXISTS prep_firm_address;
