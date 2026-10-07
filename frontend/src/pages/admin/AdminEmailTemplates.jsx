import { useEffect, useState } from 'react'
import { Mail, Pencil, Plus, Trash2 } from 'lucide-react'
import { Link } from 'react-router-dom'
import { apiFetch } from '../../services/api'

const protectedTemplates = new Set([
  'CONTACT_NOTIFICATION',
  'VISITOR_CONFIRMATION',
  'RECRUITER_RESPONSE',
  'APPLICATION_FOLLOW_UP',
  'PROFESSIONAL_ENQUIRY',
])
const emptyTemplate = { name: '', subject: '', body: '' }

export default function AdminEmailTemplates() {
  const [templates, setTemplates] = useState(null)
  const [draft, setDraft] = useState(emptyTemplate)
  const [editingId, setEditingId] = useState(null)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    let active = true
    apiFetch('/api/admin/email-templates')
      .then((result) => { if (active) setTemplates(result) })
      .catch((requestError) => { if (active) setError(requestError.message) })
    return () => { active = false }
  }, [])

  function startEdit(template) {
    setEditingId(template.id)
    setDraft({ name: template.name, subject: template.subject, body: template.body })
    setError('')
    setNotice('')
  }

  function startCreate() {
    setEditingId(null)
    setDraft(emptyTemplate)
    setError('')
    setNotice('')
  }

  async function saveTemplate(event) {
    event.preventDefault()
    setSaving(true)
    setError('')
    setNotice('')
    try {
      const saved = await apiFetch(editingId ? `/api/admin/email-templates/${editingId}` : '/api/admin/email-templates', {
        method: editingId ? 'PUT' : 'POST',
        body: JSON.stringify(draft),
      })
      setTemplates((current) => editingId
        ? current.map((item) => item.id === saved.id ? saved : item)
        : [...current, saved].sort((left, right) => left.name.localeCompare(right.name)))
      setNotice(`Template ${editingId ? 'updated' : 'created'}.`)
      setDraft(emptyTemplate)
      setEditingId(null)
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setSaving(false)
    }
  }

  async function deleteTemplate(template) {
    if (!window.confirm(`Delete the "${template.name}" template?`)) return
    setError('')
    try {
      await apiFetch(`/api/admin/email-templates/${template.id}`, { method: 'DELETE' })
      setTemplates((current) => current.filter((item) => item.id !== template.id))
      if (editingId === template.id) startCreate()
      setNotice('Template deleted.')
    } catch (requestError) {
      setError(requestError.message)
    }
  }

  return <section className="admin-page">
    <div className="admin-page-heading">
      <div><span className="eyebrow">Communication</span><h1>Email templates</h1><p>Edit the messages used for enquiry notifications and replies.</p></div>
      <Link className="admin-secondary-button" to="/admin/email-deliveries"><Mail size={16} />Delivery log</Link>
    </div>
    {error && <p className="admin-form-error" role="alert">{error}</p>}
    {notice && <p className="admin-success" role="status">{notice}</p>}
    <div className="admin-template-layout">
      <section className="admin-panel admin-template-list" aria-label="Email template list">
        <div className="admin-panel-heading"><div><span className="admin-section-eyebrow">AVAILABLE</span><h2>Templates</h2></div><button type="button" className="admin-template-add" onClick={startCreate} aria-label="Create template"><Plus size={16} /></button></div>
        {!templates && !error && <p role="status">Loading templates…</p>}
        {templates?.length === 0 && <p className="admin-rank-empty">No templates available.</p>}
        {templates?.map((template) => <article key={template.id} className={`admin-template-item ${editingId === template.id ? 'is-selected' : ''}`}>
          <div><strong>{template.name.replaceAll('_', ' ')}</strong><small>{template.subject}</small></div>
          <div className="admin-template-actions">
            <button type="button" onClick={() => startEdit(template)} aria-label={`Edit ${template.name}`}><Pencil size={15} /></button>
            {!protectedTemplates.has(template.name) && <button type="button" onClick={() => deleteTemplate(template)} aria-label={`Delete ${template.name}`}><Trash2 size={15} /></button>}
          </div>
        </article>)}
      </section>
      <form className="admin-panel admin-template-editor" onSubmit={saveTemplate}>
        <div className="admin-panel-heading"><div><span className="admin-section-eyebrow">{editingId ? 'EDIT' : 'NEW'}</span><h2>{editingId ? 'Edit template' : 'Create template'}</h2></div></div>
        <label className="admin-field"><span>Template key</span><input value={draft.name} onChange={(event) => setDraft((current) => ({ ...current, name: event.target.value.toUpperCase().replace(/[^A-Z0-9_]/g, '_') }))} required maxLength={120} pattern="[A-Z][A-Z0-9_]*" disabled={Boolean(editingId && protectedTemplates.has(draft.name))} /><small>Uppercase letters, numbers, and underscores only.</small></label>
        <label className="admin-field"><span>Email subject</span><input value={draft.subject} onChange={(event) => setDraft((current) => ({ ...current, subject: event.target.value }))} required maxLength={200} /></label>
        <label className="admin-field"><span>Plain-text email body</span><textarea value={draft.body} onChange={(event) => setDraft((current) => ({ ...current, body: event.target.value }))} required maxLength={20000} rows={12} /></label>
        <p className="admin-template-variables">Available placeholders: <code>{'{{name}}'}</code>, <code>{'{{email}}'}</code>, <code>{'{{company}}'}</code>, <code>{'{{subject}}'}</code>, <code>{'{{message}}'}</code>.</p>
        <div className="admin-template-editor-actions">
          <button className="admin-primary-button" type="submit" disabled={saving}>{saving ? 'Saving…' : editingId ? 'Save template' : 'Create template'}</button>
          {editingId && <button className="admin-secondary-button" type="button" onClick={startCreate}>Cancel edit</button>}
        </div>
      </form>
    </div>
  </section>
}
