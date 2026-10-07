import { useEffect, useRef, useState } from 'react'
import { Activity, ArrowUpRight, Award, Bell, BriefcaseBusiness, FileText, FolderKanban, GraduationCap, History, LayoutDashboard, LogOut, MailOpen, Menu, PanelsTopLeft, Search, Settings, Sparkles, X } from 'lucide-react'
import { Link, NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useAdminAuth } from '../admin/adminAuthContext'
import { apiFetch } from '../services/api'

const workspaceDestinations = [
  { label: 'Overview', path: '/admin' },
  { label: 'Projects', path: '/admin/projects' },
  { label: 'Experience', path: '/admin/experience' },
  { label: 'Education', path: '/admin/education' },
  { label: 'Skills', path: '/admin/skills' },
  { label: 'Achievements', path: '/admin/achievements' },
  { label: 'Documents', path: '/admin/documents' },
  { label: 'Pages', path: '/admin/pages' },
  { label: 'Profile and settings', path: '/admin/settings' },
  { label: 'Messages inbox', path: '/admin/inbox' },
  { label: 'Analytics', path: '/admin/analytics', adminOnly: true },
  { label: 'Email templates', path: '/admin/email-templates', adminOnly: true },
  { label: 'Email delivery log', path: '/admin/email-deliveries', adminOnly: true },
  { label: 'Audit log', path: '/admin/audit-logs', adminOnly: true },
]

export default function AdminLayout() {
  const { user, setUser } = useAdminAuth()
  const [error, setError] = useState('')
  const [unreadMessages, setUnreadMessages] = useState(null)
  const [notificationError, setNotificationError] = useState('')
  const [mobileNavOpen, setMobileNavOpen] = useState(false)
  const [search, setSearch] = useState('')
  const [searchOpen, setSearchOpen] = useState(false)
  const searchRef = useRef(null)
  const navigate = useNavigate()
  const { pathname } = useLocation()
  const pageTitle = getAdminPageTitle(pathname)

  useEffect(() => {
    document.body.classList.add('admin-layout-open')
    return () => document.body.classList.remove('admin-layout-open')
  }, [])

  useEffect(() => {
    document.title = `${pageTitle} | Admin | Sanele Dlomo`
  }, [pageTitle])

  useEffect(() => {
    let active = true
    apiFetch('/api/admin/overview')
      .then((overview) => {
        if (!active) return
        setUnreadMessages(overview.newMessages)
        setNotificationError('')
      })
      .catch((requestError) => {
        if (!active) return
        setUnreadMessages(null)
        setNotificationError(requestError.message)
      })
    return () => { active = false }
  }, [pathname])

  useEffect(() => {
    function closeOnEscape(event) {
      if (event.key === 'Escape') {
        setMobileNavOpen(false)
        setSearchOpen(false)
      }
      if (event.key === '/' && !['INPUT', 'TEXTAREA'].includes(document.activeElement?.tagName)) {
        event.preventDefault()
        searchRef.current?.focus()
        setSearchOpen(true)
      }
    }
    window.addEventListener('keydown', closeOnEscape)
    return () => window.removeEventListener('keydown', closeOnEscape)
  }, [])

  async function logout() {
    setError('')
    try {
      await apiFetch('/api/auth/logout', { method: 'POST' })
      setUser(null)
      navigate('/admin/login', { replace: true })
    } catch (requestError) {
      setError(requestError.message)
    }
  }

  const searchResults = workspaceDestinations
    .filter((destination) => !destination.adminOnly || user.roles?.includes('ADMIN'))
    .filter((destination) => destination.label.toLowerCase().includes(search.trim().toLowerCase()))
    .slice(0, 6)

  function navigateTo(destination) {
    setSearch('')
    setSearchOpen(false)
    setMobileNavOpen(false)
    navigate(destination.path)
  }

  function submitSearch(event) {
    event.preventDefault()
    const destination = searchResults[0]
    if (destination) navigateTo(destination)
    else setSearchOpen(true)
  }

  return <div className="admin-shell">
    <aside className={`admin-sidebar ${mobileNavOpen ? 'is-open' : ''}`}>
      <Link to="/admin" className="admin-brand"><span>SD</span><div>Sanele Dlomo<small>PORTFOLIO STUDIO</small></div></Link>
      <p className="admin-nav-label">WORKSPACE</p>
      <nav className="admin-nav" aria-label="Admin navigation" onClick={() => setMobileNavOpen(false)}>
        <NavLink to="/admin" end><LayoutDashboard size={17} />Overview</NavLink>
        <span className="admin-nav-group">Portfolio</span>
        <NavLink to="/admin/projects"><FolderKanban size={17} />Projects</NavLink>
        <NavLink to="/admin/experience"><BriefcaseBusiness size={17} />Experience</NavLink>
        <NavLink to="/admin/education"><GraduationCap size={17} />Education</NavLink>
        <NavLink to="/admin/skills"><Sparkles size={17} />Skills</NavLink>
        <NavLink to="/admin/achievements"><Award size={17} />Achievements</NavLink>
        <NavLink to="/admin/documents"><FileText size={17} />Documents</NavLink>
        <span className="admin-nav-group">Manage</span>
        <NavLink to="/admin/pages"><PanelsTopLeft size={17} />Pages</NavLink>
        <NavLink to="/admin/settings"><Settings size={17} />Profile & settings</NavLink>
        <NavLink to="/admin/inbox" className="admin-inbox-nav"><MailOpen size={17} />Inbox{unreadMessages > 0 && <span className="admin-nav-count">{unreadMessages}</span>}{notificationError && <span className="sr-only">Message count unavailable: {notificationError}</span>}</NavLink>
        {user.roles?.includes('ADMIN') && <NavLink to="/admin/analytics"><Activity size={17} />Analytics</NavLink>}
        {user.roles?.includes('ADMIN') && <NavLink to="/admin/email-templates"><FileText size={17} />Email templates</NavLink>}
        {user.roles?.includes('ADMIN') && <NavLink to="/admin/email-deliveries"><History size={17} />Delivery log</NavLink>}
        {user.roles?.includes('ADMIN') && <NavLink to="/admin/audit-logs"><History size={17} />Audit log</NavLink>}
      </nav>
      <div className="admin-sidebar-bottom">
        <Link to="/" target="_blank" rel="noreferrer"><ArrowUpRight size={16} />Return to public site</Link>
        <div className="admin-user"><span className="admin-avatar">{user.displayName?.slice(0, 1).toUpperCase() || 'A'}</span><div><strong>{user.displayName}</strong><small>{user.roles?.join(', ')}</small></div></div>
        <button type="button" className="admin-logout" onClick={logout}><LogOut size={16} />Sign out</button>
        {error && <p className="admin-inline-error" role="alert">{error}</p>}
      </div>
    </aside>
    {mobileNavOpen && <button className="admin-nav-backdrop" type="button" aria-label="Close navigation" onClick={() => setMobileNavOpen(false)} />}
    <main className="admin-main">
      <header className="admin-topbar">
        <button className="admin-menu-toggle" type="button" aria-label={mobileNavOpen ? 'Close navigation' : 'Open navigation'} aria-expanded={mobileNavOpen} onClick={() => setMobileNavOpen((open) => !open)}>
          {mobileNavOpen ? <X size={19} /> : <Menu size={19} />}
        </button>
        <h1 className="admin-topbar-title">{pageTitle}</h1>
        <div className="admin-workspace-search-wrap">
          <form className="admin-search" role="search" onSubmit={submitSearch}>
            <Search size={16} aria-hidden="true" />
            <input ref={searchRef} value={search} onChange={(event) => { setSearch(event.target.value); setSearchOpen(true) }} onFocus={() => setSearchOpen(true)} onBlur={() => window.setTimeout(() => setSearchOpen(false), 120)} aria-label="Search admin workspace" aria-expanded={searchOpen && search.trim().length > 0} aria-controls="admin-search-results" placeholder="Search workspace…" autoComplete="off" />
            <kbd aria-hidden="true">/</kbd>
          </form>
          {searchOpen && search.trim() && <div className="admin-search-results" id="admin-search-results" role="listbox" aria-label="Workspace pages">
            {searchResults.length ? searchResults.map((destination) => <button key={destination.path} type="button" role="option" aria-selected="false" onMouseDown={(event) => event.preventDefault()} onClick={() => navigateTo(destination)}>{destination.label}<ArrowUpRight size={13} /></button>) : <p>No matching workspace page</p>}
          </div>}
        </div>
        <div className="admin-topbar-actions">
          <Link className="admin-notification-button" to="/admin/inbox" aria-label={`Inbox${unreadMessages ? `, ${unreadMessages} unread messages` : ''}`} title={notificationError || undefined}>
            <Bell size={17} />{unreadMessages > 0 && <span aria-hidden="true" />}
          </Link>
          <details className="admin-account-menu">
            <summary aria-label={`Account menu for ${user.displayName}`}><span className="admin-avatar">{user.displayName?.slice(0, 1).toUpperCase() || 'A'}</span><span>{user.displayName || user.username}</span></summary>
            <div className="admin-account-popover">
              <strong>{user.displayName || user.username}</strong><small>{user.roles?.join(', ')}</small>
              <Link to="/admin/settings" onClick={() => setMobileNavOpen(false)}><Settings size={15} />Profile & settings</Link>
              <Link to="/" target="_blank" rel="noreferrer"><ArrowUpRight size={15} />Return to public site</Link>
              <button type="button" onClick={logout}><LogOut size={15} />Sign out</button>
            </div>
          </details>
        </div>
      </header>
      <div className="admin-content"><Outlet /></div>
    </main>
  </div>
}

