import { ArrowRight, Award, BriefcaseBusiness, Code2 as Github, Mail, Trophy, Users } from 'lucide-react'
import { Fragment, useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useApi } from '../../hooks/useApi'
import ProjectCard from '../../components/ProjectCard'
import QuickProfile from '../../components/QuickProfile'
import SectionHeading from '../../components/SectionHeading'
import ImageSlideshow from '../../components/ImageSlideshow'
import { recordAnalyticsEvent } from '../../services/analytics'
import { resolveExternalUrl, resolvePublicAssetUrl, resolvePublicImageUrl } from '../../services/api'

const processSteps = [
  ['Understand', 'Clarify the problem and who it affects.'],
  ['Design', 'Plan the data, system, and experience.'],
  ['Build', 'Create useful, maintainable software.'],
  ['Test', 'Check the details and edge cases.'],
  ['Improve', 'Learn from feedback and keep iterating.'],
]
const cachedAvatarKey = 'portfolio-profile-avatar-url'

export default function Home() {
  const { data: home, loading: homeLoading, error: homeError, retry: retryHome } = useApi('/api/home')
  const { data: profile, loading: profileLoading, error: profileError, retry: retryProfile } = useApi('/api/profile')
  const { data: achievements } = useApi('/api/achievements')
  const [cachedAvatar, setCachedAvatar] = useState(readCachedAvatar)
  const projects = home?.featuredProjects || []
  const credentials = achievements || []
  const featureCards = credentials.filter((item) => item.cardType && item.cardType !== 'CERTIFICATION').slice(0, 3)
  const firstCredential = credentials.find((item) => item.cardType === 'CERTIFICATION')
  const profileAvatar = resolvePublicAssetUrl(profile?.avatarUrl)
  const avatarSrc = profileAvatar || (profileLoading || !profile ? cachedAvatar : null)
  const avatarIsCached = Boolean(avatarSrc && avatarSrc === cachedAvatar)
  const githubUrl = resolveExternalUrl(profile?.githubUrl)
  const linkedinUrl = resolveExternalUrl(profile?.linkedinUrl)
  const email = profile?.email

  useEffect(() => {
    if (profileLoading || !profile) return
    try {
      if (profileAvatar) window.localStorage.setItem(cachedAvatarKey, profileAvatar)
      else window.localStorage.removeItem(cachedAvatarKey)
    } catch {
      return
    }
  }, [profile, profileAvatar, profileLoading])

  function handleAvatarError() {
    try {
      window.localStorage.removeItem(cachedAvatarKey)
    } catch {
      return
    }
    setCachedAvatar(null)
  }

  return <>
    <section className="hero-section home-hero" aria-busy={profileLoading}>
      <div className="hero-grid-overlay" aria-hidden="true" />
      <div className="hero-copy">
        <p className="hero-status"><span />SOFTWARE DEVELOPER · ICT APPLICATIONS DEVELOPMENT</p>
        <span className="eyebrow">Thoughtful ideas. Useful software.</span>
        {profile?.name
          ? <h1>{profile.name}</h1>
          : profileLoading
            ? <h1 className="home-loading-title">Loading profile…</h1>
            : <h1>Sanele Dlomo</h1>}
        {profile?.title && <p className="role">{profile.title}</p>}
        <p className="hero-intro">{profile?.bio || 'I build practical digital products and enjoy taking ideas from a real problem to a working solution.'}</p>
        {profileError && <p className="public-data-error" role="alert">Your profile could not be refreshed. <button type="button" onClick={retryProfile}>Try again</button></p>}
        <div className="hero-actions">
          <Link className="button button-dark" to="/projects">Explore my work <ArrowRight size={17} /></Link>
          <Link className="button button-light" to="/contact">Get in touch</Link>
        </div>
        <div className="social-row">
          {githubUrl && <a href={githubUrl} aria-label="GitHub" onClick={() => void recordAnalyticsEvent('social_link_click')}><Github size={20} /></a>}
          {linkedinUrl && <a href={linkedinUrl} aria-label="LinkedIn" onClick={() => void recordAnalyticsEvent('social_link_click')}><BriefcaseBusiness size={20} /></a>}
          {email && <a href={`mailto:${email}`} aria-label="Email" onClick={() => void recordAnalyticsEvent('social_link_click')}><Mail size={20} /></a>}
        </div>
      </div>
      <div className="hero-portrait">
        <div className="portrait-art">
          <ProfilePortrait key={avatarSrc || 'loading'} src={avatarSrc} name={profile?.name || 'Sanele Dlomo'} cached={avatarIsCached} loading={profileLoading && !profile} onError={handleAvatarError} />
        </div>
      </div>
      <div className="hero-index" aria-hidden="true">SD—01</div>
    </section>

    <div className="home-profile-strip">
      <QuickProfile profile={home?.quickProfile} loading={homeLoading} error={homeError} retry={retryHome} />
    </div>

    <section className="content-section projects-section" aria-busy={homeLoading}>
      <SectionHeading eyebrow="Selected work" title="Projects" action="View all projects" to="/projects" />
      {homeError && <p className="public-data-error" role="alert">Featured work could not be refreshed. <button type="button" onClick={retryHome}>Try again</button></p>}
      <div className="project-grid">
        {projects.slice(0, 3).map((project) => <ProjectCard compact key={project.id} project={project} />)}
        {homeLoading && !projects.length && <div className="empty-state" role="status">Loading featured projects…</div>}
        {!homeLoading && !homeError && !projects.length && <div className="empty-state">No projects have been published yet.</div>}
      </div>
    </section>

    <section className="content-section beyond-preview">
      <SectionHeading eyebrow="Outside the usual routine" title="More than code" action="A little more about me" to="/beyond" />
      <div className="highlight-grid">
        {featureCards.map((item) => <Link className="highlight-card" key={item.id} to={item.cardType === 'CHALLENGE' ? '/hackathons' : '/beyond'}>
          <ImageSlideshow className="highlight-image-frame"
            images={(item.imageUrls?.length ? item.imageUrls : [item.imageUrl || '/images/portfolio/credential-data.svg'])
              .map((image) => resolvePublicImageUrl(image) || image)}
            alt={`${item.title} portfolio highlight`} />
          <div className="highlight-content"><span className="highlight-icon">{item.cardType === 'CHALLENGE' ? <Trophy size={20} /> : item.cardType === 'BUSINESS' ? <BriefcaseBusiness size={20} /> : <Users size={20} />}</span><span className="event-label">{item.issuer || item.cardType}</span>
            <strong>{item.title}</strong><span>{item.description}</span><ArrowRight className="highlight-arrow" size={17} /></div>
        </Link>)}
        <Link className="highlight-card" to="/certifications">
          <ImageSlideshow className="highlight-image-frame"
            images={(firstCredential?.imageUrls?.length ? firstCredential.imageUrls : [firstCredential?.imageUrl || '/images/portfolio/credential-data.svg'])
              .map((image) => resolvePublicImageUrl(image) || image)}
            alt={`${firstCredential?.title || 'Certification'} portfolio highlight`} />
          <div className="highlight-content"><span className="highlight-icon"><Award size={20} /></span><span className="event-label">{firstCredential?.issuer || 'Keep learning'}</span>
            <strong>{firstCredential?.title || 'Certifications'}</strong><span>{firstCredential?.description || 'Courses and verified credentials'}</span><ArrowRight className="highlight-arrow" size={17} /></div>
        </Link>
      </div>
    </section>

    <section className="content-section process-section">
      <span className="eyebrow">My process</span><h2>From problem to progress</h2>
      <p className="process-intro">I approach each project with curiosity, care for the people who will use it, and a focus on useful outcomes.</p>
      <div className="process-grid">{processSteps.map(([title, description], index) => <Fragment key={title}>
        <div className="process-step"><div className="process-icon"><span>0{index + 1}</span></div><h3>{title}</h3><p>{description}</p></div>
        {index < processSteps.length - 1 && <div className="process-connector" aria-hidden="true"><ArrowRight size={18} /></div>}
      </Fragment>)}</div>
    </section>

    <section className="home-cta"><div className="home-cta-copy"><span className="eyebrow">Let’s build something useful</span><h2>Have an idea or an opportunity?</h2><p>I’m open to meaningful work, collaborations, and conversations about technology.</p><Link className="button button-light" to="/contact">Get in touch <ArrowRight size={18} /></Link></div><div className="home-cta-mark" aria-hidden="true">Build.<br />Learn.<br />Contribute.</div></section>
    <footer className="public-footer"><span>© {new Date().getFullYear()} Sanele Dlomo</span><span>Made with intention.</span><Link to="/contact">Contact</Link></footer>
  </>
}

function ProfilePortrait({ src, name, cached, loading, onError }) {
  const [loaded, setLoaded] = useState(() => Boolean(src && cached))
  const initials = name.split(/\s+/).filter(Boolean).map((part) => part[0]).join('').slice(0, 2)
  return <>
    <div className={`hero-avatar-placeholder${loaded ? ' is-hidden' : ''}`} aria-hidden="true">{loading ? '' : initials}</div>
    {src && <img className={loaded ? 'is-loaded' : 'is-loading'} src={src} alt={`Portrait of ${name}`} loading="eager" fetchPriority="high" decoding="async" onLoad={() => setLoaded(true)} onError={() => { setLoaded(false); onError() }} />}
  </>
}

function readCachedAvatar() {
  try {
    return resolveExternalUrl(window.localStorage.getItem(cachedAvatarKey))
  } catch {
    return null
  }
}
