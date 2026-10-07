import { useEffect, useState } from 'react'
import { Award, Pencil, Plus, Trash2 } from 'lucide-react'
import { Link, useLocation } from 'react-router-dom'
import { apiFetch } from '../../services/api'

export default function AdminAchievements() {
  const location = useLocation()
  const [items, setItems] = useState(null)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState(() => location.state?.notice || '')
  const [deletingId, setDeletingId] = useState(null)
  const [refreshKey, setRefreshKey] = useState(0)

  useEffect(() => {
    let active = true
    apiFetch('/api/admin/achievements')
      .then((data) => {
        if (!active) return
        setItems(data)
        setError('')
      })
      .catch((requestError) => { if (active) setError(requestError.message) })
    return () => { active = false }
  }, [refreshKey])

  async function deleteAchievement(item) {
    if (!window.confirm(`Delete ${item.title}? This cannot be undone.`)) return
    setDeletingId(item.id)
    setError('')
    try {
      await apiFetch(`/api/admin/achievements/${item.id}`, { method: 'DELETE' })
      setRefreshKey((value) => value + 1)
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setDeletingId(null)
    }
  }

  return <section className="admin-page">
    <div className="admin-page-heading">
      <div><span className="eyebrow">Content</span><h1>Portfolio cards</h1><p>Manage certifications, business, community, and challenge cards shown throughout your public portfolio.</p></div>
      <Link className="admin-primary-button" to="/admin/achievements/new"><Plus size={17} />Add achievement</Link>
    </div>
    {notice && <p className="admin-success" role="status">{notice}<button type="button" aria-label="Dismiss message" onClick={() => setNotice('')}>×</button></p>}
    {error && <p className="admin-form-error" role="alert">{error}</p>}
    {items ? items.length ? <div className="admin-table-wrap">
      <table className="admin-table">
        <thead><tr><th>Portfolio card</th><th>Section</th><th>Date</th><th>Order</th><th><span className="sr-only">Actions</span></th></tr></thead>
        <tbody>{items.map((item) => <tr key={item.id}>
          <td><strong>{item.title}</strong><small>{item.issuer || 'No issuer'}</small></td>
          <td>{formatType(item.cardType)}</td>
          <td>{formatDate(item.achievedOn)}</td>
          <td>{item.sortOrder}</td>
          <td><div className="admin-row-actions">
            <Link to={`/admin/achievements/${item.id}/edit`} aria-label={`Edit ${item.title}`}><Pencil size={16} /></Link>
            <button type="button" onClick={() => deleteAchievement(item)} disabled={deletingId === item.id} aria-label={`Delete ${item.title}`}><Trash2 size={16} /></button>
          </div></td>
        </tr>)}</tbody>
      </table>
    </div> : <div className="admin-empty"><Award size={28} /><h2>No achievements added</h2><p>Add an award or accomplishment to highlight your progress.</p><Link to="/admin/achievements/new" className="admin-primary-button"><Plus size={17} />Add achievement</Link></div> : !error && <p role="status">Loading achievements…</p>}
  </section>
}

function formatDate(value) {
  if (!value) return '—'
  return new Intl.DateTimeFormat(undefined, { month: 'short', year: 'numeric', timeZone: 'UTC' })
    .format(new Date(`${value}T00:00:00Z`))
}

function formatType(value) {
  const labels = { CERTIFICATION: 'Certification', BUSINESS: 'Business', COMMUNITY: 'Community', CHALLENGE: 'Challenge', OTHER: 'Other' }
  return labels[value] || labels.CERTIFICATION
}
