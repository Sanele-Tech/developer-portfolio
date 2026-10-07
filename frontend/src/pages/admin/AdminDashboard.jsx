import { useEffect, useState } from 'react'
import { Activity, ArrowRight, ArrowUpRight, Award, BriefcaseBusiness, FolderKanban, MailOpen, Sparkles } from 'lucide-react'
import { Link } from 'react-router-dom'
import { useAdminAuth } from '../../admin/adminAuthContext'
import { apiFetch } from '../../services/api'

const metrics = [
  { label: 'Total Projects', key: 'projects', path: '/admin/projects', Icon: FolderKanban },
  { label: 'Experience Entries', key: 'experience', path: '/admin/experience', Icon: BriefcaseBusiness },
  { label: 'Technical Skills', key: 'skills', path: '/admin/skills', Icon: Sparkles },
  { label: 'Unread Messages', key: 'newMessages', path: '/admin/inbox', Icon: MailOpen },
]

const quickActions = [
  { label: 'Add Project', description: 'Create a portfolio project.', path: '/admin/projects/new', Icon: FolderKanban },
  { label: 'Add Experience', description: 'Add a role to your career history.', path: '/admin/experience/new', Icon: BriefcaseBusiness },
  { label: 'Add Achievement', description: 'Record an award or milestone.', path: '/admin/achievements/new', Icon: Award },
  { label: 'View Messages', description: 'Review portfolio enquiries.', path: '/admin/inbox', Icon: MailOpen },
]

