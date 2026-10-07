import { useCallback, useEffect, useState } from 'react'
import { ArrowLeft, ArrowRight, FolderKanban, Pencil, Plus, Trash2 } from 'lucide-react'
import { Link, useLocation } from 'react-router-dom'
import { apiFetch } from '../../services/api'

export default function AdminProjects() {
  const location = useLocation()
  const [result, setResult] = useState(null)
  const [page, setPage] = useState(0)
  const [error, setError] = useState('')
  const [deletingId, setDeletingId] = useState(null)
  const [notice, setNotice] = useState(() => location.state?.notice || '')

  const loadProjects = useCallback(async () => {
    try {
      setResult(await apiFetch(`/api/admin/projects?page=${page}&size=20`))
      setError('')
    } catch (requestError) {
      setError(requestError.message)
    }
  }, [page])

  useEffect(() => {
    let active = true
    apiFetch(`/api/admin/projects?page=${page}&size=20`)
      .then((data) => {
        if (!active) return
        setResult(data)
        setError('')
      })
      .catch((requestError) => {
        if (active) setError(requestError.message)
      })
    return () => { active = false }
  }, [page])

  async function deleteProject(project) {
    if (!window.confirm(`Delete "${project.title}"? This cannot be undone.`)) return
    setDeletingId(project.id)
    setError('')
    try {
      await apiFetch(`/api/admin/projects/${project.id}`, { method: 'DELETE' })
      if (result.content.length === 1 && page > 0) setPage(page - 1)
      else await loadProjects()
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setDeletingId(null)
    }
  }

  return <section className="admin-page">
    <div className="admin-page-heading">
      <div><span className="eyebrow">Content</span><h1>Projects</h1><p>Manage the projects displayed on your public portfolio.</p></div>
      <Link className="admin-primary-button" to="/admin/projects/new"><Plus size={17} />New project</Link>
    </div>
    {notice && <p className="admin-success" role="status">{notice}<button type="button" aria-label="Dismiss message" onClick={() => setNotice('')}>×</button></p>}
    {error && <p className="admin-form-error" role="alert">{error}</p>}
    {result ? result.content.length ? <>
      <div className="admin-table-wrap">
        <table className="admin-table">
          <thead><tr><th>Project</th><th>Technologies</th><th>Visibility</th><th>Order</th><th><span className="sr-only">Actions</span></th></tr></thead>
          <tbody>{result.content.map((project) => <tr key={project.id}>
            <td><strong>{project.title}</strong><small>/{project.slug}</small></td>
            <td className="admin-tech-cell">{project.technologies?.length ? project.technologies.join(', ') : '—'}</td>
            <td><span className={`admin-status ${project.featured ? 'is-featured' : ''}`}>{project.featured ? 'Featured' : 'Published'}</span></td>
            <td>{project.sortOrder}</td>
            <td><div className="admin-row-actions">
              <Link to={`/admin/projects/${project.id}/edit`} aria-label={`Edit ${project.title}`}><Pencil size={16} /></Link>
              <button type="button" onClick={() => deleteProject(project)} disabled={deletingId === project.id} aria-label={`Delete ${project.title}`}><Trash2 size={16} /></button>
            </div></td>
          </tr>)}</tbody>
        </table>
      </div>
      <div className="admin-pagination"><span>{result.totalElements} project{result.totalElements === 1 ? '' : 's'}</span><div>
        <button type="button" disabled={result.first} onClick={() => setPage((value) => value - 1)}><ArrowLeft size={16} />Previous</button>
        <span>Page {result.number + 1} of {Math.max(result.totalPages, 1)}</span>
        <button type="button" disabled={result.last} onClick={() => setPage((value) => value + 1)}>Next<ArrowRight size={16} /></button>
      </div></div>
    </> : <div className="admin-empty"><FolderKanban size={28} /><h2>No projects yet</h2><p>Create your first project to start filling your portfolio.</p><Link to="/admin/projects/new" className="admin-primary-button"><Plus size={17} />Create project</Link></div> : !error && <p role="status">Loading projects…</p>}
  </section>
}
