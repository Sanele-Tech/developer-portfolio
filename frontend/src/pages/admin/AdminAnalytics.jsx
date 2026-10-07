import { useEffect, useMemo, useState } from 'react'
import { Activity, Download, Eye, FileText, Mail, RotateCw, Users } from 'lucide-react'
import { apiFetch } from '../../services/api'

const today = new Date()
const localDate = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}
const defaultTo = localDate(today)
const defaultFromDate = new Date(today)
defaultFromDate.setDate(defaultFromDate.getDate() - 29)

const metrics = [
  ['visits', 'Unique visits', Users],
  ['pageViews', 'Page views', Eye],
  ['resumeViews', 'CV views', FileText],
  ['resumeDownloads', 'CV downloads', Download],
  ['contactFormOpens', 'Contact opens', Mail],
  ['contactFormSubmissions', 'Messages sent', Activity],
]

export default function AdminAnalytics() {
  const [from, setFrom] = useState(localDate(defaultFromDate))
  const [to, setTo] = useState(defaultTo)
  const [data, setData] = useState(null)
  const [status, setStatus] = useState('loading')
  const [error, setError] = useState('')
  const [retry, setRetry] = useState(0)
  const [loadedRequestKey, setLoadedRequestKey] = useState('')
  const requestKey = `${from}:${to}:${retry}`

  useEffect(() => {
    let active = true
    apiFetch(`/api/admin/analytics?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`)
      .then((result) => {
        if (active) {
          setData(result)
          setLoadedRequestKey(requestKey)
          setStatus('ready')
        }
      })
      .catch((requestError) => {
        if (active) {
          setError(requestError.message)
          setLoadedRequestKey(requestKey)
          setStatus('error')
        }
      })
    return () => { active = false }
  }, [from, to, retry, requestKey])
  const currentStatus = loadedRequestKey === requestKey ? status : 'loading'

  const maxVisits = useMemo(
    () => Math.max(1, ...(data?.visitsOverTime || []).map((item) => item.visits)),
    [data],
  )

  return <section className="admin-analytics">
    <div className="admin-page-heading">
      <div><span className="eyebrow">Audience insights</span><h1>Portfolio analytics</h1>
        <p>Anonymous engagement signals collected only from visitors who opt in.</p></div>
      <div className="analytics-date-filters">
        <label>From<input type="date" value={from} max={to} onChange={(event) => setFrom(event.target.value)} /></label>
        <label>To<input type="date" value={to} min={from} max={defaultTo} onChange={(event) => setTo(event.target.value)} /></label>
      </div>
    </div>

    {currentStatus === 'loading' && <p className="admin-analytics-message" role="status">Loading analytics…</p>}
    {currentStatus === 'error' && <div className="admin-analytics-message is-error" role="alert">
      <p>{error}</p><button type="button" className="admin-secondary-button" onClick={() => setRetry((value) => value + 1)}><RotateCw size={14} /> Try again</button>
    </div>}
    {currentStatus === 'ready' && data && <>
      <div className="analytics-metrics">
        {metrics.map(([key, label, Icon]) => <article className="analytics-metric-card" key={key}>
          <span className="analytics-metric-icon"><Icon size={18} /></span>
          <span>{label}</span><strong>{data[key].toLocaleString()}</strong>
        </article>)}
      </div>

      <div className="analytics-grid">
        <section className="admin-panel analytics-visits-panel">
          <header className="admin-panel-heading"><div><span className="admin-section-eyebrow">Traffic</span><h2>Visits over time</h2></div><strong>{data.visits.toLocaleString()} visits</strong></header>
          {data.visitsOverTime.length
            ? <div className="analytics-chart-wrap">
              <svg className="analytics-chart" viewBox="0 0 720 210" role="img" aria-label="Daily unique visits line chart">
                <line x1="34" y1="174" x2="704" y2="174" />
                <line x1="34" y1="104" x2="704" y2="104" />
                <line x1="34" y1="34" x2="704" y2="34" />
                <polyline points={data.visitsOverTime.map((item, index, rows) => {
                  const x = rows.length === 1 ? 369 : 38 + (index / (rows.length - 1)) * 660
                  const y = 174 - (item.visits / maxVisits) * 140
                  return `${x},${y}`
                }).join(' ')} />
                {data.visitsOverTime.map((item, index, rows) => {
                  const x = rows.length === 1 ? 369 : 38 + (index / (rows.length - 1)) * 660
                  const y = 174 - (item.visits / maxVisits) * 140
                  return <circle key={item.date} cx={x} cy={y} r="3"><title>{item.date}: {item.visits} visits</title></circle>
                })}
              </svg>
              <div className="analytics-chart-labels"><span>{data.visitsOverTime[0].date}</span><span>{data.visitsOverTime.at(-1).date}</span></div>
            </div>
            : <p className="analytics-empty">No opted-in page visits in this date range yet.</p>}
        </section>

        <section className="admin-panel analytics-conversion-panel">
          <header className="admin-panel-heading"><div><span className="admin-section-eyebrow">Contact</span><h2>Contact conversion</h2></div></header>
          <strong className="analytics-conversion-rate">{data.contactConversionRate.toFixed(2)}<small>%</small></strong>
          <p>of sessions that opened the contact page also submitted the form.</p>
          <div className="analytics-conversion-counts"><span><strong>{data.contactFormOpens}</strong> form opens</span><span><strong>{data.contactFormSubmissions}</strong> messages sent</span></div>
        </section>

        <RankedList title="Top pages" eyebrow="Most viewed" items={data.topPages} />
        <RankedList title="Top projects" eyebrow="Project interest" items={data.topProjects} />

        <section className="admin-panel analytics-events-panel">
          <header className="admin-panel-heading"><div><span className="admin-section-eyebrow">Interactions</span><h2>Event counts</h2></div></header>
          <div className="analytics-event-list">
            {data.eventCounts.map((event) => <div key={event.eventName}><span>{formatEventName(event.eventName)}</span><strong>{event.count.toLocaleString()}</strong></div>)}
          </div>
        </section>
      </div>
      <p className="analytics-retention-note"><span>Privacy-first reporting</span> Events are anonymous, use no contact-form contents or IP addresses, and are automatically deleted after 180 days. CV and contact metrics represent opted-in interactions; a CV download count reflects a click on the CV link, not confirmation that a file was saved.</p>
    </>}
  </section>
}

function RankedList({ title, eyebrow, items }) {
  const largest = Math.max(1, ...items.map((item) => item.views))
  return <section className="admin-panel analytics-ranked-panel">
    <header className="admin-panel-heading"><div><span className="admin-section-eyebrow">{eyebrow}</span><h2>{title}</h2></div></header>
    {items.length ? <ol className="analytics-ranked-list">{items.map((item) => <li key={item.path}>
      <div><span title={item.path}>{item.path}</span><strong>{item.views.toLocaleString()}</strong></div>
      <span className="analytics-rank-bar"><i style={{ width: `${Math.max(3, item.views / largest * 100)}%` }} /></span>
    </li>)}</ol> : <p className="analytics-empty">No views in this date range yet.</p>}
  </section>
}

function formatEventName(value) {
  return value.replaceAll('_', ' ').replace(/\b\w/g, (letter) => letter.toUpperCase())
}
