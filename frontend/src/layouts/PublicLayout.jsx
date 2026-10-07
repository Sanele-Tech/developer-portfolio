import { Menu, X } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link, NavLink, Outlet, useLocation } from 'react-router-dom'
import CvLink from '../components/CvLink'
import PortfolioAssistant from '../components/PortfolioAssistant'
import { getAnalyticsConsent, recordAnalyticsEvent, setAnalyticsConsent } from '../services/analytics'

const topLinks = [
  ['Home', '/'],
  ['About', '/about'],
  ['Projects', '/projects'],
  ['Hackathons', '/hackathons'],
  ['Skills', '/skills'],
  ['Certifications', '/certifications'],
  ['Education', '/education'],
  ['Beyond code', '/beyond'],
  ['Contact', '/contact'],
]

export default function PublicLayout() {
  const [menuOpen, setMenuOpen] = useState(false)
  const [analyticsConsent, setConsent] = useState(getAnalyticsConsent)
  const [privacyOpen, setPrivacyOpen] = useState(false)
  const { pathname } = useLocation()
  useEffect(() => {
    document.body.classList.toggle('menu-lock', menuOpen)
    return () => document.body.classList.remove('menu-lock')
  }, [menuOpen])
  useEffect(() => {
    const pageName = pathname.startsWith('/projects/')
      ? pathname.split('/').at(-1).replaceAll('-', ' ')
      : pathname.startsWith('/pages/')
        ? pathname.split('/').at(-1).replaceAll('-', ' ')
      : pathname === '/pages'
        ? 'Pages'
        : pathname === '/certifications'
          ? 'Certifications'
        : pathname === '/hackathons'
          ? 'Hackathons'
        : pathname === '/beyond'
          ? 'Beyond code'
        : topLinks.find(([, path]) => path === pathname)?.[0] || 'Page not found'
      document.title = pathname === '/'
        ? 'Sanele Dlomo — Software Developer'
        : `${pageName.replace(/\b\w/g, (character) => character.toUpperCase())} | Sanele Dlomo`
  }, [pathname])
  useEffect(() => {
      void recordAnalyticsEvent('page_view', pathname)
  }, [pathname, analyticsConsent])
  function updateConsent(consent) {
    setAnalyticsConsent(consent)
    setConsent(consent)
    setPrivacyOpen(false)
  }

  return <div className="site-shell">
    <header className={`topbar${menuOpen ? ' menu-open' : ''}`}>
      <Link to="/" className="top-brand" aria-label="Sanele Dlomo home" onClick={() => setMenuOpen(false)}><span className="top-brand-mark">SD</span><span>Sanele Dlomo<small>SOFTWARE DEVELOPER</small></span></Link>
      <nav className="top-nav" aria-label="Main navigation">
        {topLinks.map(([label, path]) => <NavLink to={path} end={path === '/'} key={path} onClick={() => setMenuOpen(false)}>{label}</NavLink>)}
      </nav>
      <div className="topbar-actions"><CvLink /><button className="menu-button" aria-label={menuOpen ? 'Close navigation' : 'Open navigation'} aria-expanded={menuOpen} onClick={() => setMenuOpen((open) => !open)}>{menuOpen ? <X size={21} /> : <Menu size={21} />}</button></div>
    </header>
    {menuOpen && <button className="public-nav-backdrop" type="button" aria-label="Close navigation" onClick={() => setMenuOpen(false)} />}
    <main className="main-canvas">
      <Outlet />
    </main>
    <PortfolioAssistant />
    {analyticsConsent === 'unset' && <aside className="analytics-consent" aria-label="Privacy notice">
      <div><strong>Your privacy matters</strong><p>With your permission, anonymous page and portfolio interactions are counted to help improve this site. No contact-form content, name, email, or IP address is added to analytics. Data is removed after 180 days.</p></div>
      <div className="analytics-consent-actions">
        <button type="button" onClick={() => updateConsent('accepted')}>Allow anonymous analytics</button>
        <button type="button" className="analytics-consent-decline" onClick={() => updateConsent('declined')}>Decline</button>
      </div>
    </aside>}
    <button type="button" className="privacy-choices-button" onClick={() => setPrivacyOpen(true)}>Privacy choices</button>
    {privacyOpen && <div className="privacy-dialog-backdrop" role="presentation" onMouseDown={(event) => {
      if (event.target === event.currentTarget) setPrivacyOpen(false)
    }}>
      <section className="privacy-dialog" role="dialog" aria-modal="true" aria-labelledby="privacy-title">
        <button type="button" className="privacy-dialog-close" aria-label="Close privacy choices" onClick={() => setPrivacyOpen(false)}>×</button>
        <span className="eyebrow">Your choice</span><h2 id="privacy-title">Privacy & analytics</h2>
        <p>Analytics records anonymous page and portfolio interactions only after you opt in. It does not record contact message contents, names, email addresses, or IP addresses. Events are automatically deleted after 180 days.</p>
        <div className="analytics-consent-actions">
          <button type="button" onClick={() => updateConsent('accepted')}>Allow anonymous analytics</button>
          <button type="button" className="analytics-consent-decline" onClick={() => updateConsent('declined')}>Decline analytics</button>
        </div>
      </section>
    </div>}
  </div>
}
