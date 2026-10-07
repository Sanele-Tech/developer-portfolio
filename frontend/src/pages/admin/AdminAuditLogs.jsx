import { useEffect, useState } from 'react'
import { ChevronLeft, ChevronRight, History } from 'lucide-react'
import { apiFetch } from '../../services/api'

const PAGE_SIZE = 20

export default function AdminAuditLogs() {
  const [page, setPage] = useState(0)
  const [result, setResult] = useState(null)
  const [error, setError] = useState(null)

  useEffect(() => {
    let active = true
    apiFetch(`/api/admin/audit-logs?page=${page}&size=${PAGE_SIZE}`)
      .then((data) => {
        if (!active) return
        setResult(data)
        setError(null)
      })
      .catch((requestError) => {
        if (active) setError({ page, message: requestError.message })
      })
    return () => { active = false }
  }, [page])

  const visibleResult = result?.number === page ? result : null
  const visibleError = error?.page === page ? error.message : ''

  return <section className="admin-page">
    <div className="admin-page-heading">
      <div><span className="eyebrow">Administration</span><h1>Audit log</h1><p>Review recent content changes and who made them.</p></div>
    </div>
    {visibleError && <p className="admin-form-error" role="alert">{visibleError}</p>}
    {!visibleResult && !visibleError && <p role="status">Loading audit history…</p>}
    {visibleResult && visibleResult.content.length === 0 && <div className="admin-empty">
      <History size={28} /><h2>No activity recorded</h2><p>Content changes will appear here as they are made.</p>
    </div>}
    {visibleResult && visibleResult.content.length > 0 && <>
      <div className="admin-table-wrap">
        <table className="admin-table admin-audit-table">
          <thead><tr><th>When</th><th>Administrator</th><th>Action</th><th>Content</th><th>Change details</th></tr></thead>
          <tbody>{visibleResult.content.map((entry) => <tr key={entry.id}>
            <td>{formatDate(entry.createdAt)}</td>
            <td>{entry.actor || 'System'}</td>
            <td><span className={`admin-status ${entry.action === 'DELETE' ? 'is-destructive' : ''}`}>{entry.action}</span></td>
            <td>{entry.entityType || '—'}{entry.entityId != null && ` #${entry.entityId}`}</td>
            <td>{entry.details && <details className="admin-audit-details">
              <summary>View changes</summary>
              <pre>{JSON.stringify(entry.details, null, 2)}</pre>
            </details>}</td>
          </tr>)}</tbody>
        </table>
      </div>
      <div className="admin-pagination">
        <span>{visibleResult.totalElements} entr{visibleResult.totalElements === 1 ? 'y' : 'ies'}</span>
        <div>
          <button type="button" onClick={() => setPage((current) => Math.max(0, current - 1))} disabled={visibleResult.first}>
            <ChevronLeft size={14} />Previous
          </button>
          <span>Page {visibleResult.number + 1} of {Math.max(visibleResult.totalPages, 1)}</span>
          <button type="button" onClick={() => setPage((current) => current + 1)} disabled={visibleResult.last}>
            Next<ChevronRight size={14} />
          </button>
        </div>
      </div>
    </>}
  </section>
}

function formatDate(value) {
  if (!value) return '—'
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value))
}