function getAdminPageTitle(pathname) {
  if (pathname.includes('/analytics')) return 'Analytics'
  if (pathname.includes('/experience')) return pathname.endsWith('/new') ? 'New experience' : pathname.endsWith('/edit') ? 'Edit experience' : 'Experience'
  if (pathname.includes('/education')) return pathname.endsWith('/new') ? 'New education' : pathname.endsWith('/edit') ? 'Edit education' : 'Education'
  if (pathname.includes('/skills')) return pathname.endsWith('/new') ? 'New skill' : pathname.endsWith('/edit') ? 'Edit skill' : 'Skills'
  if (pathname.includes('/achievements')) return pathname.endsWith('/new') ? 'New achievement' : pathname.endsWith('/edit') ? 'Edit achievement' : 'Achievements'
  if (pathname.includes('/documents')) return pathname.endsWith('/new') ? 'New document' : pathname.endsWith('/edit') ? 'Edit document' : 'Documents'
  if (pathname.includes('/pages')) return pathname.endsWith('/new') ? 'New page' : pathname.endsWith('/edit') ? 'Edit page' : 'Pages'
  if (pathname.includes('/settings')) return pathname.endsWith('/new') || pathname.endsWith('/edit') ? 'Edit setting' : 'Settings'
  if (pathname.includes('/audit-logs')) return 'Audit log'
  if (pathname.includes('/inbox')) return 'Messages'
  if (pathname.includes('/email-templates')) return 'Email templates'
  if (pathname.includes('/email-deliveries')) return 'Email delivery log'
  if (pathname.includes('/projects')) return pathname.endsWith('/new') ? 'New project' : pathname.endsWith('/edit') ? 'Edit project' : 'Projects'
  return 'Dashboard'
}
