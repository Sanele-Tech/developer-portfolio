import { useEffect, useState } from 'react'
import { BriefcaseBusiness as Linkedin, Code2 as Github, Mail, Phone, Send } from 'lucide-react'
import PageHeading from '../../components/PageHeading'
import { useApi } from '../../hooks/useApi'
import { apiFetch, initializeCsrf, resolveExternalUrl } from '../../services/api'
import { recordAnalyticsEvent } from '../../services/analytics'

const initialForm = { name: '', email: '', company: '', subject: '', message: '', website: '' }

export default function Contact() {
  const { data: profile, error: profileError, retry: retryProfile } = useApi('/api/profile')
  const githubUrl = resolveExternalUrl(profile?.githubUrl)
  const linkedinUrl = resolveExternalUrl(profile?.linkedinUrl)
  const [form, setForm] = useState(initialForm)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)

  useEffect(() => {
    void recordAnalyticsEvent('contact_form_open')
  }, [])

  function updateField(event) {
    const { name, value } = event.target
    setForm((current) => ({ ...current, [name]: value }))
    setError('')
    setSuccess(false)
  }

  async function submit(event) {
    event.preventDefault()
    setSubmitting(true)
    setError('')
    setSuccess(false)
    try {
      await initializeCsrf()
      await apiFetch('/api/contact', {
        method: 'POST',
        body: JSON.stringify(form),
      })
      void recordAnalyticsEvent('contact_form_submit')
      setForm(initialForm)
      setSuccess(true)
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setSubmitting(false)
    }
  }

  return <section className="page-content contact-page">
    <PageHeading eyebrow="Get in touch" title="Let’s work together" />
    <p className="page-intro">Have a project opportunity, collaboration or question? Send a message and I’ll get back to you.</p>
    <div className="contact-layout">
      <form className="contact-form" onSubmit={submit}>
        <div className="contact-honeypot" aria-hidden="true">
          <label htmlFor="contact-website">Leave this field empty</label>
          <input id="contact-website" name="website" value={form.website} onChange={updateField} tabIndex="-1" autoComplete="off" />
        </div>
        <div className="form-row">
          <input name="name" aria-label="Name" placeholder="Name *" autoComplete="name" maxLength={120} value={form.name} onChange={updateField} required />
          <input name="email" type="email" aria-label="Email" placeholder="Email *" autoComplete="email" maxLength={320} value={form.email} onChange={updateField} required />
        </div>
        <input name="company" aria-label="Company" placeholder="Company (Optional)" maxLength={160} value={form.company} onChange={updateField} />
        <select name="subject" aria-label="Subject" value={form.subject} onChange={updateField}>
          <option value="">Subject (Optional)</option>
          <option>Project opportunity</option>
          <option>Job opportunity</option>
          <option>Collaboration</option>
          <option>Recruitment</option>
          <option>General enquiry</option>
        </select>
        <textarea name="message" aria-label="Message" placeholder="Message *" rows="7" maxLength={5000} value={form.message} onChange={updateField} required />
        {error && <p className="contact-feedback is-error" role="alert">{error}</p>}
        {success && <p className="contact-feedback is-success" role="status">Your message has been received. Thank you for getting in touch.</p>}
        <button className="button button-dark" type="submit" disabled={submitting}>
          {submitting ? 'Sending…' : 'Send Message'} <Send size={16} />
        </button>
      </form>
      <aside className="contact-details" aria-label="Direct contact details">
        <span className="eyebrow">Direct contact</span>
        {profile?.email && <a className="contact-detail" href={`mailto:${profile.email}`}>
          <Mail size={18} aria-hidden="true" />
          <span><small>Email</small><strong>{profile.email}</strong></span>
        </a>}
        {profile?.phoneNumber && <a className="contact-detail" href={`tel:${profile.phoneNumber.replace(/[^\d+]/g, '')}`}>
          <Phone size={18} aria-hidden="true" />
          <span><small>Phone</small><strong>{profile.phoneNumber}</strong></span>
        </a>}
        {(githubUrl || linkedinUrl) && <div className="contact-socials" aria-label="Social profiles">
          {githubUrl && <a className="contact-social-link" href={githubUrl} target="_blank" rel="noreferrer"
            aria-label="GitHub profile (opens in a new tab)" onClick={() => void recordAnalyticsEvent('social_link_click')}>
            <Github size={18} aria-hidden="true" /><span>GitHub</span>
          </a>}
          {linkedinUrl && <a className="contact-social-link" href={linkedinUrl} target="_blank" rel="noreferrer"
            aria-label="LinkedIn profile (opens in a new tab)" onClick={() => void recordAnalyticsEvent('social_link_click')}>
            <Linkedin size={18} aria-hidden="true" /><span>LinkedIn</span>
          </a>}
        </div>}
        {profileError && <p className="public-data-error" role="alert">Contact details could not be loaded. <button type="button" onClick={retryProfile}>Try again</button></p>}
      </aside>
    </div>
  </section>
}