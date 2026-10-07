import { useState } from 'react'
import { ImagePlus, Trash2 } from 'lucide-react'
import { apiFetch, resolvePublicAssetUrl } from '../../services/api'

const MAX_IMAGES = 10

export default function AdminImageGalleryField({ images, onChange, onUploadingChange, purpose = 'Portfolio image' }) {
  const imageUrls = Array.isArray(images) ? images : []
  const [uploading, setUploading] = useState(false)
  const [error, setError] = useState('')
  const [urlsDraft, setUrlsDraft] = useState('')
  const [editingUrls, setEditingUrls] = useState(false)

  async function uploadFiles(files) {
    if (!files.length) return
    const availableSlots = MAX_IMAGES - imageUrls.length
    if (files.length > availableSlots) {
      setError(`You can add up to ${MAX_IMAGES} images per ${purpose.toLowerCase()}. Remove an image before adding more.`)
      return
    }
    setError('')
    setUploading(true)
    onUploadingChange?.(true)
    let nextImages = imageUrls
    try {
      for (const file of files) {
        const formData = new FormData()
        formData.append('file', file)
        const uploaded = await apiFetch('/api/admin/media?purpose=IMAGE', { method: 'POST', body: formData })
        if (!uploaded.url) throw new Error(`The upload for ${file.name} did not return an image URL.`)
        nextImages = [...nextImages, uploaded.url]
        onChange(nextImages)
      }
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setUploading(false)
      onUploadingChange?.(false)
    }
  }

  function updateUrls(value) {
    const next = value.split(/\r?\n/).map((url) => url.trim()).filter(Boolean)
    if (next.length > MAX_IMAGES) {
      setError(`You can add up to ${MAX_IMAGES} images per ${purpose.toLowerCase()}.`)
      return
    }
    setError('')
    onChange(next)
  }

  return <div className="admin-image-gallery-field">
    <label className="admin-field admin-field-full">
      {purpose} images <span className="admin-optional-label">(optional, up to {MAX_IMAGES})</span>
      <input type="file" accept="image/jpeg,image/png,image/webp,image/gif" multiple disabled={uploading || imageUrls.length >= MAX_IMAGES}
        onChange={(event) => {
          const files = [...(event.target.files || [])]
          event.target.value = ''
          void uploadFiles(files)
        }} />
      <small>{uploading ? 'Uploading selected images…' : 'Select several images to create an automatic slideshow, or paste one image URL per line.'}</small>
    </label>
    <label className="admin-field admin-field-full">
      Image URLs
      <textarea rows="3" value={editingUrls ? urlsDraft : imageUrls.join('\n')}
        onFocus={(event) => {
          setUrlsDraft(event.currentTarget.value)
          setEditingUrls(true)
        }}
        onChange={(event) => {
          setUrlsDraft(event.target.value)
          updateUrls(event.target.value)
        }}
        onBlur={() => {
          setEditingUrls(false)
        }}
        placeholder="https://example.com/image-1.jpg&#10;https://example.com/image-2.jpg" />
    </label>
    {error && <p className="admin-form-error admin-field-full" role="alert">{error}</p>}
    {imageUrls.length > 0 && <div className="admin-image-gallery-preview admin-field-full" aria-label={`${purpose} image previews`}>
      {imageUrls.map((url, index) => <figure key={`${url}-${index}`}>
        <img src={resolvePublicAssetUrl(url) || url} alt={`${purpose} ${index + 1}`} />
        <figcaption><span>Image {index + 1}</span><button type="button" aria-label={`Remove image ${index + 1}`}
          onClick={() => onChange(imageUrls.filter((_, imageIndex) => imageIndex !== index))}><Trash2 size={14} /></button></figcaption>
      </figure>)}
      <span className="admin-image-gallery-count"><ImagePlus size={14} />{imageUrls.length}/{MAX_IMAGES}</span>
    </div>}
  </div>
}
