CREATE SCHEMA IF NOT EXISTS form8038cp;

CREATE TABLE form8038cp.users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(75) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    full_name VARCHAR(100),
    phone VARCHAR(20),
    role VARCHAR(10) NOT NULL DEFAULT 'USER',
    status VARCHAR(10) NOT NULL DEFAULT 'ACTIVE',
    channel VARCHAR(10) NOT NULL DEFAULT 'WEB',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    terms_accepted_at TIMESTAMPTZ,
    tokens_valid_from TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE form8038cp.email_verification_challenges (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES form8038cp.users(id),
    challenge_id VARCHAR(36) NOT NULL UNIQUE,
    code VARCHAR(6) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    attempts INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE UNIQUE INDEX idx_evc_challenge_id ON form8038cp.email_verification_challenges(challenge_id);

CREATE TABLE form8038cp.password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES form8038cp.users(id),
    token VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE UNIQUE INDEX idx_prt_token ON form8038cp.password_reset_tokens(token);

CREATE TABLE form8038cp.form_submissions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES form8038cp.users(id),
    is_amended BOOLEAN NOT NULL DEFAULT FALSE,
    -- Part I
    line1 VARCHAR(80),
    line2 VARCHAR(10),
    line3_street VARCHAR(60),
    line3_room VARCHAR(10),
    line4 VARCHAR(60),
    line5 VARCHAR(60),
    line6 VARCHAR(20),
    -- Part II
    line7 VARCHAR(80),
    line8 VARCHAR(10),
    line9_street VARCHAR(60),
    line9_room VARCHAR(10),
    line10 VARCHAR(3),
    line11 VARCHAR(60),
    line12 VARCHAR(10),
    line13 VARCHAR(80),
    line14 VARCHAR(9),
    line15 VARCHAR(60),
    line16 VARCHAR(20),
    line17a VARCHAR(10),
    line17b VARCHAR(20),
    line17c VARCHAR(5),
    -- Part III
    line18 VARCHAR(10),
    line19a VARCHAR(20),
    line19b VARCHAR(10),
    line19c VARCHAR(20),
    line21a VARCHAR(20),
    line21b VARCHAR(20),
    line21c_code VARCHAR(5),
    line21c_date VARCHAR(10),
    line22 NUMERIC(15,2),
    line23a VARCHAR(3),
    line23b VARCHAR(3),
    line24a VARCHAR(3),
    line24b VARCHAR(5),
    line25 VARCHAR(3),
    -- Direct Deposit
    routing_number VARCHAR(9),
    account_type VARCHAR(10),
    account_number VARCHAR(17),
    -- Signature
    sig_signature VARCHAR(100),
    sig_date VARCHAR(10),
    sig_name_title VARCHAR(80),
    -- Paid Preparer
    prep_name VARCHAR(60),
    prep_signature VARCHAR(60),
    prep_date VARCHAR(10),
    prep_self_employed BOOLEAN DEFAULT FALSE,
    prep_ptin VARCHAR(11),
    prep_firm_name VARCHAR(60),
    prep_firm_ein VARCHAR(10),
    prep_phone VARCHAR(20),
    prep_firm_address VARCHAR(80),
    -- Payment / Status
    stripe_payment_intent_id VARCHAR(50),
    payment_status VARCHAR(20),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    submitted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
