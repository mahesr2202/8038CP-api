-- V3: Schedule A (Form 8038-CP) — per-maturity interest limit rows

CREATE TABLE IF NOT EXISTS form8038cp.form_schedule_a (
    id                         BIGSERIAL PRIMARY KEY,
    submission_id              BIGINT NOT NULL REFERENCES form8038cp.form_submissions(id) ON DELETE CASCADE,
    row_order                  INT NOT NULL DEFAULT 0,
    col_a_maturity_date        VARCHAR(10),
    col_b_actual_interest      NUMERIC(15, 2),
    col_c_credit_rate_interest NUMERIC(15, 2)
);

CREATE INDEX idx_fsa_submission_id ON form8038cp.form_schedule_a(submission_id);
