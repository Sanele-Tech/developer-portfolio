import { BriefcaseBusiness, ExternalLink, Trophy, Users } from 'lucide-react'
import { useApi } from '../../hooks/useApi'
import PageHeading from '../../components/PageHeading'
import EmptyState from '../../components/EmptyState'
import { resolvePublicImageUrl } from '../../services/api'
import ImageSlideshow from '../../components/ImageSlideshow'
import ExpandableText from '../../components/ExpandableText'

export default function BeyondCode() {
  const { data, loading, error, retry } = useApi('/api/achievements')
  const highlights = (data || []).filter((item) => item.cardType && item.cardType !== 'CERTIFICATION')

  return <section className="page-content public-feature-page">
    <PageHeading eyebrow="A little more about me" title="Beyond the code" />
    <p className="page-intro">Building software is one part of what I do. I also enjoy creating opportunities, organizing work, and helping people move toward their next step.</p>
    {error && <p className="public-data-error" role="alert">Portfolio highlights could not be loaded. <button type="button" onClick={retry}>Try again</button></p>}
    {highlights.length > 0 && <div className="beyond-grid">
      {highlights.map((item) => <article className="beyond-card" key={item.id}>
        <ImageSlideshow className="beyond-image-frame"
          images={(item.imageUrls?.length ? item.imageUrls : [item.imageUrl || fallbackImage(item.cardType)])
            .map((image) => resolvePublicImageUrl(image) || image)}
          alt={`${item.title} highlight`} />
        <div className="beyond-card-content">
          <span className="beyond-icon">{item.cardType === 'BUSINESS' ? <BriefcaseBusiness size={20} /> : item.cardType === 'CHALLENGE' ? <Trophy size={20} /> : <Users size={20} />}</span>
          <p className="event-label">{item.issuer || item.cardType}</p>
          <h2>{item.title}</h2>
          {item.description && <ExpandableText className="beyond-description" text={item.description} />}
          {item.url && <a href={item.url} target="_blank" rel="noreferrer">View details <ExternalLink size={15} /></a>}
        </div>
      </article>)}
    </div>}
    {!loading && !error && highlights.length === 0 && <EmptyState title="Portfolio highlights are being added" message="Check back soon for business, community, and challenge updates." />}
    <p className="beyond-footnote">The details here focus on work and involvement I’ve shared publicly; I’ll add more personal interests and updates as they develop.</p>
  </section>
}

function fallbackImage(cardType) {
  if (cardType === 'BUSINESS') return '/images/portfolio/varsity-apply.svg'
  if (cardType === 'COMMUNITY') return '/images/portfolio/circuit-breakers.svg'
  if (cardType === 'CHALLENGE') return '/images/portfolio/skills-challenge.svg'
  return '/images/portfolio/credential-data.svg'
}
