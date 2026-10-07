import { useEffect, useState } from 'react'
import { ArrowLeft, Save } from 'lucide-react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { apiFetch } from '../../services/api'

const settingOptions = [
  { key: 'profile.availability', label: 'Availability', description: 'A short note about your availability for work.' },
  { key: 'profile.education', label: 'Education summary', description: 'A fallback education summary shown when no education entry is available.' },
  { key: 'profile.focus', label: 'Areas of focus', description: 'Comma-separated focus areas shown on your home page.' },
  { key: 'profile.location', label: 'Location', description: 'The location shown in your public profile.' },
]

export default function AdminSettingForm() {
  const { key } = useParams()
  const navigate = useNavigate()
  const isEditing = Boolean(key)
  const decodedKey = key ? decodeURIComponent(key) : ''
  const selectedOption = settingOptions.find((item) => item.key === decodedKey)
  const invalidSetting = isEditing && !selectedOption
  const [settingKey, setSettingKey] = useState(decodedKey || settingOptions[0].key)
  const [value, setValue] = useState('')
  const [loading, setLoading] = useState(isEditing && Boolean(selectedOption))
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!isEditing || !selectedOption) return
    let active = true
    apiFetch('/api/admin/settings')
      .then((items) => {
        if (!active) return
        const item = items.find((setting) => setting.settingKey === decodedKey)
        if (!item) throw new Error('The requested setting could not be found.')
        setValue(item.settingValue || '')
      })
      .catch((requestError) => { if (active) setError(requestError.message) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [decodedKey, isEditing, selectedOption])

  async function submit(event) {
    event.preventDefault()
    setError('')
    setSaving(true)
    try {
      await apiFetch(`/api/admin/settings/${encodeURIComponent(settingKey)}`, {
        method: 'PUT',
        body: JSON.stringify({ settingValue: value }),
      })
      navigate('/admin/settings', {
        replace: true,
        state: { notice: 'Setting saved.' },
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

  const option = settingOptions.find((item) => item.key === settingKey)
  if (loading) return <section className="admin-page"><p role="status">Loading setting editor…</p></section>

  return <section className="admin-page">
    <div className="admin-page-heading admin-form-heading">
      <div><Link to="/admin/settings" className="admin-back-link"><ArrowLeft size={16} />Settings</Link><span className="eyebrow">Public profile</span><h1>{isEditing ? 'Edit setting' : 'Edit a setting'}</h1><p>These values are used as public-facing profile content.</p></div>
    </div>
    {(error || invalidSetting) && <p className="admin-form-error" role="alert">{error || 'This setting is not editable here.'}</p>}
    <form className="admin-editor-form" onSubmit={submit}>
      <section className="admin-form-section">
        <div className="admin-form-section-title"><h2>Profile setting</h2><p>{option?.description}</p></div>
        <div className="admin-form-grid">
          <label className="admin-field">Setting
            <select value={settingKey} disabled={isEditing} onChange={(event) => setSettingKey(event.target.value)}>
              {settingOptions.map((item) => <option key={item.key} value={item.key}>{item.label} ({item.key})</option>)}
            </select>
          </label>
          <label className="admin-field admin-field-full">Value<textarea rows="5" maxLength="5000" value={value} onChange={(event) => setValue(event.target.value)} placeholder={settingKey === 'profile.focus' ? 'Design, Engineering, Accessibility' : 'Enter a public profile value'} /><small>{settingKey === 'profile.focus' ? 'Separate each focus area with a comma.' : 'Leave blank to clear this value.'}</small></label>
        </div>
      </section>
      <div className="admin-form-actions"><Link to="/admin/settings" className="admin-cancel-button">Cancel</Link><button className="admin-primary-button" type="submit" disabled={saving || Boolean(error) || invalidSetting}><Save size={17} />{saving ? 'Saving…' : 'Save setting'}</button></div>
    </form>
  </section>
}
