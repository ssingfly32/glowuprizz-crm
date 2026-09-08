CREATE TABLE operators (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP    NOT NULL
);

CREATE TABLE html_templates (
    id          BIGSERIAL PRIMARY KEY,
    operator_id BIGINT       NOT NULL REFERENCES operators (id),
    name        VARCHAR(255) NOT NULL,
    content     TEXT         NOT NULL,
    created_at  TIMESTAMP    NOT NULL
);

CREATE TABLE campaigns (
    id                BIGSERIAL PRIMARY KEY,
    operator_id       BIGINT       NOT NULL REFERENCES operators (id),
    html_template_id  BIGINT       NOT NULL REFERENCES html_templates (id),
    name              VARCHAR(255) NOT NULL,
    public_slug       VARCHAR(255) NOT NULL UNIQUE,
    published         BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP    NOT NULL
);

CREATE TABLE distribution_links (
    id          BIGSERIAL PRIMARY KEY,
    campaign_id BIGINT       NOT NULL REFERENCES campaigns (id),
    channel     VARCHAR(20)  NOT NULL,
    link_token  VARCHAR(255) NOT NULL UNIQUE,
    created_at  TIMESTAMP    NOT NULL
);
CREATE INDEX idx_distribution_links_campaign_id ON distribution_links (campaign_id);

CREATE TABLE visits (
    id                   BIGSERIAL PRIMARY KEY,
    campaign_id          BIGINT       NOT NULL REFERENCES campaigns (id),
    distribution_link_id BIGINT       REFERENCES distribution_links (id),
    channel              VARCHAR(20),
    visitor_token        VARCHAR(255) NOT NULL,
    occurred_at          TIMESTAMP    NOT NULL
);
CREATE INDEX idx_visits_campaign_id ON visits (campaign_id);
CREATE INDEX idx_visits_visitor_token ON visits (visitor_token);

CREATE TABLE submissions (
    id                   BIGSERIAL PRIMARY KEY,
    campaign_id          BIGINT       NOT NULL REFERENCES campaigns (id),
    distribution_link_id BIGINT       REFERENCES distribution_links (id),
    channel              VARCHAR(20),
    visitor_token        VARCHAR(255) NOT NULL,
    data                 JSONB        NOT NULL,
    submitted_at         TIMESTAMP    NOT NULL
);
CREATE INDEX idx_submissions_campaign_id ON submissions (campaign_id);
