import { useEffect, useState } from 'react'
import { ArrowLeft, Plus, Save, Trash2 } from 'lucide-react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { apiFetch } from '../../services/api'

const blankProject = () => ({
  title: '',
  slug: '',
  description: '',
  technologyNames: [],
  repositoryUrl: '',
  liveUrl: '',
  imageUrl: '',
  featured: false,
  sortOrder: 0,
  links: [],
  images: [],
})

export default function AdminProjectForm() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEditing = Boolean(id)
  const [project, setProject] = useState(blankProject)
  const [technologies, setTechnologies] = useState([])
  const [technologyDraft, setTechnologyDraft] = useState('')
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [uploadingMedia, setUploadingMedia] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    let active = true
    async function load() {
      setLoading(true)
      setError('')
      try {
        const [technologyList, existingProject] = await Promise.all([
          apiFetch('/api/admin/technologies'),
          isEditing ? apiFetch(`/api/admin/projects/${id}`) : Promise.resolve(null),
        ])
        if (!active) return
        setTechnologies(technologyList)
        if (existingProject) {
          setProject({
            title: existingProject.title || '',
            slug: existingProject.slug || '',
            description: existingProject.description || '',
            technologyNames: existingProject.technologyDetails?.map((item) => item.name) || [],
            repositoryUrl: existingProject.repositoryUrl || '',
            liveUrl: existingProject.liveUrl || '',
            imageUrl: existingProject.imageUrl || '',
            featured: Boolean(existingProject.featured),
            sortOrder: existingProject.sortOrder || 0,
            links: existingProject.links?.map((link) => ({ ...link })) || [],
            images: existingProject.images?.map((image) => ({ ...image })) || [],
          })
        }
      } catch (requestError) {
        if (active) setError(requestError.message)
      } finally {
        if (active) setLoading(false)
      }
    }
    load()
    return () => { active = false }
  }, [id, isEditing])

  function updateField(field, value) {
    setProject((current) => ({ ...current, [field]: value }))
  }

  function addTechnology(value = technologyDraft) {
    const name = value.trim()
    if (!name || project.technologyNames.some((technology) => technology.toLowerCase() === name.toLowerCase())) {
      setTechnologyDraft('')
      return
    }
    updateField('technologyNames', [...project.technologyNames, name])
    setTechnologyDraft('')
  }

  function removeTechnology(name) {
    updateField('technologyNames', project.technologyNames.filter((technology) => technology !== name))
  }

  function updateArrayItem(field, index, key, value) {
    setProject((current) => ({
      ...current,
      [field]: current[field].map((item, itemIndex) => itemIndex === index ? { ...item, [key]: value } : item),
    }))
  }

  function addLink() {
    updateField('links', [...project.links, { label: '', url: '', linkType: 'OTHER' }])
  }

  function addImage() {
    updateField('images', [...project.images, { imageUrl: '', altText: '', heading: '', description: '', sortOrder: project.images.length }])
  }

  function removeArrayItem(field, index) {
    updateField(field, project[field].filter((_, itemIndex) => itemIndex !== index))
  }

  async function uploadImage(file, target) {
    if (!file) return
    setError('')
    setUploadingMedia(target)
    const formData = new FormData()
    formData.append('file', file)
    try {
      const uploaded = await apiFetch('/api/admin/media?purpose=IMAGE', {
        method: 'POST',
        body: formData,
      })
      if (target === 'cover') {
        updateField('imageUrl', uploaded.url)
      } else {
        updateArrayItem('images', Number(target), 'imageUrl', uploaded.url)
      }
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setUploadingMedia('')
    }
  }

  function updateSlugFromTitle(value) {
    setProject((current) => ({
      ...current,
      title: value,
      ...(!isEditing && { slug: value.toLowerCase().trim().replace(/[^a-z0-9]+/g, '-').replace(/^-|-$/g, '') }),
    }))
  }

  async function submit(event) {
    event.preventDefault()
    setError('')
    setSaving(true)
    const payload = {
      ...project,
      sortOrder: Number(project.sortOrder),
      technologyNames: project.technologyNames,
      links: project.links.map((link) => ({ ...link, label: link.label.trim(), url: link.url.trim() })),
      images: project.images.map((image, index) => ({
        ...image,
        sortOrder: Number(image.sortOrder ?? index),
      })),
    }
    try {
      await apiFetch(isEditing ? `/api/admin/projects/${id}` : '/api/admin/projects', {
        method: isEditing ? 'PUT' : 'POST',
        body: JSON.stringify(payload),
      })
      navigate('/admin/projects', { replace: true, state: { notice: isEditing ? 'Project updated.' : 'Project created.' } })
    } catch (requestError) {
      const fieldErrors = requestError.details?.errors
      setError(Array.isArray(fieldErrors) && fieldErrors.length
        ? fieldErrors.map((item) => `${item.field}: ${item.message}`).join(' ')
        : requestError.message)
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <section className="admin-page"><p role="status">Loading project editor…</p></section>

  const filteredTechnologies = technologies.filter((technology) =>
    technology.name.toLowerCase().includes(technologyDraft.trim().toLowerCase())
    && !project.technologyNames.some((selected) => selected.toLowerCase() === technology.name.toLowerCase()))

  return <section className="admin-page">
    <div className="admin-page-heading admin-form-heading">
      <div><Link to="/admin/projects" className="admin-back-link"><ArrowLeft size={16} />Projects</Link><span className="eyebrow">Portfolio content</span><h1>{isEditing ? 'Edit project' : 'New project'}</h1><p>Project details, links, and gallery items.</p></div>
    </div>
    {error && <p className="admin-form-error" role="alert">{error}</p>}
    <form className="admin-editor-form" onSubmit={submit}>
      <section className="admin-form-section">
        <div className="admin-form-section-title"><h2>Project details</h2><p>Core information shown on the public project pages.</p></div>
        <div className="admin-form-grid">
          <label className="admin-field">Project title<input required maxLength="160" value={project.title} onChange={(event) => updateSlugFromTitle(event.target.value)} /></label>
          <label className="admin-field">URL slug<input required maxLength="180" pattern="[a-z0-9]+(?:-[a-z0-9]+)*" value={project.slug} onChange={(event) => updateField('slug', event.target.value)} /><small>Lowercase letters, numbers, and hyphens.</small></label>
          <label className="admin-field admin-field-full">Description<textarea required rows="5" value={project.description} onChange={(event) => updateField('description', event.target.value)} /></label>
          <label className="admin-field">Repository URL<input type="url" maxLength="500" value={project.repositoryUrl} onChange={(event) => updateField('repositoryUrl', event.target.value)} placeholder="https://" /></label>
          <label className="admin-field">Live project URL<input type="url" maxLength="500" value={project.liveUrl} onChange={(event) => updateField('liveUrl', event.target.value)} placeholder="https://" /></label>
          <label className="admin-field admin-field-full">Upload cover image<input type="file" accept="image/jpeg,image/png,image/webp,image/gif" disabled={Boolean(uploadingMedia)} onChange={(event) => { const file = event.target.files?.[0]; event.target.value = ''; uploadImage(file, 'cover') }} /><small>{uploadingMedia === 'cover' ? 'Uploading…' : 'JPEG, PNG, WebP, or GIF; maximum 10 MB.'}</small></label>
          <label className="admin-field admin-field-full">Cover image URL<input type="url" maxLength="500" value={project.imageUrl} onChange={(event) => updateField('imageUrl', event.target.value)} placeholder="https://" /></label>
          <div className="admin-field">
            <span className="admin-field-label">Technologies</span>
            <div className="admin-technology-entry">
              <input type="text" aria-label="Add a technology" maxLength="120" placeholder="Type a technology name…" value={technologyDraft} onChange={(event) => setTechnologyDraft(event.target.value)} onKeyDown={(event) => { if (event.key === 'Enter') { event.preventDefault(); addTechnology() } }} />
              <button type="button" className="admin-secondary-button" disabled={!technologyDraft.trim()} onClick={() => addTechnology()}>Add</button>
            </div>
            {filteredTechnologies.length > 0 && <div className="admin-technology-options" role="group" aria-label="Technology suggestions">
              {filteredTechnologies.map((technology) => <button key={technology.id} type="button" onClick={() => addTechnology(technology.name)}>+ {technology.name}</button>)}
            </div>}
            {project.technologyNames.length > 0 && <div className="admin-selected-technologies" aria-label="Selected technologies">
              {project.technologyNames.map((technology) =>
                <button key={technology} type="button" onClick={() => removeTechnology(technology)} aria-label={`Remove ${technology}`}>{technology}<span aria-hidden="true">×</span></button>)}
            </div>}
            <small>Type a technology and press Enter or Add. Select a suggestion, or remove a selected technology with ×.</small>
          </div>
          <label className="admin-field">Display order<input type="number" min="0" max="100000" value={project.sortOrder} onChange={(event) => updateField('sortOrder', event.target.value)} /></label>
          <label className="admin-checkbox"><input type="checkbox" checked={project.featured} onChange={(event) => updateField('featured', event.target.checked)} /><span><strong>Featured project</strong><small>Show this project in the featured section on the home page.</small></span></label>
        </div>
      </section>

      <section className="admin-form-section">
        <div className="admin-form-section-title admin-repeat-title"><div><h2>Project links</h2><p>Add repository, live demo, or other related links.</p></div><button type="button" className="admin-secondary-button" onClick={addLink}><Plus size={16} />Add link</button></div>
        {project.links.map((link, index) => <div className="admin-repeat-row" key={`link-${index}`}>
          <label className="admin-field">Label<input required maxLength="80" value={link.label} onChange={(event) => updateArrayItem('links', index, 'label', event.target.value)} /></label>
          <label className="admin-field">URL<input required type="url" maxLength="500" value={link.url} onChange={(event) => updateArrayItem('links', index, 'url', event.target.value)} /></label>
          <label className="admin-field">Type<select value={link.linkType} onChange={(event) => updateArrayItem('links', index, 'linkType', event.target.value)}><option value="OTHER">Other</option><option value="REPOSITORY">Repository</option><option value="LIVE">Live demo</option><option value="DOCUMENTATION">Documentation</option></select></label>
          <button type="button" className="admin-remove-button" aria-label={`Remove link ${index + 1}`} onClick={() => removeArrayItem('links', index)}><Trash2 size={17} /></button>
        </div>)}
      </section>

      <section className="admin-form-section">
        <div className="admin-form-section-title admin-repeat-title"><div><h2>Image gallery</h2><p>Optional gallery images displayed on the project detail page.</p></div><button type="button" className="admin-secondary-button" onClick={addImage}><Plus size={16} />Add image</button></div>
        {project.images.map((image, index) => <div className="admin-image-row" key={`image-${index}`}>
          <label className="admin-field admin-field-full">Upload gallery image<input type="file" accept="image/jpeg,image/png,image/webp,image/gif" disabled={Boolean(uploadingMedia)} onChange={(event) => { const file = event.target.files?.[0]; event.target.value = ''; uploadImage(file, String(index)) }} /><small>{uploadingMedia === String(index) ? 'Uploading…' : 'JPEG, PNG, WebP, or GIF; maximum 10 MB.'}</small></label>
          <label className="admin-field admin-field-full">Image URL<input required type="url" maxLength="500" value={image.imageUrl} onChange={(event) => updateArrayItem('images', index, 'imageUrl', event.target.value)} /></label>
          <label className="admin-field">Alt text<input maxLength="255" value={image.altText || ''} onChange={(event) => updateArrayItem('images', index, 'altText', event.target.value)} /></label>
          <label className="admin-field">Heading<input maxLength="160" value={image.heading || ''} onChange={(event) => updateArrayItem('images', index, 'heading', event.target.value)} /></label>
          <label className="admin-field admin-field-full">Description<textarea rows="3" value={image.description || ''} onChange={(event) => updateArrayItem('images', index, 'description', event.target.value)} /></label>
          <label className="admin-field">Display order<input type="number" min="0" max="100000" value={image.sortOrder ?? index} onChange={(event) => updateArrayItem('images', index, 'sortOrder', event.target.value)} /></label>
          <button type="button" className="admin-remove-button" aria-label={`Remove image ${index + 1}`} onClick={() => removeArrayItem('images', index)}><Trash2 size={17} /></button>
        </div>)}
      </section>

      <div className="admin-form-actions"><Link to="/admin/projects" className="admin-cancel-button">Cancel</Link><button className="admin-primary-button" type="submit" disabled={saving || Boolean(uploadingMedia)}><Save size={17} />{saving ? 'Saving…' : 'Save project'}</button></div>
    </form>
  </section>
}
