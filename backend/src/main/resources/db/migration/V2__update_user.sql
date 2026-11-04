ALTER TABLE tb_users
    ADD is_active BOOLEAN DEFAULT TRUE NOT NULL,
    ADD last_login TIMESTAMPTZ(6) DEFAULT NOW() NOT NULL,
    ADD updated_at TIMESTAMPTZ(6),
    ALTER COLUMN created_at TYPE TIMESTAMPTZ(6);

ALTER TABLE tb_refresh_tokens
    ALTER COLUMN expires_at TYPE timestamptz(6);