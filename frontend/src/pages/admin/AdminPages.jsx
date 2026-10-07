import { useEffect, useState } from 'react'
import { FilePlus2, Pencil, Plus, Trash2 } from 'lucide-react'
import { Link, useLocation } from 'react-router-dom'
import { apiFetch } from '../../services/api'

export default function AdminPages() {
  const location = useLocation()
  const [items, setItems] = useState(null)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState(() => location.state?.notice || '')
  const [deletingId, setDeletingId] = useState(null)
  const [refreshKey, setRefreshKey] = useState(0)

  useEffect(() => {
    let active = true
    apiFetch('/api/admin/pages')
      .then((data) => {
        if (!active) return
        setItems(data)
        setError('')
      })
      .catch((requestError) => { if (active) setError(requestError.message) })
    return () => { active = false }
  }, [refreshKey])

  async function deletePage(item) {
    if (!window.confirm(`Delete the "${item.title}" page? Its saved content versions will also be removed.`)) return
    setDeletingId(item.id)
    setError('')
    try {
      await apiFetch(`/api/admin/pages/${item.id}`, { method: 'DELETE' })
      setRefreshKey((value) => value + 1)
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setDeletingId(null)
    }
  }

  return <section className="admin-page">
    <div className="admin-page-heading">
      <div><span className="eyebrow">Content</span><h1>Pages</h1><p>Manage standalone page content, publication status, and revision history.</p></div>
      <Link className="admin-primary-button" to="/admin/pages/new"><Plus size={17} />Add page</Link>
    </div>
    {notice && <p className="admin-success" role="status">{notice}<button type="button" aria-label="Dismiss message" onClick={() => setNotice('')}>×</button></p>}
    {error && <p className="admin-form-error" role="alert">{error}</p>}
    {items ? items.length ? <div className="admin-table-wrap">
      <table className="admin-table">
        <thead><tr><th>Page</th><th>Slug</th><th>Status</th><th>Updated</th><th><span className="sr-only">Actions</span></th></tr></thead>
        <tbody>{items.map((item) => <tr key={item.id}>
          <td><strong>{item.title}</strong></td>
          <td>/{item.slug}</td>
          <td><span className={`admin-status ${item.status === 'PUBLISHED' ? 'is-featured' : ''}`}>{formatStatus(item.status)}</span></td>
          <td>{formatDate(item.updatedAt)}</td>
          <td><div className="admin-row-actions">
            <Link to={`/admin/pages/${item.id}/edit`} aria-label={`Edit ${item.title}`}><Pencil size={16} /></Link>
            <button type="button" onClick={() => deletePage(item)} disabled={deletingId === item.id} aria-label={`Delete ${item.title}`}><Trash2 size={16} /></button>
          </div></td>
        </tr>)}</tbody>
      </table>
    </div> : <div className="admin-empty"><FilePlus2 size={28} /><h2>No pages added</h2><p>Create a page and save it as a draft or publish it.</p><Link to="/admin/pages/new" className="admin-primary-button"><Plus size={17} />Add page</Link></div> : !error && <p role="status">Loading pages…</p>}
  </section>
}

function formatStatus(status) {
  return status.charAt(0) + status.slice(1).toLowerCase()
}

function formatDate(value) {
  if (!value) return '—'
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(new Date(value))
}
