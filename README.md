# Sanele Portfolio Platform

A full-stack portfolio platform built with Spring Boot, React, PostgreSQL, and Docker.

## Local admin bootstrap

Phase 4 uses HTTP-only session cookies, BCrypt passwords, and CSRF protection. No admin password is stored in source control.

To create the first admin user on startup, set these environment variables before running the backend:

```powershell
$env:ADMIN_USERNAME = "admin"
$env:ADMIN_PASSWORD = "use-a-local-development-password"
$env:ADMIN_EMAIL = "admin@localhost"
$env:ADMIN_DISPLAY_NAME = "Portfolio Admin"
Set-Location backend
.\mvnw.cmd spring-boot:run
```

The seeder is idempotent and does nothing when the username already exists or the required variables are absent. Obtain the CSRF token from `GET /api/auth/csrf` before login or other state-changing requests; send it back in the `X-XSRF-TOKEN` header with the session cookie.

## Admin CMS

Open `http://localhost:5173/admin/login` after the backend and frontend are running. The admin session is checked through `GET /api/auth/me`; only users with the `ADMIN` or `EDITOR` role can access the CMS.

To open the development site from another device on the same network, start Vite with `npm run dev -- --host 0.0.0.0` from `frontend`. Start the backend with `PORTFOLIO_CORS_ALLOWED_ORIGINS` set to include the exact frontend origin for this computer's LAN IP (for example, `http://localhost:5173,http://127.0.0.1:5173,http://192.168.1.20:5173`). Then open `http://<LAN-IP>:5173` on the other device. Keep the CORS origin exact rather than allowing arbitrary network origins.

The Projects workspace is available at `/admin/projects`, with full-page create and edit forms. Its API is protected under `/api/admin/projects`; project writes also update the audit log.

The Experience workspace is available at `/admin/experience`, with full-page create and edit forms for roles and work dates. Its API is protected under `/api/admin/experience`; experience writes also update the audit log.

The Education workspace is available at `/admin/education`, with full-page create and edit forms for qualifications, institutions, and study dates. Its API is protected under `/api/admin/education`; education writes also update the audit log.

The Skills workspace is available at `/admin/skills`, with full-page create and edit forms for skill names, categories, proficiency, and display order. Its API is protected under `/api/admin/skills`; skill writes also update the audit log.

The Achievements workspace is available at `/admin/achievements`, with full-page create and edit forms for awards, issuers, descriptions, dates, and credential links. Its API is protected under `/api/admin/achievements`; achievement writes also update the audit log.

The Documents workspace is available at `/admin/documents`, with full-page create and edit forms for hosted document links and file metadata. Its API is protected under `/api/admin/documents`; document writes also update the audit log. Administrators can upload PDFs, project cover images, gallery images, and a profile avatar, or continue to use hosted HTTP or HTTPS URLs. Uploads are content-signature checked, limited to 10 MB, and stored under the configured media directory.

Set `PORTFOLIO_MEDIA_PUBLIC_BASE_URL` to the externally reachable backend base URL in deployed environments. Uploaded bytes are retained in PostgreSQL and served publicly from `/media/{generated-filename}`; database backups therefore include the stored media. See [ADR-006](docs/decisions/ADR-006-media-storage.md) for the storage decision.

The public CV link reads the latest document with type `CV` from `GET /api/documents/cv/current`. If no CV has been added, visitors are directed to the contact page to request it; the API returns only public document metadata and the hosted URL.

The Pages workspace is available at `/admin/pages`, with full-page create and edit forms for slugs, content, drafts, publication, and archival. Content changes create page versions. Its API is protected under `/api/admin/pages`; page writes also update the audit log. Published pages are discoverable at `/pages`, listed through `GET /api/pages`, and available individually at `/pages/{slug}` through `GET /api/pages/{slug}`. The public list returns summaries only; drafts, archived pages, and unknown slugs are not exposed.

The Settings workspace is available at `/admin/settings`, with profile identity, biography, contact links, and avatar editing, plus settings for public profile availability, education summary, focus areas, and location. The profile API is protected under `/api/admin/profile`; settings writes use `/api/admin/settings` and update the audit log. Arbitrary application settings and secrets are intentionally not exposed in this editor.

