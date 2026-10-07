import { useEffect, useState } from 'react'
import { Pencil, Plus, Sparkles, Trash2 } from 'lucide-react'
import { Link, useLocation } from 'react-router-dom'
import { apiFetch } from '../../services/api'

export default function AdminSkills() {
  const location = useLocation()
  const [items, setItems] = useState(null)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState(() => location.state?.notice || '')
  const [deletingId, setDeletingId] = useState(null)
  const [refreshKey, setRefreshKey] = useState(0)

  useEffect(() => {
    let active = true
    apiFetch('/api/admin/skills')
      .then((data) => {
        if (!active) return
        setItems(data)
        setError('')
      })
      .catch((requestError) => { if (active) setError(requestError.message) })
    return () => { active = false }
  }, [refreshKey])

  async function deleteSkill(item) {
    if (!window.confirm(`Delete ${item.name}? This cannot be undone.`)) return
    setDeletingId(item.id)
    setError('')
    try {
      await apiFetch(`/api/admin/skills/${item.id}`, { method: 'DELETE' })
      setRefreshKey((value) => value + 1)
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setDeletingId(null)
    }
  }

  return <section className="admin-page">
    <div className="admin-page-heading">
      <div><span className="eyebrow">Content</span><h1>Skills</h1><p>Manage the skills and proficiency shown on your public portfolio.</p></div>
      <Link className="admin-primary-button" to="/admin/skills/new"><Plus size={17} />Add skill</Link>
    </div>
    {notice && <p className="admin-success" role="status">{notice}<button type="button" aria-label="Dismiss message" onClick={() => setNotice('')}>×</button></p>}
    {error && <p className="admin-form-error" role="alert">{error}</p>}
    {items ? items.length ? <div className="admin-table-wrap">
      <table className="admin-table">
        <thead><tr><th>Skill</th><th>Category</th><th>Proficiency</th><th>Order</th><th><span className="sr-only">Actions</span></th></tr></thead>
        <tbody>{items.map((item) => <tr key={item.id}>
          <td><strong>{item.name}</strong></td>
          <td>{item.category || '—'}</td>
          <td>{item.proficiency == null ? '—' : `${item.proficiency}%`}</td>
          <td>{item.sortOrder}</td>
          <td><div className="admin-row-actions">
            <Link to={`/admin/skills/${item.id}/edit`} aria-label={`Edit ${item.name}`}><Pencil size={16} /></Link>
            <button type="button" onClick={() => deleteSkill(item)} disabled={deletingId === item.id} aria-label={`Delete ${item.name}`}><Trash2 size={16} /></button>
          </div></td>
        </tr>)}</tbody>
      </table>
    </div> : <div className="admin-empty"><Sparkles size={28} /><h2>No skills added</h2><p>Add a skill to show your capabilities and strengths.</p><Link to="/admin/skills/new" className="admin-primary-button"><Plus size={17} />Add skill</Link></div> : !error && <p role="status">Loading skills…</p>}
  </section>
}
