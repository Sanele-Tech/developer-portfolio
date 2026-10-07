import { NavLink } from 'react-router-dom'
import { Award, BookOpenText, BriefcaseBusiness, CircleUserRound, Code2, GraduationCap, LayoutDashboard, Mail, Wrench } from 'lucide-react'
import { recordAnalyticsEvent } from '../services/analytics'

const links = [
  ['Home', '/', LayoutDashboard], ['About', '/about', CircleUserRound], ['Projects', '/projects', BriefcaseBusiness],
  ['Experience', '/experience', BriefcaseBusiness], ['Education', '/education', GraduationCap], ['Skills', '/skills', Wrench],
  ['Achievements', '/achievements', Award], ['Pages', '/pages', BookOpenText], ['Contact', '/contact', Mail],
]

export default function Sidebar({ menuOpen, onClose }) {
  return <aside className={`sidebar ${menuOpen ? 'sidebar-open' : ''}`}>
    <div className="brand-mark">SD</div><div className="brand-name">Sanele Dlomo</div>
    <button className="close-menu" aria-label="Close navigation" onClick={onClose}>×</button>
    <nav className="sidebar-nav" aria-label="Primary navigation">
      {links.map(([label, path, Icon]) => <NavLink key={path} to={path} end={path === '/'} onClick={onClose}><Icon size={18} />{label}</NavLink>)}
    </nav>
    <div className="sidebar-bottom"><span className="eyebrow">Quick Links</span><a href="https://github.com" target="_blank" rel="noreferrer" onClick={() => void recordAnalyticsEvent('social_link_click')}><Code2 size={17} /> GitHub</a><a href="https://linkedin.com" target="_blank" rel="noreferrer" onClick={() => void recordAnalyticsEvent('social_link_click')}><BriefcaseBusiness size={17} /> LinkedIn</a><a href="mailto:hello@saneledlomo.dev" onClick={() => void recordAnalyticsEvent('social_link_click')}><Mail size={17} /> Email</a><p>Build. Improve. Create.<br />Better Solutions.</p></div>
  </aside>
}