The Audit log is available to administrators at `/admin/audit-logs`, with paginated change history from `GET /api/admin/audit-logs`. Access is restricted to the `ADMIN` role because entries can include before-and-after content snapshots.

The contact inbox is available at `/admin/inbox` to `ADMIN` and `EDITOR` users. It lists contact form submissions through `GET /api/admin/contact-messages`, allows status updates (`NEW`, `READ`, `REPLIED`, `ARCHIVED`) through `PATCH /api/admin/contact-messages/{id}/status`, and records status changes in the audit log. Admins and editors can send templated replies and review reply history; successful replies are marked `REPLIED`.

## Phase 7 communication

The public contact form submits validated requests to `POST /api/contact`. It includes a honeypot anti-spam field and is rate-limited to five submissions per remote address per 15 minutes by default. Configure `PORTFOLIO_CONTACT_RATE_LIMIT_MAXIMUM` and `PORTFOLIO_CONTACT_RATE_LIMIT_WINDOW` to change those limits. The limiter is in-memory and applies per backend instance. A contact message is retained if notification email delivery fails.

Outbound email uses the configured SMTP relay. Set the relay host and sender before starting the backend; add authentication settings when required by the provider:

```powershell
$env:SPRING_MAIL_HOST = "smtp.example.com"
$env:SPRING_MAIL_PORT = "587"
$env:SPRING_MAIL_USERNAME = "your-smtp-username"
$env:SPRING_MAIL_PASSWORD = "your-smtp-password"
$env:SPRING_MAIL_SMTP_AUTH = "true"
$env:SPRING_MAIL_STARTTLS = "true"
$env:PORTFOLIO_MAIL_FROM = "portfolio@example.com"
$env:PORTFOLIO_NOTIFICATION_EMAIL = "owner@example.com"
```

On Windows, run `.\backend\run-with-gmail-smtp.ps1` from the repository root to start the backend with Gmail. On first run, it prompts for the Google account email associated with the App Password, the address that should receive portfolio enquiries, and the App Password with hidden input. It saves the settings encrypted with Windows DPAPI under the current Windows account's AppData folder; the password is not stored in the repository or as a plaintext file. Later runs reuse the saved settings without prompting. Run `.\backend\run-with-gmail-smtp.ps1 -Configure` to replace them. The sender address uses the Google account email, and the recipient can be the same or a different inbox.

Only one backend can listen on port 8080 at a time. If the launcher reports that the port is already in use, stop the backend already running in its terminal with Ctrl+C before starting this one; an already-running backend cannot inherit settings from a new launcher.

`PORTFOLIO_MAIL_FROM` falls back to `SPRING_MAIL_USERNAME`. SMTP credentials and addresses are environment-specific and must not be committed. Contact notifications and visitor confirmations use the seeded `CONTACT_NOTIFICATION` and `VISITOR_CONFIRMATION` templates. SMTP failures are recorded without returning provider error details to visitors. Administrators can manage templates at `/admin/email-templates` and review paginated outcomes at `/admin/email-deliveries`; the corresponding admin APIs are `/api/admin/email-templates` and `/api/admin/email-deliveries`. Template bodies support `{{name}}`, `{{email}}`, `{{company}}`, `{{subject}}`, and `{{message}}`.

The admin dashboard shows current content totals and new contact message counts from `GET /api/admin/overview`.

Use the environment variables above to bootstrap a local admin account. Never use those example credentials outside a local development environment.

## Phase 8 analytics

Visitors are asked to opt in before anonymous portfolio analytics are recorded; they can change or withdraw their choice using the Privacy choices control. The tracker records page and project views, project link clicks, CV link clicks, contact form opens/submissions, and social link clicks. Events use a random session-scoped identifier and route path only; contact form contents and visitor identity are not sent to analytics. Analytics events are automatically deleted after 180 days.

Administrators can review daily visits, most-viewed pages and projects, CV engagement, contact conversion, and event totals at `/admin/analytics`. The dashboard supports date ranges of up to 366 days. Its API is `GET /api/admin/analytics`; event collection uses `POST /api/analytics/events`. Flyway applies the analytics indexes automatically. CV download totals represent link clicks, not confirmation that the browser saved the file.
