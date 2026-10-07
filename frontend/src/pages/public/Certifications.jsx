import { Award, ExternalLink } from 'lucide-react'
import EmptyState from '../../components/EmptyState'
import PageHeading from '../../components/PageHeading'
import { useApi } from '../../hooks/useApi'
import { resolvePublicImageUrl } from '../../services/api'
import ImageSlideshow from '../../components/ImageSlideshow'

export default function Certifications() {
  const { data, loading, error, retry } = useApi('/api/achievements')
  const credentials = (data || []).filter((item) => !item.cardType || item.cardType === 'CERTIFICATION')

  return <section className="page-content public-feature-page" aria-busy={loading}>
    <PageHeading eyebrow="Learning in practice" title="Certifications" />
    <p className="page-intro">Verified learning, skills, and milestones that support the work I do.</p>
    {error && <p className="public-data-error" role="alert">Credentials could not be loaded. <button type="button" onClick={retry}>Try again</button></p>}
    {loading && !data && <p className="page-intro" role="status">Loading credentials…</p>}
    {credentials.length > 0 && <div className="credential-grid">
      {credentials.map((credential) => <article className="credential-card" key={credential.url || credential.id}>
        <ImageSlideshow className="credential-image-frame"
          images={(credential.imageUrls?.length ? credential.imageUrls : [credential.imageUrl || '/images/portfolio/credential-data.svg'])
            .map((image) => resolvePublicImageUrl(image) || image)}
          alt={`${credential.title} certificate`} />
        <div className="credential-content">
          <span className="credential-icon"><Award size={19} /></span>
          <p className="credential-issuer">{credential.issuer || 'Professional development'}{credential.achievedOn && ` · ${formatDate(credential.achievedOn)}`}</p>
          <h2>{credential.title}</h2>
          {credential.description && <p className="credential-description">{credential.description}</p>}
          {credential.url && <a href={credential.url} target="_blank" rel="noreferrer">Verify credential <ExternalLink size={15} /></a>}
        </div>
      </article>)}
    </div>}
    {!loading && !error && credentials.length === 0 && <EmptyState title="Credentials are being added" message="Check back soon for certificates and verification links." />}
  </section>
}

function formatDate(value) {
  return new Intl.DateTimeFormat(undefined, { month: 'short', year: 'numeric', timeZone: 'UTC' })
    .format(new Date(`${value}T00:00:00Z`))
}
