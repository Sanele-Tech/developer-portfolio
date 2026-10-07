CREATE INDEX idx_analytics_events_occurred_name ON analytics_events (occurred_at, event_name);
CREATE INDEX idx_analytics_events_session_name ON analytics_events (session_id, event_name);
CREATE INDEX idx_analytics_sessions_started_at ON analytics_sessions (started_at);
