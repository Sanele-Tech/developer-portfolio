import { ArrowUpRight, Code2 } from 'lucide-react'
import EmptyState from '../../components/EmptyState'
import PageHeading from '../../components/PageHeading'
import { useApi } from '../../hooks/useApi'
import { resolvePublicImageUrl } from '../../services/api'
import ImageSlideshow from '../../components/ImageSlideshow'
import ExpandableText from '../../components/ExpandableText'

export default function Hackathons() {
  const { data, loading, error, retry } = useApi('/api/achievements')
  const challenges = (data || []).filter((item) => item.cardType === 'CHALLENGE')

  return <section className="page-content public-feature-page">
    <PageHeading eyebrow="Build, learn, collaborate" title="Hackathons" />
    <p className="page-intro">Short, intensive challenges are a great place to turn ideas into working software and learn alongside other builders.</p>
    {error && <p className="public-data-error" role="alert">Challenge details could not be loaded. <button type="button" onClick={retry}>Try again</button></p>}
    {challenges.length > 0 && <div className="hackathon-grid">{challenges.map((challenge) => <article className="event-card" key={challenge.id}>
      <ImageSlideshow className="event-image-frame"
        images={(challenge.imageUrls?.length ? challenge.imageUrls : [challenge.imageUrl || '/images/portfolio/skills-challenge.svg'])
          .map((image) => resolvePublicImageUrl(image) || image)}
        alt={`${challenge.title} event`} />
      <div className="event-content">
        <span className="event-label">{challenge.issuer || 'Challenge participation'}</span>
        <h2>{challenge.title}</h2>
        {challenge.description && <ExpandableText className="event-description" text={challenge.description} />}
        <div className="event-meta"><span><Code2 size={15} /> Software developer</span>{challenge.achievedOn && <span><ArrowUpRight size={15} />{challenge.achievedOn}</span>}</div>
      </div>
    </article>)}</div>}
    {!loading && !error && challenges.length === 0 && <EmptyState title="Challenges are being added" message="Check back soon for challenge and hackathon updates." />}
  </section>
}
