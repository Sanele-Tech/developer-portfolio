-- Communication workflow schema follows the previously applied analytics migration.
ALTER TABLE contact_messages ADD COLUMN company VARCHAR(160);

CREATE TABLE contact_message_replies (
    id BIGSERIAL PRIMARY KEY,
    contact_message_id BIGINT NOT NULL REFERENCES contact_messages (id) ON DELETE CASCADE,
    author_id BIGINT REFERENCES users (id) ON DELETE SET NULL,
    body TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_contact_message_replies_message_time
    ON contact_message_replies (contact_message_id, created_at);

CREATE TABLE email_delivery_logs (
    id BIGSERIAL PRIMARY KEY,
    contact_message_id BIGINT REFERENCES contact_messages (id) ON DELETE SET NULL,
    template_name VARCHAR(120) NOT NULL,
    recipient VARCHAR(320),
    status VARCHAR(20) NOT NULL,
    failure_reason VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_email_delivery_logs_created_at ON email_delivery_logs (created_at DESC);

INSERT INTO email_templates (name, subject, body) VALUES
    ('CONTACT_NOTIFICATION', 'New portfolio enquiry: {{subject}}', $$A new portfolio enquiry was received.

Name: {{name}}
Email: {{email}}
Company: {{company}}
Subject: {{subject}}

Message:
{{message}}$$),
    ('VISITOR_CONFIRMATION', 'Thanks for getting in touch', $$Hi {{name}},

Thank you for your message. I have received your enquiry and will get back to you as soon as I can.

Regards,
Sanele Dlomo$$),
    ('RECRUITER_RESPONSE', 'Re: {{subject}}', $$Hi {{name}},

{{message}}

Regards,
Sanele Dlomo$$),
    ('APPLICATION_FOLLOW_UP', 'Following up on {{subject}}', $$Hi {{name}},

I am following up regarding {{subject}}.

{{message}}

Regards,
Sanele Dlomo$$),
    ('PROFESSIONAL_ENQUIRY', 'Re: {{subject}}', $$Hi {{name}},

Thank you for your professional enquiry.

{{message}}

Regards,
Sanele Dlomo$$)
ON CONFLICT (name) DO NOTHING;
