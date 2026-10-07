package com.sanele.portfolio.email.entity;

import com.sanele.portfolio.common.entity.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "email_templates")
public class EmailTemplate extends AuditedEntity {
    @NotBlank @Size(max = 120) @Column(nullable = false, unique = true, length = 120) private String name;
    @NotBlank @Size(max = 200) @Pattern(regexp = "^[^\\r\\n]*$") @Column(nullable = false, length = 200) private String subject;
    @NotBlank @Size(max = 20000) @Column(nullable = false, columnDefinition = "TEXT") private String body;

    protected EmailTemplate() { }

    public EmailTemplate(String name, String subject, String body) {
        this.name = name;
        this.subject = subject;
        this.body = body;
    }

    public void update(String name, String subject, String body) {
        this.name = name;
        this.subject = subject;
        this.body = body;
    }

    public String getName() { return name; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
}