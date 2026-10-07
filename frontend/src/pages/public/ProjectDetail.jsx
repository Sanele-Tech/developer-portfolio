import { ArrowLeft, ExternalLink } from 'lucide-react'
import { Link, useParams } from 'react-router-dom'
import { useApi } from '../../hooks/useApi'
import EmptyState from '../../components/EmptyState'
import { useEffect } from 'react'
import { recordAnalyticsEvent } from '../../services/analytics'
import { resolvePublicAssetUrl } from '../../services/api'

export default function ProjectDetail() {
  const { slug } = useParams()
  const { data, loading, error, retry } = useApi(`/api/projects/${slug}`)
  const project = Array.isArray(data) ? data.find((item) => item.slug === slug) : data
  useEffect(() => {
    if (project) void recordAnalyticsEvent('project_view', `/projects/${slug}`)
  }, [project, slug])
  if (loading) return <section className="page-content" role="status">Loading project…</section>
  if (error) return <section className="page-content"><EmptyState title="Project unavailable" message="The project could not be loaded." /><button type="button" className="button button-light" onClick={retry}>Try again</button><Link className="back-link" to="/projects"><ArrowLeft size={16} /> Back to projects</Link></section>
  if (!project) return <section className="page-content"><EmptyState title="Project not found" message="That project does not exist or is not published." /><Link className="back-link" to="/projects"><ArrowLeft size={16} /> Back to projects</Link></section>
  const repositoryUrl = project.repositoryUrl || project.links?.find((link) => /github|source|repository/i.test(`${link.label} ${link.linkType}`))?.url
  const coverImageUrl = resolvePublicAssetUrl(project.imageUrl)
  return <article className="page-content project-detail">
    <Link className="back-link" to="/projects"><ArrowLeft size={16} /> Back to projects</Link>
    <span className="eyebrow">Case study</span><h1>{project.title}</h1>
    <p className="page-intro">{project.description}</p>
    <div className="project-detail-actions">
      {repositoryUrl && <a className="button button-dark" href={repositoryUrl} target="_blank" rel="noreferrer" onClick={() => void recordAnalyticsEvent('project_github_click', `/projects/${slug}`)}>View source on GitHub <ExternalLink size={16} /></a>}
      {project.liveUrl && <a className="button button-light" href={project.liveUrl} target="_blank" rel="noreferrer" onClick={() => void recordAnalyticsEvent('project_demo_click', `/projects/${slug}`)}>Open live project <ExternalLink size={16} /></a>}
    </div>
    {coverImageUrl && <figure className="project-cover"><img className="detail-image" src={coverImageUrl} alt={`${project.title} overview`} /><figcaption>Project overview</figcaption></figure>}
    <div className="tag-row project-detail-tags">{project.technologies?.map((technology) => <span key={technology}>{technology}</span>)}</div>
    <section className="project-gallery"><div className="section-heading"><div><span className="eyebrow">Product walkthrough</span><h2>Interface screens</h2></div></div>{project.images?.length ? project.images.map((image, index) => <figure className="gallery-item" key={`${image.imageUrl}-${image.sortOrder ?? index}`}><div className="gallery-image-frame"><img src={resolvePublicAssetUrl(image.imageUrl) || undefined} alt={image.altText || image.heading || `${project.title} interface screen ${index + 1}`} loading="lazy" /></div><figcaption><h3>{image.heading || `Screen ${index + 1}`}</h3>{image.description && <p>{image.description}</p>}</figcaption></figure>) : <p className="gallery-empty">Interface screenshots haven’t been added yet.</p>}</section>
  </article>
}