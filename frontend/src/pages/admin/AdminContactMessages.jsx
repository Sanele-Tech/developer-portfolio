import { useEffect, useState } from 'react'
import { ChevronLeft, ChevronRight, MailOpen, Send } from 'lucide-react'
import { Link } from 'react-router-dom'
import { apiFetch } from '../../services/api'

const PAGE_SIZE = 20
const statuses = ['NEW', 'READ', 'REPLIED', 'ARCHIVED']
const replyTemplates = [
  ['RECRUITER_RESPONSE', 'Professional response'],
  ['APPLICATION_FOLLOW_UP', 'Application follow-up'],
  ['PROFESSIONAL_ENQUIRY', 'General professional enquiry'],
]

export default function AdminContactMessages() {
  const [page, setPage] = useState(0)
  const [result, setResult] = useState(null)
  const [selectedStatuses, setSelectedStatuses] = useState({})
  const [updatingId, setUpdatingId] = useState(null)
  const [error, setError] = useState(null)
  const [replies, setReplies] = useState({})
  const [replyDrafts, setReplyDrafts] = useState({})
  const [replyTemplate, setReplyTemplate] = useState({})
  const [replyErrors, setReplyErrors] = useState({})
  const [sendingReplyId, setSendingReplyId] = useState(null)

  useEffect(() => {
    let active = true
    apiFetch(`/api/admin/contact-messages?page=${page}&size=${PAGE_SIZE}`)
      .then((data) => {
        if (!active) return
        setResult(data)
        setSelectedStatuses(Object.fromEntries(data.content.map((message) => [message.id, message.status])))
        setError(null)
      })
      .catch((requestError) => {
        if (active) setError({ page, message: requestError.message })
      })
    return () => { active = false }
  }, [page])

  async function saveStatus(message) {
    const status = selectedStatuses[message.id]
    if (status === message.status) return
    setUpdatingId(message.id)
    setError(null)
    try {
      const updated = await apiFetch(`/api/admin/contact-messages/${message.id}/status`, {
        method: 'PATCH',
        body: JSON.stringify({ status }),
      })
      setResult((current) => current && ({
        ...current,
        content: current.content.map((item) => item.id === message.id ? updated : item),
      }))
    } catch (requestError) {
      setSelectedStatuses((current) => ({ ...current, [message.id]: message.status }))
      setError({ page, message: requestError.message })
    } finally {
      setUpdatingId(null)
    }
  }

  async function loadReplies(messageId) {
    if (replies[messageId]) return
    setReplyErrors((current) => ({ ...current, [messageId]: '' }))
    try {
      const data = await apiFetch(`/api/admin/contact-messages/${messageId}/replies`)
      setReplies((current) => ({ ...current, [messageId]: data }))
    } catch (requestError) {
      setReplyErrors((current) => ({ ...current, [messageId]: requestError.message }))
    }
  }

  async function sendReply(event, message) {
    event.preventDefault()
    const text = (replyDrafts[message.id] || '').trim()
    if (!text) return
    setSendingReplyId(message.id)
    setReplyErrors((current) => ({ ...current, [message.id]: '' }))
    try {
      const reply = await apiFetch(`/api/admin/contact-messages/${message.id}/replies`, {
        method: 'POST',
        body: JSON.stringify({
          message: text,
          templateName: replyTemplate[message.id] || 'RECRUITER_RESPONSE',
        }),
      })
      setReplies((current) => ({ ...current, [message.id]: [...(current[message.id] || []), reply] }))
      setReplyDrafts((current) => ({ ...current, [message.id]: '' }))
      if (reply.emailDelivered) {
        setResult((current) => current && ({
          ...current,
          content: current.content.map((item) => item.id === message.id ? { ...item, status: 'REPLIED' } : item),
        }))
        setSelectedStatuses((current) => ({ ...current, [message.id]: 'REPLIED' }))
      } else {
        setReplyErrors((current) => ({ ...current, [message.id]: 'Reply saved, but email delivery failed. Check the email delivery log before retrying.' }))
      }
    } catch (requestError) {
      setReplyErrors((current) => ({ ...current, [message.id]: requestError.message }))
    } finally {
      setSendingReplyId(null)
    }
  }

  const visibleResult = result?.number === page ? result : null
  const visibleError = error?.page === page ? error.message : ''

  return <section className="admin-page">
    <div className="admin-page-heading">
      <div><span className="eyebrow">Communication</span><h1>Inbox</h1><p>Review contact requests, send replies, and track delivery.</p></div>
      <Link className="admin-secondary-button" to="/admin/email-deliveries">Email delivery log</Link>
    </div>
    {visibleError && <p className="admin-form-error" role="alert">{visibleError}</p>}
    {!visibleResult && !visibleError && <p role="status">Loading messages…</p>}
    {visibleResult && visibleResult.content.length === 0 && <div className="admin-empty">
      <MailOpen size={28} /><h2>Your inbox is clear</h2><p>New contact form submissions will appear here.</p>
    </div>}
    {visibleResult && visibleResult.content.length > 0 && <>
      <div className="admin-table-wrap">
        <table className="admin-table admin-inbox-table">
          <thead><tr><th>Received</th><th>From</th><th>Subject and message</th><th>Status</th><th><span className="sr-only">Save status</span></th></tr></thead>
          <tbody>{visibleResult.content.map((message) => <tr key={message.id}>
            <td>{formatDate(message.createdAt)}</td>
            <td><strong>{message.name}</strong><small><a href={`mailto:${message.email}`}>{message.email}</a></small></td>
            <td>
              <strong>{message.subject || 'No subject'}</strong>
              {message.company && <small>{message.company}</small>}
              <details className="admin-message-details" onToggle={(event) => { if (event.currentTarget.open) void loadReplies(message.id) }}>
                <summary>Read message and reply</summary>
                <p>{message.message}</p>
                <div className="admin-reply-history" aria-label="Previous replies">
                  {(replies[message.id] || []).map((reply) => <article key={reply.id}>
                    <strong>{reply.author}</strong><time>{formatDate(reply.createdAt)}</time><p>{reply.message}</p>
                  </article>)}
                  {replies[message.id]?.length === 0 && <small>No replies sent from the inbox yet.</small>}
                </div>
                <form className="admin-reply-form" onSubmit={(event) => sendReply(event, message)}>
                  <label><span>Reply template</span><select value={replyTemplate[message.id] || 'RECRUITER_RESPONSE'} onChange={(event) => setReplyTemplate((current) => ({ ...current, [message.id]: event.target.value }))}>
                    {replyTemplates.map(([key, label]) => <option value={key} key={key}>{label}</option>)}
                  </select></label>
                  <label><span>Reply message</span><textarea value={replyDrafts[message.id] || ''} onChange={(event) => setReplyDrafts((current) => ({ ...current, [message.id]: event.target.value }))} maxLength={5000} rows={4} required placeholder="Write a professional response…" /></label>
                  {replyErrors[message.id] && <p className="admin-inline-error" role="alert">{replyErrors[message.id]}</p>}
                  <button className="admin-primary-button" type="submit" disabled={sendingReplyId === message.id}>{sendingReplyId === message.id ? 'Sending…' : 'Send reply'} <Send size={14} /></button>
                </form>
              </details>
            </td>
            <td><span className={`admin-status is-${message.status.toLowerCase()}`}>{formatStatus(message.status)}</span><select aria-label={`Status for message from ${message.name}`}
              value={selectedStatuses[message.id] || message.status}
              onChange={(event) => setSelectedStatuses((current) => ({ ...current, [message.id]: event.target.value }))}>
              {statuses.map((status) => <option key={status} value={status}>{formatStatus(status)}</option>)}
            </select></td>
            <td><button type="button" className="admin-small-save"
              disabled={updatingId === message.id || selectedStatuses[message.id] === message.status}
              onClick={() => saveStatus(message)}>{updatingId === message.id ? 'Saving…' : 'Save'}</button></td>
          </tr>)}</tbody>
        </table>
      </div>
      <div className="admin-pagination">
        <span>{visibleResult.totalElements} message{visibleResult.totalElements === 1 ? '' : 's'}</span>
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

function formatStatus(value) {
  return value.charAt(0) + value.slice(1).toLowerCase()
}
