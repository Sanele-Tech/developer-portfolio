import { useEffect, useState } from 'react'
import { ArrowLeft, Save } from 'lucide-react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { apiFetch } from '../../services/api'
import AdminImageGalleryField from './AdminImageGalleryField'

const blankAchievement = {
  title: '',
  cardType: 'CERTIFICATION',
  issuer: '',
  description: '',
  achievedOn: '',
  url: '',
  imageUrl: '',
  imageUrls: [],
  sortOrder: 0,
}

export default function AdminAchievementForm() {
  const { id } = useParams()
  const navigate = useNavigate()
  const isEditing = Boolean(id)
  const [achievement, setAchievement] = useState(blankAchievement)
  const [loading, setLoading] = useState(isEditing)
  const [saving, setSaving] = useState(false)
  const [uploadingImages, setUploadingImages] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!isEditing) return
    let active = true
    apiFetch(`/api/admin/achievements/${id}`)
      .then((item) => {
        if (active) setAchievement({
          ...blankAchievement,
          ...item,
          cardType: item.cardType || 'CERTIFICATION',
          issuer: item.issuer || '',
          description: item.description || '',
          achievedOn: item.achievedOn || '',
          url: item.url || '',
          imageUrl: item.imageUrl || '',
          imageUrls: item.imageUrls?.length ? item.imageUrls : (item.imageUrl ? [item.imageUrl] : []),
        })
      })
      .catch((requestError) => { if (active) setError(requestError.message) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [id, isEditing])

  function updateField(field, value) {
    setAchievement((current) => ({ ...current, [field]: value }))
  }

  async function submit(event) {
    event.preventDefault()
    setError('')
    setSaving(true)
    const payload = {
      ...achievement,
      title: achievement.title.trim(),
      cardType: achievement.cardType,
      issuer: achievement.issuer.trim() || null,
      description: achievement.description.trim() || null,
      achievedOn: achievement.achievedOn || null,
      url: achievement.url.trim() || null,
      imageUrl: achievement.imageUrls[0] || achievement.imageUrl.trim() || null,
      imageUrls: achievement.imageUrls,
      sortOrder: Number(achievement.sortOrder),
    }
    try {
      await apiFetch(isEditing ? `/api/admin/achievements/${id}` : '/api/admin/achievements', {
        method: isEditing ? 'PUT' : 'POST',
        body: JSON.stringify(payload),
      })
      navigate('/admin/achievements', {
        replace: true,
        state: { notice: isEditing ? 'Achievement updated.' : 'Achievement added.' },
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

  if (loading) return <section className="admin-page"><p role="status">Loading achievement editor…</p></section>

  return <section className="admin-page">
    <div className="admin-page-heading admin-form-heading">
      <div><Link to="/admin/achievements" className="admin-back-link"><ArrowLeft size={16} />Achievements</Link><span className="eyebrow">Portfolio content</span><h1>{isEditing ? 'Edit achievement' : 'Add achievement'}</h1><p>Enter the award or accomplishment details.</p></div>
    </div>
    {error && <p className="admin-form-error" role="alert">{error}</p>}
    <form className="admin-editor-form" onSubmit={submit}>
      <section className="admin-form-section">
        <div className="admin-form-section-title"><h2>Portfolio card details</h2><p>Choose where this item appears. It controls its public card, content, image, and details.</p></div>
        <div className="admin-form-grid">
          <label className="admin-field">Title<input required maxLength="200" value={achievement.title} onChange={(event) => updateField('title', event.target.value)} /></label>
          <label className="admin-field">Card section<select value={achievement.cardType} onChange={(event) => updateField('cardType', event.target.value)}><option value="CERTIFICATION">Certification</option><option value="BUSINESS">Business</option><option value="COMMUNITY">Community</option><option value="CHALLENGE">Challenge / hackathon</option><option value="OTHER">Other highlight</option></select></label>
          <label className="admin-field">Issuer <span className="admin-optional-label">(optional)</span><input maxLength="160" value={achievement.issuer} onChange={(event) => updateField('issuer', event.target.value)} /></label>
          <label className="admin-field admin-field-full">Description <span className="admin-optional-label">(optional)</span><textarea rows="5" value={achievement.description} onChange={(event) => updateField('description', event.target.value)} /></label>
          <label className="admin-field">Date achieved <span className="admin-optional-label">(optional)</span><input type="date" value={achievement.achievedOn} onChange={(event) => updateField('achievedOn', event.target.value)} /></label>
          <label className="admin-field">Credential URL <span className="admin-optional-label">(optional)</span><input type="url" maxLength="500" value={achievement.url} onChange={(event) => updateField('url', event.target.value)} placeholder="https://example.com/credential" /></label>
          <AdminImageGalleryField images={achievement.imageUrls} onChange={(images) => updateField('imageUrls', images)}
            onUploadingChange={setUploadingImages} purpose="Card" />
          <label className="admin-field">Display order<input type="number" min="0" max="100000" value={achievement.sortOrder} onChange={(event) => updateField('sortOrder', event.target.value)} /></label>
        </div>
      </section>
      <div className="admin-form-actions"><Link to="/admin/achievements" className="admin-cancel-button">Cancel</Link><button className="admin-primary-button" type="submit" disabled={saving || uploadingImages}><Save size={17} />{saving ? 'Saving…' : uploadingImages ? 'Uploading images…' : 'Save achievement'}</button></div>
    </form>
  </section>
}