export default function AdminDashboard() {
  const { user } = useAdminAuth()
  const [overview, setOverview] = useState(null)
  const [overviewError, setOverviewError] = useState('')
  const [activity, setActivity] = useState([])
  const [activityError, setActivityError] = useState('')
  const [activityLoading, setActivityLoading] = useState(true)
  const isAdmin = user.roles?.includes('ADMIN')

  useEffect(() => {
    let active = true
    const requests = [
      apiFetch('/api/admin/overview'),
      apiFetch('/api/admin/contact-messages?page=0&size=6'),
    ]
    if (isAdmin) requests.push(apiFetch('/api/admin/audit-logs?page=0&size=6'))

    Promise.allSettled(requests).then(([overviewResult, messagesResult, auditResult]) => {
      if (!active) return
      if (overviewResult.status === 'fulfilled') {
        setOverview(overviewResult.value)
      } else {
        setOverviewError(overviewResult.reason.message)
      }

      const activityItems = []
      const errors = []
      if (messagesResult.status === 'fulfilled') {
        activityItems.push(...(messagesResult.value.content || []).map((message) => ({
          id: `message-${message.id}`,
          type: 'message',
          title: 'New message received',
          detail: message.name || message.subject || 'Portfolio enquiry',
          createdAt: message.createdAt,
        })))
      } else {
        errors.push(`Messages: ${messagesResult.reason.message}`)
      }

      if (isAdmin && auditResult?.status === 'fulfilled') {
        activityItems.push(...(auditResult.value.content || []).map((entry) => ({
          id: `audit-${entry.id}`,
          type: 'audit',
          title: formatAuditTitle(entry.action, entry.entityType),
          detail: entry.actor ? `By ${entry.actor}` : '',
          createdAt: entry.createdAt,
        })))
      } else if (isAdmin && auditResult?.status === 'rejected') {
        errors.push(`Audit log: ${auditResult.reason.message}`)
      }

      activityItems.sort((left, right) => timestamp(right.createdAt) - timestamp(left.createdAt))
      setActivity(activityItems.slice(0, 6))
      setActivityError(errors.join(' '))
      setActivityLoading(false)
    })

    return () => { active = false }
  }, [isAdmin])

  return <section className="admin-page admin-dashboard">
    <p className="admin-dashboard-intro">Manage your portfolio content, professional experience, and incoming enquiries.</p>

    {overviewError && <p className="admin-form-error" role="alert">Could not load dashboard totals: {overviewError}</p>}

    <div className="admin-overview-grid" aria-label="Portfolio totals">
      {metrics.map(({ label, key, path, Icon }) => (
        <Link key={key} to={path} className={`admin-overview-card ${key === 'newMessages' && overview?.newMessages > 0 ? 'has-new-items' : ''}`} aria-label={`${label}: ${overview ? overview[key] ?? 0 : 'unavailable'}`}>
          <span className="admin-metric-icon"><Icon size={17} aria-hidden="true" /></span>
          <span className="admin-metric-label">{label}</span>
          <strong>{overview ? overview[key] ?? 0 : '—'}</strong>
          <ArrowUpRight className="admin-metric-link-icon" size={15} aria-hidden="true" />
        </Link>
      ))}
    </div>

    <div className="admin-dashboard-lower">
      <section className="admin-panel admin-activity-panel" aria-labelledby="recent-activity-title">
        <div className="admin-panel-heading">
          <div><span className="admin-section-eyebrow">WORKSPACE</span><h2 id="recent-activity-title">Recent Activity</h2></div>
          {isAdmin && <Link to="/admin/audit-logs" aria-label="View audit log" title="View audit log"><ArrowUpRight size={17} /></Link>}
        </div>
        {activityError && <p className="admin-inline-error" role="alert">Some activity could not be loaded. {activityError}</p>}
        {activityLoading
          ? <p className="admin-activity-state" role="status">Loading recent activity…</p>
          : activity.length === 0
            ? <div className="admin-feed-empty"><Activity size={22} /><strong>No recent activity</strong><span>New messages and content updates will appear here.</span></div>
            : <ul className="admin-activity-list">
              {activity.map((item) => {
                const Icon = item.type === 'message' ? MailOpen : Activity
                return <li key={item.id}>
                  <span className={`admin-activity-icon ${item.type === 'message' ? 'is-message' : ''}`}><Icon size={15} aria-hidden="true" /></span>
                  <span className="admin-activity-copy"><strong>{item.title}</strong><small>{item.detail || 'Portfolio content updated'}</small></span>
                  <time dateTime={item.createdAt || undefined}>{formatTimeAgo(item.createdAt)}</time>
                </li>
              })}
            </ul>}
      </section>

      <section className="admin-panel admin-quick-actions" aria-labelledby="quick-actions-title">
        <div className="admin-panel-heading">
          <div><span className="admin-section-eyebrow">SHORTCUTS</span><h2 id="quick-actions-title">Quick Actions</h2></div>
        </div>
        {quickActions.map(({ label, description, path, Icon }) => (
          <Link key={path} to={path}>
            <span className="admin-quick-icon"><Icon size={16} aria-hidden="true" /></span>
            <span><strong>{label}</strong><small>{description}</small></span>
            <ArrowRight size={15} aria-hidden="true" />
          </Link>
        ))}
      </section>
    </div>
  </section>
}

function formatAuditTitle(action, entityType) {
  const label = [action, entityType]
    .filter(Boolean)
    .join(' ')
    .replace(/[_-]+/g, ' ')
    .toLowerCase()
  return label ? label.replace(/\b\w/g, (character) => character.toUpperCase()) : 'Portfolio content updated'
}

function timestamp(value) {
  const parsed = value ? Date.parse(value) : 0
  return Number.isFinite(parsed) ? parsed : 0
}

function formatTimeAgo(value) {
  const createdAt = timestamp(value)
  if (!createdAt) return 'Time unavailable'
  const minutes = Math.max(0, Math.floor((Date.now() - createdAt) / 60000))
  if (minutes < 1) return 'Just now'
  if (minutes < 60) return `${minutes}m ago`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}h ago`
  const days = Math.floor(hours / 24)
  if (days < 30) return `${days}d ago`
  return new Date(createdAt).toLocaleDateString()
}
