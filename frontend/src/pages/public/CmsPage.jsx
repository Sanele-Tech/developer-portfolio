import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import PageHeading from '../../components/PageHeading'
import { apiFetch } from '../../services/api'

export default function CmsPage() {
  const { slug } = useParams()
  const [state, setState] = useState({ slug: null, status: 'loading', page: null })

  useEffect(() => {
    let active = true

    apiFetch(`/api/pages/${encodeURIComponent(slug)}`)
      .then((page) => {
        if (active) setState({ slug, status: 'loaded', page })
      })
      .catch((error) => {
        if (active) setState({ slug, status: error.status === 404 ? 'not-found' : 'error', page: null })
      })

    return () => { active = false }
  }, [slug])

  useEffect(() => {
    if (state.status === 'loaded') document.title = `${state.page.title} | Sanele Dlomo`
  }, [state])

  if (state.slug !== slug || state.status === 'loading') {
    return <section className="page-content cms-page-state" aria-live="polite">Loading page…</section>
  }

  if (state.status === 'not-found') {
    return <section className="page-content cms-page-state">
      <PageHeading eyebrow="404" title="Page not found" />
      <p className="page-intro">This page may not exist or may not be published.</p>
      <Link className="button button-dark" to="/">Return home</Link>
    </section>
  }

  if (state.status === 'error') {
    return <section className="page-content cms-page-state" role="alert">
      <PageHeading eyebrow="Unavailable" title="We couldn’t load this page" />
      <p className="page-intro">Please try again in a moment.</p>
    </section>
  }

  return <article className="page-content cms-page">
    <PageHeading eyebrow="Page" title={state.page.title} />
    {state.page.excerpt && <p className="page-intro">{state.page.excerpt}</p>}
    <div className="cms-page-body">{state.page.content}</div>
    <Link className="cms-page-index-link" to="/pages">Browse published pages</Link>
  </article>
}
