import { Download, Eye } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import CvDocumentPreview from './CvDocumentPreview'
import { apiFetch, resolvePublicAssetUrl } from '../services/api'
import { recordAnalyticsEvent } from '../services/analytics'

export default function CvLink() {
  const [state, setState] = useState({ status: 'loading', document: null })
  const [previewOpen, setPreviewOpen] = useState(false)
  const [preview, setPreview] = useState({ status: 'idle', url: null })
  const dialogRef = useRef(null)
  const cvUrl = state.status === 'available' ? resolvePublicAssetUrl(state.document.storageUrl) : null

  useEffect(() => {
    let active = true
    apiFetch('/api/documents/cv/current')
      .then((document) => {
        if (active) setState({ status: 'available', document })
      })
      .catch((error) => {
        if (active) setState({ status: error.status === 404 ? 'not-configured' : 'unavailable', document: null })
      })

    return () => { active = false }
  }, [])

  useEffect(() => {
    const dialog = dialogRef.current
    if (previewOpen && dialog && !dialog.open) dialog.showModal()
    if (!previewOpen && dialog?.open) dialog.close()
  }, [previewOpen])

  useEffect(() => {
    if (!previewOpen || !cvUrl) return undefined
    let active = true
    let objectUrl

    fetch(cvUrl, { credentials: 'include' })
      .then((response) => {
        if (!response.ok) throw new Error(`CV preview request failed (${response.status}).`)
        return response.blob()
      })
      .then((blob) => {
        if (!blob.size) throw new Error('The CV preview is empty.')
        objectUrl = URL.createObjectURL(blob)
        if (active) setPreview({ status: 'ready', url: objectUrl })
        else URL.revokeObjectURL(objectUrl)
      })
      .catch((error) => {
        console.error('Unable to load the CV preview.', error)
        if (active) setPreview({ status: 'failed', url: null })
      })

    return () => {
      active = false
      if (objectUrl) URL.revokeObjectURL(objectUrl)
    }
  }, [previewOpen, cvUrl])

  if (state.status === 'loading') {
    return <span className="download-button" role="status">Loading CV…</span>
  }

  if (state.status === 'not-configured') {
    return <Link className="download-button" to="/contact" title="No CV is currently listed">
      Request CV
    </Link>
  }

  if (state.status === 'unavailable') {
    return <span className="download-button cv-link-unavailable" aria-disabled="true" title="CV link is currently unavailable">
      CV unavailable
    </span>
  }

  if (!cvUrl) {
    return <span className="download-button cv-link-unavailable" aria-disabled="true" title="CV link is invalid">
      CV unavailable
    </span>
  }

  const downloadName = state.document.mimeType === 'application/pdf' && !/\.pdf$/i.test(state.document.name)
    ? `${state.document.name}.pdf`
    : state.document.name

  return <>
    <button className="download-button" type="button" aria-haspopup="dialog" aria-label={`Preview ${state.document.name}`}
      onClick={() => {
        void recordAnalyticsEvent('resume_view')
        setPreview({ status: 'loading', url: null })
        setPreviewOpen(true)
      }}>
      <Eye size={16} />View CV
    </button>
    <dialog ref={dialogRef} className="cv-preview-dialog" aria-labelledby="cv-preview-title"
      onCancel={() => setPreviewOpen(false)} onClose={() => setPreviewOpen(false)}>
      <div className="cv-preview-header">
        <div><span className="eyebrow">Curriculum vitae</span><h2 id="cv-preview-title">{state.document.name}</h2></div>
        <button type="button" className="cv-preview-close" aria-label="Close CV preview" onClick={() => setPreviewOpen(false)}>×</button>
      </div>
      {preview.status === 'failed'
        ? <div className="cv-preview-body"><div className="cv-preview-status" role="alert">
          <p>The CV preview could not be loaded. You can still open or download the original file.</p>
          <a href={cvUrl} target="_blank" rel="noreferrer">Open CV in a new tab</a>
        </div></div>
        : previewOpen && preview.status === 'ready' && <CvDocumentPreview url={preview.url} name={state.document.name} />}
      <div className="cv-preview-footer">
        <p>Preview the CV here, or download a copy to your device.</p>
        <a className={`button button-dark${preview.status !== 'ready' ? ' is-disabled' : ''}`}
          href={preview.url || cvUrl} download={downloadName} aria-disabled={preview.status !== 'ready'}
          onClick={(event) => {
            if (preview.status !== 'ready') {
              event.preventDefault()
              return
            }
            void recordAnalyticsEvent('resume_download')
          }}>
          <Download size={16} />Download CV
        </a>
      </div>
    </dialog>
  </>
}
