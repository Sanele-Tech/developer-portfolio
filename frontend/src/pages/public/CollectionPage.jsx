import { useApi } from '../../hooks/useApi'
import EmptyState from '../../components/EmptyState'
import PageHeading from '../../components/PageHeading'
import ProjectCard from '../../components/ProjectCard'
import ExpandableText from '../../components/ExpandableText'
import ImageSlideshow from '../../components/ImageSlideshow'
import { resolvePublicAssetUrl } from '../../services/api'

const config = {
  experience: { title: 'Experience', eyebrow: 'My journey', endpoint: '/api/experience' },
  education: { title: 'Education', eyebrow: 'Learning', endpoint: '/api/education' },
  skills: { title: 'Skills', eyebrow: 'Tools and technologies', endpoint: '/api/skills' },
  achievements: { title: 'Achievements', eyebrow: 'Recognition', endpoint: '/api/achievements' },
}

export function CollectionPage({ type }) {
  const page = config[type]
  const { data, loading, error, retry } = useApi(page.endpoint)
  const items = data || []
  return <section className="page-content" aria-busy={loading}><PageHeading eyebrow={page.eyebrow} title={page.title} /><p className="page-intro">A closer look at the work, learning and tools that shape my practice.</p>{loading && !items.length && <p className="page-intro" role="status">Loading {page.title.toLowerCase()}…</p>}{error && <p className="public-data-error" role="alert">We couldn’t refresh {page.title.toLowerCase()}. <button type="button" onClick={retry}>Try again</button></p>}{items.length > 0 && <div className={`collection-grid ${type}-grid`}>{items.map((item) => <CollectionItem key={item.id} type={type} item={item} />)}</div>}{!loading && !error && !items.length && <EmptyState title={`No ${page.title.toLowerCase()} published yet`} message="Please check back later." />}</section>
}

function CollectionItem({ type, item }) {
  if (type === 'skills') return <article className="collection-card"><span className="eyebrow">{item.category || 'Skill'}</span><h3>{item.name}</h3><p>Proficiency: {item.proficiency ?? 'Developing'}</p></article>
  if (type === 'experience') return <article className="collection-card"><span className="timeline-date">{item.startDate || '2023'} — {item.currentRole ? 'Present' : item.endDate || ''}</span><h3>{item.jobTitle}</h3><strong>{item.company}</strong><p>{item.description}</p></article>
  if (type === 'education') return <article className="education-story-card">
    <ImageSlideshow className="education-story-gallery"
      images={(item.imageUrls?.length ? item.imageUrls : ['/images/portfolio/credential-methodology.svg']).map((image) => resolvePublicAssetUrl(image) || image)}
      alt={`${item.institution} education experience`} />
    <div className="education-story-content">
      <span className="timeline-date">{formatStudyDates(item.startDate, item.endDate)}</span>
      <h2>{item.institution}</h2>
      <h3>{item.qualification}</h3>
      {(item.description || item.experience) && <ExpandableText className="education-story-description" threshold={220} text={[item.description, item.experience].filter(Boolean).join('\n\n')} />}
      {item.coreModules?.length > 0 && <div className="education-modules"><h4>Core modules</h4><ul>{item.coreModules.map((module) => <li key={module}>{module}</li>)}</ul></div>}
    </div>
  </article>
  return <article className="collection-card"><span className="timeline-date">{item.achievedOn || 'Recognition'}</span><h3>{item.title}</h3><p>{item.description}</p></article>
}

function formatStudyDates(startDate, endDate) {
  if (!startDate && !endDate) return 'Study experience'
  const format = (value) => new Intl.DateTimeFormat(undefined, { month: 'short', year: 'numeric', timeZone: 'UTC' })
    .format(new Date(`${value}T00:00:00Z`))
  const start = startDate ? format(startDate) : 'Start date not listed'
  const end = endDate ? format(endDate) : 'Present'
  return `${start} — ${end}`
}

export function ProjectsPage() {
  const { data, loading, error, retry } = useApi('/api/projects')
  const projects = data || []
  return <section className="page-content" aria-busy={loading}><PageHeading eyebrow="Selected work" title="Projects" /><p className="page-intro">A collection of products, experiments and systems built to solve real problems.</p>{loading && !projects.length && <p className="page-intro" role="status">Loading projects…</p>}{error && <p className="public-data-error" role="alert">Projects could not be refreshed. <button type="button" onClick={retry}>Try again</button></p>}{projects.length > 0 && <div className="project-grid projects-page-grid">{projects.map((project) => <ProjectCard key={project.id} project={project} />)}</div>}{!loading && !error && !projects.length && <EmptyState title="No projects published yet" message="Please check back later." />}</section>
}
