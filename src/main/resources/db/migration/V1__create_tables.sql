CREATE TABLE IF NOT EXISTS stores (
                                      id BIGSERIAL PRIMARY KEY,
                                      name VARCHAR(200) NOT NULL,
    city VARCHAR(100) NOT NULL,
    latitude DOUBLE PRECISION NOT NULL,
    longitude DOUBLE PRECISION NOT NULL,
    region VARCHAR(50),
    manager_email VARCHAR(100) NOT NULL,
    manager_phone VARCHAR(20),
    current_risk_level INTEGER DEFAULT 0,
    last_risk_calculation TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS notifications (
                                             id BIGSERIAL PRIMARY KEY,
                                             store_id BIGINT NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
    risk_score INTEGER NOT NULL,
    sent_via VARCHAR(20) NOT NULL,
    recipient VARCHAR(200) NOT NULL,
    sent_at TIMESTAMP NOT NULL,
    message_content TEXT
    );