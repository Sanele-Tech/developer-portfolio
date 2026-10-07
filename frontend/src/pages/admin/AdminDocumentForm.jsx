import { useEffect, useState } from 'react'
import { ArrowLeft, Save } from 'lucide-react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { apiFetch } from '../../services/api'

const blankDocument = {
  name: '',
  documentType: 'CV',
  storageUrl: '',
  mimeType: '',
  sizeBytes: '',
}

export default function AdminDocumentForm() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEditing = Boolean(id)
  const [document, setDocument] = useState(blankDocument)
  const [loading, setLoading] = useState(isEditing)
  const [saving, setSaving] = useState(false)
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!isEditing) return
    let active = true
    apiFetch(`/api/admin/documents/${id}`)
      .then((item) => {
        if (active) setDocument({
          ...blankDocument,
          ...item,
          mimeType: item.mimeType || '',
          sizeBytes: item.sizeBytes ?? '',
        })
      })
      .catch((requestError) => { if (active) setError(requestError.message) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [id, isEditing])

  function updateField(field, value) {
    setDocument((current) => ({ ...current, [field]: value }))
  }

  async function uploadDocument(file) {
    if (!file) return
    setError('')
    setUploading(true)
    const formData = new FormData()
    formData.append('file', file)
    try {
      const uploaded = await apiFetch('/api/admin/media?purpose=DOCUMENT', {
        method: 'POST',
        body: formData,
      })
      setDocument((current) => ({
        ...current,
        storageUrl: uploaded.url,
        mimeType: uploaded.mimeType,
        sizeBytes: uploaded.sizeBytes,
      }))
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setUploading(false)
    }
  }

  async function submit(event) {
    event.preventDefault()
    setError('')
    setSaving(true)
    const payload = {
      ...document,
      name: document.name.trim(),
      documentType: document.documentType.trim(),
      storageUrl: document.storageUrl.trim(),
      mimeType: document.mimeType.trim() || null,
      sizeBytes: document.sizeBytes === '' ? null : Number(document.sizeBytes),
    }
    try {
      await apiFetch(isEditing ? `/api/admin/documents/${id}` : '/api/admin/documents', {
        method: isEditing ? 'PUT' : 'POST',
        body: JSON.stringify(payload),
      })
      navigate('/admin/documents', {
        replace: true,
        state: { notice: isEditing ? 'Document updated.' : 'Document added.' },
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

  if (loading) return <section className="admin-page"><p role="status">Loading document editor…</p></section>

  return <section className="admin-page">
    <div className="admin-page-heading admin-form-heading">
      <div><Link to="/admin/documents" className="admin-back-link"><ArrowLeft size={16} />Documents</Link><span className="eyebrow">Portfolio content</span><h1>{isEditing ? 'Edit document' : 'Add document'}</h1><p>Upload a PDF or keep using a trusted hosted document link.</p></div>
    </div>
    {error && <p className="admin-form-error" role="alert">{error}</p>}
    <form className="admin-editor-form" onSubmit={submit}>
      <section className="admin-form-section">
        <div className="admin-form-section-title"><h2>Document details</h2><p>Upload a PDF or enter an existing secure document URL.</p></div>
        <div className="admin-form-grid">
          <label className="admin-field">Document name<input required maxLength="255" value={document.name} onChange={(event) => updateField('name', event.target.value)} placeholder="Sanele Dlomo CV" /></label>
          <label className="admin-field">Document type<input required maxLength="80" value={document.documentType} onChange={(event) => updateField('documentType', event.target.value)} placeholder="CV" /></label>
          <label className="admin-field admin-field-full">Upload PDF<input type="file" accept="application/pdf,.pdf" disabled={uploading} onChange={(event) => { const file = event.target.files?.[0]; event.target.value = ''; uploadDocument(file) }} /><small>{uploading ? 'Uploading…' : 'Maximum file size: 10 MB. Uploading replaces the current document URL.'}</small></label>
          <label className="admin-field admin-field-full">Hosted document URL<input required type="url" maxLength="500" value={document.storageUrl} onChange={(event) => updateField('storageUrl', event.target.value)} placeholder="https://example.com/sanele-cv.pdf" /></label>
          <label className="admin-field">MIME type <span className="admin-optional-label">(optional)</span><input maxLength="120" value={document.mimeType} onChange={(event) => updateField('mimeType', event.target.value)} placeholder="application/pdf" /></label>
          <label className="admin-field">File size in bytes <span className="admin-optional-label">(optional)</span><input type="number" min="0" step="1" value={document.sizeBytes} onChange={(event) => updateField('sizeBytes', event.target.value)} /></label>
        </div>
      </section>
      <div className="admin-form-actions"><Link to="/admin/documents" className="admin-cancel-button">Cancel</Link><button className="admin-primary-button" type="submit" disabled={saving || uploading}><Save size={17} />{saving ? 'Saving…' : 'Save document'}</button></div>
    </form>
  </section>
}
