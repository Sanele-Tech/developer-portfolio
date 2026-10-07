import { useEffect, useState } from 'react'
import { ArrowLeft, Eye, Pencil, Save } from 'lucide-react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { apiFetch } from '../../services/api'

const blankPage = {
  slug: '',
  title: '',
  excerpt: '',
  content: '',
  status: 'DRAFT',
}

export default function AdminPageForm() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEditing = Boolean(id)
  const [page, setPage] = useState(blankPage)
  const [loading, setLoading] = useState(isEditing)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [previewMode, setPreviewMode] = useState(false)

  useEffect(() => {
    if (!isEditing) return
    let active = true
    apiFetch(`/api/admin/pages/${id}`)
      .then((item) => { if (active) setPage({ ...blankPage, ...item, excerpt: item.excerpt || '' }) })
      .catch((requestError) => { if (active) setError(requestError.message) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [id, isEditing])

  function updateField(field, value) {
    setPage((current) => ({ ...current, [field]: value }))
  }

  async function submit(event) {
    event.preventDefault()
    setError('')
    setSaving(true)
    const payload = {
      ...page,
      slug: page.slug.trim(),
      title: page.title.trim(),
      excerpt: page.excerpt.trim() || null,
      content: page.content,
    }
    try {
      await apiFetch(isEditing ? `/api/admin/pages/${id}` : '/api/admin/pages', {
        method: isEditing ? 'PUT' : 'POST',
        body: JSON.stringify(payload),
      })
      navigate('/admin/pages', {
        replace: true,
        state: { notice: isEditing ? 'Page updated.' : 'Page created.' },
      })
    } catch (requestError) {
      const fieldErrors = requestError.details?.errors
      setError(Array.isArray(fieldErrors) && fieldErrors.length
        ? fieldErrors.map((item) => `${item.field}: ${item.message}`).join(' ')
        : requestError.message)
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <section className="admin-page"><p role="status">Loading page editor…</p></section>

  return <section className="admin-page">
    <div className="admin-page-heading admin-form-heading">
      <div><Link to="/admin/pages" className="admin-back-link"><ArrowLeft size={16} />Pages</Link><span className="eyebrow">Portfolio content</span><h1>{isEditing ? 'Edit page' : 'Add page'}</h1><p>Page content is versioned whenever the body changes.</p></div>
    </div>
    {error && <p className="admin-form-error" role="alert">{error}</p>}
    <form className="admin-editor-form" onSubmit={submit}>
      <section className="admin-form-section">
        <div className="admin-form-section-title admin-page-editor-title">
          <div><h2>Page details</h2><p>Choose a unique page slug and whether the page is a draft, published, or archived.</p></div>
          <button className="admin-secondary-button" type="button" aria-pressed={previewMode}
            onClick={() => setPreviewMode((current) => !current)}>
            {previewMode ? <Pencil size={15} /> : <Eye size={15} />}
            {previewMode ? 'Edit content' : 'Preview'}
          </button>
        </div>
        {previewMode ? <div className="admin-page-preview" aria-label="Unsaved page preview">
          <div className="admin-page-preview-meta"><span>/{page.slug || 'your-page-slug'}</span><span className={`admin-status ${page.status === 'PUBLISHED' ? 'is-featured' : ''}`}>{formatStatus(page.status)}</span></div>
          <span className="eyebrow">Page preview</span>
          <h2>{page.title || 'Untitled page'}</h2>
          {page.excerpt && <p className="admin-page-preview-excerpt">{page.excerpt}</p>}
          <div className="admin-page-preview-body">{page.content || 'Page content will appear here.'}</div>
        </div> : <div className="admin-form-grid">
          <label className="admin-field">Title<input required maxLength="200" value={page.title} onChange={(event) => updateField('title', event.target.value)} /></label>
          <label className="admin-field">URL slug<input required maxLength="180" pattern="[a-z0-9]+(?:-[a-z0-9]+)*" value={page.slug} onChange={(event) => updateField('slug', event.target.value)} placeholder="about-me" /><small>Use lowercase letters, numbers, and hyphens.</small></label>
          <label className="admin-field">Status<select value={page.status} onChange={(event) => updateField('status', event.target.value)}><option value="DRAFT">Draft</option><option value="PUBLISHED">Published</option><option value="ARCHIVED">Archived</option></select></label>
          <label className="admin-field admin-field-full">Excerpt <span className="admin-optional-label">(optional)</span><textarea rows="3" value={page.excerpt} onChange={(event) => updateField('excerpt', event.target.value)} /></label>
          <label className="admin-field admin-field-full">Page content<textarea required rows="14" value={page.content} onChange={(event) => updateField('content', event.target.value)} /></label>
        </div>}
        {previewMode && <p className="admin-preview-note" role="status">Preview only — this content has not been saved or published.</p>}
      </section>
      <div className="admin-form-actions"><Link to="/admin/pages" className="admin-cancel-button">Cancel</Link><button className="admin-primary-button" type="submit" disabled={saving || previewMode}><Save size={17} />{saving ? 'Saving…' : previewMode ? 'Return to edit to save' : 'Save page'}</button></div>
    </form>
  </section>
}

function formatStatus(value) {
  return value.charAt(0) + value.slice(1).toLowerCase()
}
