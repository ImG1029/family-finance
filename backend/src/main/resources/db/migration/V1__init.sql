CREATE SEQUENCE IF NOT EXISTS revinfo_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE revchanges
(
    rev        BIGINT NOT NULL,
    entityname VARCHAR(255)
);

CREATE TABLE revinfo
(
    rev      BIGINT NOT NULL,
    revtstmp BIGINT,
    CONSTRAINT pk_revinfo PRIMARY KEY (rev)
);

CREATE TABLE tb_refresh_tokens
(
    token      UUID    NOT NULL,
    user_id    UUID    NOT NULL,
    expires_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    revoked    BOOLEAN NOT NULL,
    CONSTRAINT pk_tb_refresh_tokens PRIMARY KEY (token)
);

CREATE TABLE tb_users
(
    id         UUID         NOT NULL,
    email      VARCHAR(255) NOT NULL,
    name       VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_tb_users PRIMARY KEY (id)
);

ALTER TABLE tb_users
    ADD CONSTRAINT uc_81f59ce6e4df568670ca73af3 UNIQUE (email);

ALTER TABLE tb_refresh_tokens
    ADD CONSTRAINT FK_TB_REFRESH_TOKENS_ON_USER FOREIGN KEY (user_id) REFERENCES tb_users (id);

ALTER TABLE revchanges
    ADD CONSTRAINT fk_revchanges_on_default_tracking_modified_entities_changelog FOREIGN KEY (rev) REFERENCES revinfo (rev);