import { useEffect, useState } from 'react'
import { ArrowLeft, Save } from 'lucide-react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { apiFetch } from '../../services/api'

const blankSkill = {
  name: '',
  category: '',
  proficiency: '',
  sortOrder: 0,
}

export default function AdminSkillForm() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEditing = Boolean(id)
  const [skill, setSkill] = useState(blankSkill)
  const [loading, setLoading] = useState(isEditing)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!isEditing) return
    let active = true
    apiFetch(`/api/admin/skills/${id}`)
      .then((item) => {
        if (active) setSkill({ ...blankSkill, ...item, category: item.category || '', proficiency: item.proficiency ?? '' })
      })
      .catch((requestError) => { if (active) setError(requestError.message) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [id, isEditing])

  function updateField(field, value) {
    setSkill((current) => ({ ...current, [field]: value }))
  }

  async function submit(event) {
    event.preventDefault()
    setError('')
    setSaving(true)
    const payload = {
      ...skill,
      name: skill.name.trim(),
      category: skill.category.trim() || null,
      proficiency: skill.proficiency === '' ? null : Number(skill.proficiency),
      sortOrder: Number(skill.sortOrder),
    }
    try {
      await apiFetch(isEditing ? `/api/admin/skills/${id}` : '/api/admin/skills', {
        method: isEditing ? 'PUT' : 'POST',
        body: JSON.stringify(payload),
      })
      navigate('/admin/skills', {
        replace: true,
        state: { notice: isEditing ? 'Skill updated.' : 'Skill added.' },
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

  if (loading) return <section className="admin-page"><p role="status">Loading skill editor…</p></section>

  return <section className="admin-page">
    <div className="admin-page-heading admin-form-heading">
      <div><Link to="/admin/skills" className="admin-back-link"><ArrowLeft size={16} />Skills</Link><span className="eyebrow">Portfolio content</span><h1>{isEditing ? 'Edit skill' : 'Add skill'}</h1><p>Enter a skill and optionally group it by category.</p></div>
    </div>
    {error && <p className="admin-form-error" role="alert">{error}</p>}
    <form className="admin-editor-form" onSubmit={submit}>
      <section className="admin-form-section">
        <div className="admin-form-section-title"><h2>Skill details</h2><p>This information appears on your public Skills page.</p></div>
        <div className="admin-form-grid">
          <label className="admin-field">Skill name<input required maxLength="120" value={skill.name} onChange={(event) => updateField('name', event.target.value)} /></label>
          <label className="admin-field">Category <span className="admin-optional-label">(optional)</span><input maxLength="120" value={skill.category} onChange={(event) => updateField('category', event.target.value)} placeholder="e.g. Frontend" /></label>
          <label className="admin-field">Proficiency <span className="admin-optional-label">(optional)</span><input type="number" min="0" max="100" step="1" value={skill.proficiency} onChange={(event) => updateField('proficiency', event.target.value)} placeholder="0–100" /></label>
          <label className="admin-field">Display order<input type="number" min="0" max="100000" value={skill.sortOrder} onChange={(event) => updateField('sortOrder', event.target.value)} /></label>
        </div>
      </section>
      <div className="admin-form-actions"><Link to="/admin/skills" className="admin-cancel-button">Cancel</Link><button className="admin-primary-button" type="submit" disabled={saving}><Save size={17} />{saving ? 'Saving…' : 'Save skill'}</button></div>
    </form>
  </section>
}
