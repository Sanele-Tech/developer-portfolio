import { ArrowRight, Check } from 'lucide-react'
import { Link } from 'react-router-dom'
import { resolvePublicAssetUrl } from '../services/api'

export default function ProjectCard({ project, compact = false }) {
  const imageUrl = resolvePublicAssetUrl(project.imageUrl)
  return <article className={`project-card ${compact ? 'project-card-compact' : ''}`}><div className="project-image">{imageUrl && <img src={imageUrl} alt={`${project.title} preview`} loading="lazy" />}<span className="status-chip"><Check size={13} /> Completed</span></div><div className={`project-body ${compact ? 'project-body-compact' : ''}`}><div><h3>{project.title}</h3>{!compact && <p>{project.description}</p>}</div>{compact ? <Link className="project-arrow" to={`/projects/${project.slug}`} aria-label={`View ${project.title}`}><ArrowRight size={18} /></Link> : <><div className="tag-row">{(project.technologies || []).slice(0, 4).map((technology) => <span key={technology}>{technology}</span>)}</div><Link to={`/projects/${project.slug}`}>View Case Study <ArrowRight size={15} /></Link></>}</div></article>
}
