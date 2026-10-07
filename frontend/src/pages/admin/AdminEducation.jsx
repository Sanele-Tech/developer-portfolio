import { useEffect, useState } from 'react'
import { GraduationCap, Pencil, Plus, Trash2 } from 'lucide-react'
import { Link, useLocation } from 'react-router-dom'
import { apiFetch } from '../../services/api'

export default function AdminEducation() {
  const location = useLocation()
  const [items, setItems] = useState(null)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState(() => location.state?.notice || '')
  const [deletingId, setDeletingId] = useState(null)
  const [refreshKey, setRefreshKey] = useState(0)

  useEffect(() => {
    let active = true
    apiFetch('/api/admin/education')
      .then((data) => {
        if (!active) return
        setItems(data)
        setError('')
      })
      .catch((requestError) => { if (active) setError(requestError.message) })
    return () => { active = false }
  }, [refreshKey])

  async function deleteEducation(item) {
    if (!window.confirm(`Delete ${item.qualification} at ${item.institution}? This cannot be undone.`)) return
    setDeletingId(item.id)
    setError('')
    try {
      await apiFetch(`/api/admin/education/${item.id}`, { method: 'DELETE' })
      setRefreshKey((value) => value + 1)
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setDeletingId(null)
    }
  }

  return <section className="admin-page">
    <div className="admin-page-heading">
      <div><span className="eyebrow">Content</span><h1>Education</h1><p>Manage the qualifications shown on your public portfolio.</p></div>
      <Link className="admin-primary-button" to="/admin/education/new"><Plus size={17} />Add education</Link>
    </div>
    {notice && <p className="admin-success" role="status">{notice}<button type="button" aria-label="Dismiss message" onClick={() => setNotice('')}>×</button></p>}
    {error && <p className="admin-form-error" role="alert">{error}</p>}
    {items ? items.length ? <div className="admin-table-wrap">
      <table className="admin-table">
        <thead><tr><th>Course / qualification</th><th>Institution</th><th>Dates</th><th>Order</th><th><span className="sr-only">Actions</span></th></tr></thead>
        <tbody>{items.map((item) => <tr key={item.id}>
          <td><strong>{item.qualification}</strong><small>{item.coreModules?.length || 0} core modules · {item.imageUrls?.length || 0} images</small></td>
          <td>{item.institution}</td>
          <td>{formatDate(item.startDate)} – {formatDate(item.endDate)}</td>
          <td>{item.sortOrder}</td>
          <td><div className="admin-row-actions">
            <Link to={`/admin/education/${item.id}/edit`} aria-label={`Edit ${item.qualification} at ${item.institution}`}><Pencil size={16} /></Link>
            <button type="button" onClick={() => deleteEducation(item)} disabled={deletingId === item.id} aria-label={`Delete ${item.qualification} at ${item.institution}`}><Trash2 size={16} /></button>
          </div></td>
        </tr>)}</tbody>
      </table>
    </div> : <div className="admin-empty"><GraduationCap size={28} /><h2>No education added</h2><p>Add a qualification to show your educational background.</p><Link to="/admin/education/new" className="admin-primary-button"><Plus size={17} />Add education</Link></div> : !error && <p role="status">Loading education…</p>}
  </section>
}

function formatDate(value) {
  if (!value) return '—'
  return new Intl.DateTimeFormat(undefined, { month: 'short', year: 'numeric', timeZone: 'UTC' })
    .format(new Date(`${value}T00:00:00Z`))
}
