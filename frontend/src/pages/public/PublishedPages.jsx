import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import PageHeading from '../../components/PageHeading'
import { apiFetch } from '../../services/api'

export default function PublishedPages() {
  const [state, setState] = useState({ status: 'loading', pages: [] })

  useEffect(() => {
    let active = true
    apiFetch('/api/pages')
      .then((pages) => {
        if (active) setState({ status: 'loaded', pages })
      })
      .catch(() => {
        if (active) setState({ status: 'error', pages: [] })
      })

    return () => { active = false }
  }, [])

  return <section className="page-content published-pages">
    <PageHeading eyebrow="From the CMS" title="More to explore" />
    {state.status === 'loading' && <p className="page-intro" aria-live="polite">Loading pages…</p>}
    {state.status === 'error' && <p className="page-intro" role="alert">We couldn’t load the pages. Please try again in a moment.</p>}
    {state.status === 'loaded' && state.pages.length === 0 && <p className="page-intro">There are no published pages yet. Please check back soon.</p>}
    {state.status === 'loaded' && state.pages.length > 0 && <div className="published-page-list">
      {state.pages.map((page) => <article className="published-page-card" key={page.slug}>
        <span className="eyebrow">Article</span>
        <h2><Link to={`/pages/${encodeURIComponent(page.slug)}`}>{page.title}</Link></h2>
        {page.excerpt && <p>{page.excerpt}</p>}
        <Link className="published-page-link" to={`/pages/${encodeURIComponent(page.slug)}`}>Read page <span aria-hidden="true">→</span></Link>
      </article>)}
    </div>}
  </section>
}
