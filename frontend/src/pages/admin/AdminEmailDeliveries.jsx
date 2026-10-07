import { useEffect, useState } from 'react'
import { ChevronLeft, ChevronRight, MailCheck } from 'lucide-react'
import { Link } from 'react-router-dom'
import { apiFetch } from '../../services/api'

const PAGE_SIZE = 20

export default function AdminEmailDeliveries() {
  const [page, setPage] = useState(0)
  const [result, setResult] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    apiFetch(`/api/admin/email-deliveries?page=${page}&size=${PAGE_SIZE}`)
      .then((data) => {
        if (!active) return
        setResult(data)
        setError('')
      })
      .catch((requestError) => { if (active) setError(requestError.message) })
    return () => { active = false }
  }, [page])

  const visibleResult = result?.number === page ? result : null
  return <section className="admin-page">
    <div className="admin-page-heading">
      <div><span className="eyebrow">Communication</span><h1>Email delivery log</h1><p>Review sent notifications and troubleshoot delivery failures.</p></div>
      <Link className="admin-secondary-button" to="/admin/email-templates">Email templates</Link>
    </div>
    {error && <p className="admin-form-error" role="alert">{error}</p>}
    {!visibleResult && !error && <p role="status">Loading delivery history…</p>}
    {visibleResult?.content.length === 0 && <div className="admin-empty"><MailCheck size={28} /><h2>No email attempts yet</h2><p>Notification and reply delivery attempts will be listed here.</p></div>}
    {visibleResult?.content.length > 0 && <>
      <div className="admin-table-wrap">
        <table className="admin-table">
          <thead><tr><th>When</th><th>Message</th><th>Template</th><th>Result</th><th>Delivery details</th></tr></thead>
          <tbody>{visibleResult.content.map((entry) => <tr key={entry.id}>
            <td>{formatDate(entry.createdAt)}</td>
            <td>{entry.contactMessageId ? <Link to="/admin/inbox">Enquiry #{entry.contactMessageId}</Link> : '—'}</td>
            <td>{entry.templateName.replaceAll('_', ' ')}</td>
            <td><span className={`admin-status ${entry.status === 'FAILED' ? 'is-destructive' : 'is-success'}`}>{entry.status}</span></td>
            <td>{entry.failureReason || 'Delivered'}</td>
          </tr>)}</tbody>
        </table>
      </div>
      <div className="admin-pagination"><span>{visibleResult.totalElements} attempt{visibleResult.totalElements === 1 ? '' : 's'}</span><div>
        <button type="button" onClick={() => setPage((current) => Math.max(0, current - 1))} disabled={visibleResult.first}><ChevronLeft size={14} />Previous</button>
        <span>Page {visibleResult.number + 1} of {Math.max(visibleResult.totalPages, 1)}</span>
        <button type="button" onClick={() => setPage((current) => current + 1)} disabled={visibleResult.last}>Next<ChevronRight size={14} /></button>
      </div></div>
    </>}
  </section>
}

function formatDate(value) {
  return value ? new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value)) : '—'
}
