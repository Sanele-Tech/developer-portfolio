import { useEffect, useState } from 'react'
import { ArrowLeft, Save } from 'lucide-react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { apiFetch } from '../../services/api'
import AdminImageGalleryField from './AdminImageGalleryField'

const blankEducation = {
  institution: '',
  qualification: '',
  description: '',
  startDate: '',
  endDate: '',
  coreModules: [],
  experience: '',
  imageUrls: [],
  sortOrder: 0,
}

export default function AdminEducationForm() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEditing = Boolean(id)
  const [education, setEducation] = useState(blankEducation)
  const [coreModulesText, setCoreModulesText] = useState('')
  const [loading, setLoading] = useState(isEditing)
  const [saving, setSaving] = useState(false)
  const [uploadingImages, setUploadingImages] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!isEditing) return
    let active = true
    apiFetch(`/api/admin/education/${id}`)
      .then((item) => {
        if (!active) return
        setEducation({
          ...blankEducation,
          ...item,
          startDate: item.startDate || '',
          endDate: item.endDate || '',
          coreModules: item.coreModules || [],
          experience: item.experience || '',
          imageUrls: item.imageUrls || [],
        })
        setCoreModulesText((item.coreModules || []).join('\n'))
      })
      .catch((requestError) => { if (active) setError(requestError.message) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [id, isEditing])

  function updateField(field, value) {
    setEducation((current) => ({ ...current, [field]: value }))
  }

  async function submit(event) {
    event.preventDefault()
    setError('')
    setSaving(true)
    const payload = {
      ...education,
      institution: education.institution.trim(),
      qualification: education.qualification.trim(),
      description: education.description.trim() || null,
      startDate: education.startDate || null,
      endDate: education.endDate || null,
      coreModules: coreModulesText.split(/\r?\n/).map((module) => module.trim()).filter(Boolean),
      experience: education.experience.trim() || null,
      imageUrls: education.imageUrls,
      sortOrder: Number(education.sortOrder),
    }
    try {
      await apiFetch(isEditing ? `/api/admin/education/${id}` : '/api/admin/education', {
        method: isEditing ? 'PUT' : 'POST',
        body: JSON.stringify(payload),
      })
      navigate('/admin/education', {
        replace: true,
        state: { notice: isEditing ? 'Education updated.' : 'Education added.' },
      })
    } catch (requestError) {
      const fieldErrors = requestError.details?.errors
      setError(Array.isArray(fieldErrors) && fieldErrors.length
        ? fieldErrors.map((item) => `${item.field}: ${item.message}`).join(' ')
        : requestError.message)
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <section className="admin-page"><p role="status">Loading education editor…</p></section>

  return <section className="admin-page">
    <div className="admin-page-heading admin-form-heading">
      <div><Link to="/admin/education" className="admin-back-link"><ArrowLeft size={16} />Education</Link><span className="eyebrow">Portfolio content</span><h1>{isEditing ? 'Edit education' : 'Add education'}</h1><p>Enter the qualification and institution details.</p></div>
    </div>
    {error && <p className="admin-form-error" role="alert">{error}</p>}
    <form className="admin-editor-form" onSubmit={submit}>
      <section className="admin-form-section">
        <div className="admin-form-section-title"><h2>Education details</h2><p>This information appears on your public Education page.</p></div>
        <div className="admin-form-grid">
          <label className="admin-field">Institution<input required maxLength="200" value={education.institution} onChange={(event) => updateField('institution', event.target.value)} /></label>
          <label className="admin-field">Course / qualification<input required maxLength="200" value={education.qualification} onChange={(event) => updateField('qualification', event.target.value)} /></label>
          <label className="admin-field admin-field-full">Description <span className="admin-optional-label">(optional)</span><textarea rows="3" value={education.description || ''} onChange={(event) => updateField('description', event.target.value)} /></label>
          <label className="admin-field admin-field-full">Core modules <span className="admin-optional-label">(one per line)</span><textarea rows="4" value={coreModulesText} onChange={(event) => setCoreModulesText(event.target.value)} placeholder={'Software Engineering\nDatabase Systems\nWeb Development'} /></label>
          <label className="admin-field admin-field-full">Experience / highlights <span className="admin-optional-label">(optional)</span><textarea rows="5" maxLength="10000" value={education.experience || ''} onChange={(event) => updateField('experience', event.target.value)} placeholder="What did you learn, build, take part in, or enjoy during this period?" /></label>
          <label className="admin-field">Start date <span className="admin-optional-label">(optional)</span><input type="date" value={education.startDate || ''} onChange={(event) => updateField('startDate', event.target.value)} max={education.endDate || undefined} /></label>
          <label className="admin-field">End date <span className="admin-optional-label">(optional)</span><input type="date" value={education.endDate || ''} onChange={(event) => updateField('endDate', event.target.value)} min={education.startDate || undefined} /></label>
          <AdminImageGalleryField images={education.imageUrls} onChange={(images) => updateField('imageUrls', images)}
            onUploadingChange={setUploadingImages} purpose="Education experience" />
          <label className="admin-field">Display order<input type="number" min="0" max="100000" value={education.sortOrder} onChange={(event) => updateField('sortOrder', event.target.value)} /></label>
        </div>
      </section>
      <div className="admin-form-actions"><Link to="/admin/education" className="admin-cancel-button">Cancel</Link><button className="admin-primary-button" type="submit" disabled={saving || uploadingImages}><Save size={17} />{saving ? 'Saving…' : uploadingImages ? 'Uploading images…' : 'Save education'}</button></div>
    </form>
  </section>
}
