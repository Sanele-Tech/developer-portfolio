import { useEffect, useRef, useState } from 'react'

export default function CvDocumentPreview({ url, name }) {
  const viewerRef = useRef(null)
  const canvasesRef = useRef([])
  const [document, setDocument] = useState(null)
  const [pageCount, setPageCount] = useState(0)
  const [viewerSize, setViewerSize] = useState({ width: 0, height: 0 })
  const [zoom, setZoom] = useState(1)
  const [status, setStatus] = useState('loading')

  useEffect(() => {
    let active = true
    let loadingTask
    import('pdfjs-dist')
      .then((pdfjs) => {
        if (!active) return null
        pdfjs.GlobalWorkerOptions.workerSrc = new URL('pdfjs-dist/build/pdf.worker.min.mjs', import.meta.url).toString()
        loadingTask = pdfjs.getDocument({ url })
        return loadingTask.promise
      })
      .then((pdf) => {
        if (!pdf) return
        if (!active) {
          void pdf.destroy()
          return
        }
        setDocument(pdf)
        setPageCount(pdf.numPages)
        setStatus('ready')
      })
      .catch((error) => {
        if (active) {
          console.error('Unable to render the CV document.', error)
          setStatus('failed')
        }
      })

    return () => {
      active = false
      if (loadingTask) void loadingTask.destroy()
    }
  }, [url])

  useEffect(() => {
    const viewer = viewerRef.current
    if (!viewer) return undefined
    const measureViewer = () => {
      const { width, height } = viewer.getBoundingClientRect()
      setViewerSize({ width, height })
    }
    const observer = new ResizeObserver(([entry]) => {
      setViewerSize({
        width: entry.contentRect.width,
        height: entry.contentRect.height,
      })
    })
    observer.observe(viewer)
    const frame = window.requestAnimationFrame(measureViewer)
    const timeout = window.setTimeout(measureViewer, 120)
    window.addEventListener('resize', measureViewer)
    return () => {
      window.cancelAnimationFrame(frame)
      window.clearTimeout(timeout)
      window.removeEventListener('resize', measureViewer)
      observer.disconnect()
    }
  }, [])

  useEffect(() => {
    if (!document || !pageCount || !viewerSize.width || !viewerSize.height) return undefined
    let active = true
    const renderTasks = []

    async function renderPages() {
      try {
        await Promise.all(Array.from({ length: pageCount }, async (_, index) => {
          const pageNumber = index + 1
          const canvas = canvasesRef.current[pageNumber - 1]
          if (!canvas) return
          const page = await document.getPage(pageNumber)
          if (!active) return

          const originalViewport = page.getViewport({ scale: 1 })
          const widthScale = (viewerSize.width - 24) / originalViewport.width
          const fitScale = window.innerWidth <= 600
            ? Math.min(widthScale, (viewerSize.height - 24) / originalViewport.height)
            : widthScale
          const viewport = page.getViewport({ scale: Math.max(0.1, fitScale * zoom) })
          const pixelRatio = Math.min(window.devicePixelRatio || 1, 2)
          const context = canvas.getContext('2d')
          canvas.width = Math.ceil(viewport.width * pixelRatio)
          canvas.height = Math.ceil(viewport.height * pixelRatio)
          canvas.style.width = `${viewport.width}px`
          canvas.style.height = `${viewport.height}px`

          const task = page.render({
            canvasContext: context,
            viewport,
            transform: pixelRatio === 1 ? null : [pixelRatio, 0, 0, pixelRatio, 0, 0],
          })
          renderTasks.push(task)
          await task.promise
        }))
      } catch (error) {
        if (active && error.name !== 'RenderingCancelledException') {
          console.error('Unable to render a CV page.', error)
          setStatus('failed')
        }
      }
    }

    void renderPages()
    return () => {
      active = false
      renderTasks.forEach((task) => task.cancel())
    }
  }, [document, pageCount, viewerSize, zoom])

  return <div className="cv-document-viewer">
    <div className="cv-preview-body" ref={viewerRef} aria-label={`${name} document preview`}>
      {status === 'loading' && <p className="cv-preview-status" role="status">Loading CV preview…</p>}
      {status === 'failed' && <div className="cv-preview-status" role="alert">
        <p>The CV preview could not be displayed. You can still open or download the original file.</p>
        <a href={url} target="_blank" rel="noreferrer">Open CV in a new tab</a>
      </div>}
      {status === 'ready' && <div className="cv-page-stack">
        {Array.from({ length: pageCount }, (_, index) => <div className="cv-page" key={index}>
          <canvas ref={(canvas) => { canvasesRef.current[index] = canvas }} aria-label={`${name}, page ${index + 1}`} />
        </div>)}
      </div>}
    </div>
    {status === 'ready' && <div className="cv-zoom-controls" aria-label="CV zoom controls">
      <button type="button" aria-label="Zoom out" disabled={zoom <= 0.5} onClick={() => setZoom((value) => Math.max(0.5, value - 0.25))}>−</button>
      <span>{Math.round(zoom * 100)}%</span>
      <button type="button" aria-label="Zoom in" disabled={zoom >= 3} onClick={() => setZoom((value) => Math.min(3, value + 0.25))}>+</button>
      <button type="button" onClick={() => setZoom(1)}>
        <span className="cv-fit-page-label">Fit page</span>
        <span className="cv-fit-width-label">Fit width</span>
      </button>
    </div>}
  </div>
}
