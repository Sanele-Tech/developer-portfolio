import { useEffect, useState } from 'react'
import { ArrowLeft, Save } from 'lucide-react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { apiFetch } from '../../services/api'

const blankExperience = {
  jobTitle: '',
  company: '',
  description: '',
  startDate: '',
  endDate: '',
  currentRole: false,
  sortOrder: 0,
}

export default function AdminExperienceForm() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEditing = Boolean(id)
  const [experience, setExperience] = useState(blankExperience)
  const [loading, setLoading] = useState(isEditing)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!isEditing) return
    let active = true
    apiFetch(`/api/admin/experience/${id}`)
      .then((item) => { if (active) setExperience({ ...blankExperience, ...item, endDate: item.endDate || '' }) })
      .catch((requestError) => { if (active) setError(requestError.message) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [id, isEditing])

  function updateField(field, value) {
    setExperience((current) => ({ ...current, [field]: value }))
  }

  async function submit(event) {
    event.preventDefault()
    setError('')
    setSaving(true)
    const payload = {
      ...experience,
      jobTitle: experience.jobTitle.trim(),
      company: experience.company.trim(),
      description: experience.description.trim(),
      endDate: experience.currentRole || !experience.endDate ? null : experience.endDate,
      sortOrder: Number(experience.sortOrder),
    }
    try {
      await apiFetch(isEditing ? `/api/admin/experience/${id}` : '/api/admin/experience', {
        method: isEditing ? 'PUT' : 'POST',
        body: JSON.stringify(payload),
      })
      navigate('/admin/experience', {
        replace: true,
        state: { notice: isEditing ? 'Experience updated.' : 'Experience added.' },
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

  if (loading) return <section className="admin-page"><p role="status">Loading experience editor…</p></section>

  return <section className="admin-page">
    <div className="admin-page-heading admin-form-heading">
      <div><Link to="/admin/experience" className="admin-back-link"><ArrowLeft size={16} />Experience</Link><span className="eyebrow">Portfolio content</span><h1>{isEditing ? 'Edit experience' : 'Add experience'}</h1><p>Enter a role and the dates you held it.</p></div>
    </div>
    {error && <p className="admin-form-error" role="alert">{error}</p>}
    <form className="admin-editor-form" onSubmit={submit}>
      <section className="admin-form-section">
        <div className="admin-form-section-title"><h2>Role details</h2><p>This information appears on your public Experience page.</p></div>
        <div className="admin-form-grid">
          <label className="admin-field">Job title<input required maxLength="160" value={experience.jobTitle} onChange={(event) => updateField('jobTitle', event.target.value)} /></label>
          <label className="admin-field">Company<input required maxLength="160" value={experience.company} onChange={(event) => updateField('company', event.target.value)} /></label>
          <label className="admin-field admin-field-full">Description<textarea required rows="5" value={experience.description} onChange={(event) => updateField('description', event.target.value)} /></label>
          <label className="admin-field">Start date<input required type="date" value={experience.startDate || ''} onChange={(event) => updateField('startDate', event.target.value)} /></label>
          <label className="admin-field">End date<input type="date" disabled={experience.currentRole} value={experience.currentRole ? '' : experience.endDate || ''} onChange={(event) => updateField('endDate', event.target.value)} min={experience.startDate || undefined} /></label>
          <label className="admin-field">Display order<input type="number" min="0" max="100000" value={experience.sortOrder} onChange={(event) => updateField('sortOrder', event.target.value)} /></label>
          <label className="admin-checkbox"><input type="checkbox" checked={experience.currentRole} onChange={(event) => updateField('currentRole', event.target.checked)} /><span><strong>Current role</strong><small>Hide the end date and show this position as ongoing.</small></span></label>
        </div>
      </section>
      <div className="admin-form-actions"><Link to="/admin/experience" className="admin-cancel-button">Cancel</Link><button className="admin-primary-button" type="submit" disabled={saving}><Save size={17} />{saving ? 'Saving…' : 'Save experience'}</button></div>
    </form>
  </section>
}
