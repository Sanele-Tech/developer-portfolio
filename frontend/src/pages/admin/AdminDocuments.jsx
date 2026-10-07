import { useEffect, useState } from 'react'
import { FileText, Pencil, Plus, Trash2 } from 'lucide-react'
import { Link, useLocation } from 'react-router-dom'
import { apiFetch } from '../../services/api'

export default function AdminDocuments() {
  const location = useLocation()
  const [items, setItems] = useState(null)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState(() => location.state?.notice || '')
  const [deletingId, setDeletingId] = useState(null)
  const [refreshKey, setRefreshKey] = useState(0)

  useEffect(() => {
    let active = true
    apiFetch('/api/admin/documents')
      .then((data) => {
        if (!active) return
        setItems(data)
        setError('')
      })
      .catch((requestError) => { if (active) setError(requestError.message) })
    return () => { active = false }
  }, [refreshKey])

  async function deleteDocument(item) {
    if (!window.confirm(`Delete ${item.name}? This cannot be undone.`)) return
    setDeletingId(item.id)
    setError('')
    try {
      await apiFetch(`/api/admin/documents/${item.id}`, { method: 'DELETE' })
      setRefreshKey((value) => value + 1)
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setDeletingId(null)
    }
  }

  return <section className="admin-page">
    <div className="admin-page-heading">
      <div><span className="eyebrow">Content</span><h1>Documents</h1><p>Manage links and metadata for your CV and portfolio documents.</p></div>
      <Link className="admin-primary-button" to="/admin/documents/new"><Plus size={17} />Add document</Link>
    </div>
    {notice && <p className="admin-success" role="status">{notice}<button type="button" aria-label="Dismiss message" onClick={() => setNotice('')}>×</button></p>}
    {error && <p className="admin-form-error" role="alert">{error}</p>}
    {items ? items.length ? <div className="admin-table-wrap">
      <table className="admin-table">
        <thead><tr><th>Document</th><th>Type</th><th>File</th><th>Added</th><th><span className="sr-only">Actions</span></th></tr></thead>
        <tbody>{items.map((item) => <tr key={item.id}>
          <td><strong>{item.name}</strong><small>{item.mimeType || 'File type not specified'}</small></td>
          <td>{item.documentType}</td>
          <td>{formatSize(item.sizeBytes)}</td>
          <td>{formatDate(item.createdAt)}</td>
          <td><div className="admin-row-actions">
            <a href={item.storageUrl} target="_blank" rel="noreferrer" aria-label={`Open ${item.name}`}><FileText size={16} /></a>
            <Link to={`/admin/documents/${item.id}/edit`} aria-label={`Edit ${item.name}`}><Pencil size={16} /></Link>
            <button type="button" onClick={() => deleteDocument(item)} disabled={deletingId === item.id} aria-label={`Delete ${item.name}`}><Trash2 size={16} /></button>
          </div></td>
        </tr>)}</tbody>
      </table>
    </div> : <div className="admin-empty"><FileText size={28} /><h2>No documents added</h2><p>Add a link to a hosted CV or other portfolio document.</p><Link to="/admin/documents/new" className="admin-primary-button"><Plus size={17} />Add document</Link></div> : !error && <p role="status">Loading documents…</p>}
  </section>
}

function formatDate(value) {
  if (!value) return '—'
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(new Date(value))
}

function formatSize(value) {
  if (value == null) return '—'
  if (value < 1024) return `${value} B`
  if (value < 1024 * 1024) return `${(value / 1024).toFixed(1)} KB`
  return `${(value / (1024 * 1024)).toFixed(1)} MB`
}
