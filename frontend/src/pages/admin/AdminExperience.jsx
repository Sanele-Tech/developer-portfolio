import { useEffect, useState } from 'react'
import { BriefcaseBusiness, Pencil, Plus, Trash2 } from 'lucide-react'
import { Link, useLocation } from 'react-router-dom'
import { apiFetch } from '../../services/api'

export default function AdminExperience() {
  const location = useLocation()
  const [items, setItems] = useState(null)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState(() => location.state?.notice || '')
  const [deletingId, setDeletingId] = useState(null)
  const [refreshKey, setRefreshKey] = useState(0)

  useEffect(() => {
    let active = true
    apiFetch('/api/admin/experience')
      .then((data) => {
        if (!active) return
        setItems(data)
        setError('')
      })
      .catch((requestError) => { if (active) setError(requestError.message) })
    return () => { active = false }
  }, [refreshKey])

  async function deleteExperience(item) {
    if (!window.confirm(`Delete the ${item.jobTitle} role at ${item.company}? This cannot be undone.`)) return
    setDeletingId(item.id)
    setError('')
    try {
      await apiFetch(`/api/admin/experience/${item.id}`, { method: 'DELETE' })
      setRefreshKey((value) => value + 1)
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setDeletingId(null)
    }
  }

  return <section className="admin-page">
    <div className="admin-page-heading">
      <div><span className="eyebrow">Content</span><h1>Experience</h1><p>Manage work history shown on your public portfolio.</p></div>
      <Link className="admin-primary-button" to="/admin/experience/new"><Plus size={17} />Add experience</Link>
    </div>
    {notice && <p className="admin-success" role="status">{notice}<button type="button" aria-label="Dismiss message" onClick={() => setNotice('')}>×</button></p>}
    {error && <p className="admin-form-error" role="alert">{error}</p>}
    {items ? items.length ? <div className="admin-table-wrap">
      <table className="admin-table">
        <thead><tr><th>Role</th><th>Dates</th><th>Order</th><th><span className="sr-only">Actions</span></th></tr></thead>
        <tbody>{items.map((item) => <tr key={item.id}>
          <td><strong>{item.jobTitle}</strong><small>{item.company}</small></td>
          <td>{formatDate(item.startDate)} – {item.currentRole ? 'Present' : formatDate(item.endDate)}</td>
          <td>{item.sortOrder}</td>
          <td><div className="admin-row-actions">
            <Link to={`/admin/experience/${item.id}/edit`} aria-label={`Edit ${item.jobTitle} at ${item.company}`}><Pencil size={16} /></Link>
            <button type="button" onClick={() => deleteExperience(item)} disabled={deletingId === item.id} aria-label={`Delete ${item.jobTitle} at ${item.company}`}><Trash2 size={16} /></button>
          </div></td>
        </tr>)}</tbody>
      </table>
    </div> : <div className="admin-empty"><BriefcaseBusiness size={28} /><h2>No experience added</h2><p>Add a role to show your professional journey.</p><Link to="/admin/experience/new" className="admin-primary-button"><Plus size={17} />Add experience</Link></div> : !error && <p role="status">Loading experience…</p>}
  </section>
}

function formatDate(value) {
  if (!value) return '—'
  return new Intl.DateTimeFormat(undefined, { month: 'short', year: 'numeric', timeZone: 'UTC' })
    .format(new Date(`${value}T00:00:00Z`))
}
