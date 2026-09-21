CREATE INDEX idx_lat_lon ON stores (latitude, longitude);
CREATE INDEX idx_region ON stores (region);
CREATE INDEX idx_risk_level ON stores (current_risk_level);
CREATE INDEX idx_store_sent_at ON notifications (store_id, sent_at DESC);