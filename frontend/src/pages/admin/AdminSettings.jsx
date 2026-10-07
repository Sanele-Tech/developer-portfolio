import { useEffect, useState } from 'react'
import { Pencil, Settings2, Trash2 } from 'lucide-react'
import { Link, useLocation } from 'react-router-dom'
import { apiFetch } from '../../services/api'

const settingLabels = {
  'profile.availability': 'Availability',
  'profile.education': 'Education summary',
  'profile.focus': 'Areas of focus',
  'profile.location': 'Location',
}

const emptyProfile = {
  name: '',
  title: '',
  bio: '',
  email: '',
  phoneNumber: '',
  location: '',
  avatarUrl: '',
  githubUrl: '',
  linkedinUrl: '',
}

export default function AdminSettings() {
  const location = useLocation()
  const [items, setItems] = useState(null)
  const [profile, setProfile] = useState(null)
  const [profileDraft, setProfileDraft] = useState(emptyProfile)
  const [error, setError] = useState('')
  const [profileError, setProfileError] = useState('')
  const [profileNotice, setProfileNotice] = useState('')
  const [savingProfile, setSavingProfile] = useState(false)
  const [uploadingAvatar, setUploadingAvatar] = useState(false)
  const [notice, setNotice] = useState(() => location.state?.notice || '')
  const [deletingKey, setDeletingKey] = useState('')
  const [refreshKey, setRefreshKey] = useState(0)

  useEffect(() => {
    let active = true
    apiFetch('/api/admin/settings')
      .then((data) => {
        if (!active) return
        setItems(data)
        setError('')
      })
      .catch((requestError) => { if (active) setError(requestError.message) })
    return () => { active = false }
  }, [refreshKey])

  useEffect(() => {
    let active = true
    apiFetch('/api/profile')
      .then((data) => {
        if (!active) return
        setProfile(data)
        setProfileDraft(toProfileDraft(data))
      })
      .catch((requestError) => {
        if (!active) return
        if (requestError.status !== 404) setProfileError(requestError.message)
      })
    return () => { active = false }
  }, [])

  async function uploadAvatar(file) {
    if (!file) return
    setProfileError('')
    setProfileNotice('')
    setUploadingAvatar(true)
    const formData = new FormData()
    formData.append('file', file)
    try {
      const uploaded = await apiFetch('/api/admin/media?purpose=IMAGE', {
        method: 'POST',
        body: formData,
      })
      setProfileDraft((current) => ({ ...current, avatarUrl: uploaded.url }))
      setProfileNotice('Avatar uploaded. Save profile details to apply it.')
    } catch (requestError) {
      setProfileError(requestError.message)
    } finally {
      setUploadingAvatar(false)
    }
  }

  async function saveProfile(event) {
    event.preventDefault()
    setProfileError('')
    setProfileNotice('')
    setSavingProfile(true)
    const payload = {
      ...profileDraft,
      name: profileDraft.name.trim(),
      title: profileDraft.title.trim(),
      bio: profileDraft.bio.trim(),
      email: profileDraft.email.trim(),
      phoneNumber: profileDraft.phoneNumber.trim() || null,
      location: profileDraft.location.trim() || null,
      avatarUrl: profileDraft.avatarUrl.trim() || null,
      githubUrl: profileDraft.githubUrl.trim() || null,
      linkedinUrl: profileDraft.linkedinUrl.trim() || null,
    }
    try {
      const updatedProfile = await apiFetch('/api/admin/profile', {
        method: 'PUT',
        body: JSON.stringify(payload),
      })
      setProfile(updatedProfile)
      setProfileDraft(toProfileDraft(updatedProfile))
      setProfileNotice('Profile details saved.')
    } catch (requestError) {
      const fieldErrors = requestError.details?.errors
      setProfileError(Array.isArray(fieldErrors) && fieldErrors.length
        ? fieldErrors.map((item) => `${item.field}: ${item.message}`).join(' ')
        : requestError.message)
    } finally {
      setSavingProfile(false)
    }
  }

  function updateProfileField(field, value) {
    setProfileDraft((current) => ({ ...current, [field]: value }))
  }

  async function deleteSetting(item) {
    if (!window.confirm(`Remove the ${settingLabels[item.settingKey]} setting?`)) return
    setDeletingKey(item.settingKey)
    setError('')
    try {
      await apiFetch(`/api/admin/settings/${encodeURIComponent(item.settingKey)}`, { method: 'DELETE' })
      setRefreshKey((value) => value + 1)
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setDeletingKey('')
    }
  }

  return <section className="admin-page">
    <div className="admin-page-heading">
      <div><span className="eyebrow">Configuration</span><h1>Settings</h1><p>Manage public profile details used on your portfolio home page.</p></div>
      <Link className="admin-primary-button" to="/admin/settings/new">Edit a setting</Link>
    </div>
    {notice && <p className="admin-success" role="status">{notice}<button type="button" aria-label="Dismiss message" onClick={() => setNotice('')}>×</button></p>}
    {error && <p className="admin-form-error" role="alert">{error}</p>}
    <section className="admin-form-section">
      <div className="admin-form-section-title"><h2>Public profile</h2><p>Create or update profile details and upload a profile image. Create your profile first to add skills, experience, education, and achievements.</p></div>
      {profileError && <p className="admin-form-error" role="alert">{profileError}</p>}
      {profileNotice && <p className="admin-success" role="status">{profileNotice}</p>}
      <form className="admin-editor-form" onSubmit={saveProfile}>
        <div className="admin-form-grid">
          <label className="admin-field">Name<input required maxLength="120" value={profileDraft.name} onChange={(event) => updateProfileField('name', event.target.value)} /></label>
          <label className="admin-field">Title<input required maxLength="160" value={profileDraft.title} onChange={(event) => updateProfileField('title', event.target.value)} /></label>
          <label className="admin-field admin-field-full">Biography<textarea required maxLength="10000" rows="5" value={profileDraft.bio} onChange={(event) => updateProfileField('bio', event.target.value)} /></label>
          <label className="admin-field">Email<input required type="email" maxLength="320" value={profileDraft.email} onChange={(event) => updateProfileField('email', event.target.value)} /></label>
          <label className="admin-field">Phone number<input type="tel" maxLength="40" value={profileDraft.phoneNumber} onChange={(event) => updateProfileField('phoneNumber', event.target.value)} /></label>
          <label className="admin-field">Location<input maxLength="160" value={profileDraft.location} onChange={(event) => updateProfileField('location', event.target.value)} /></label>
          {profileDraft.avatarUrl && <img className="admin-profile-avatar" src={profileDraft.avatarUrl} alt="Profile avatar preview" width="120" height="120" />}
          <label className="admin-field">Upload avatar<input type="file" accept="image/jpeg,image/png,image/webp,image/gif" disabled={uploadingAvatar} onChange={(event) => { const file = event.target.files?.[0]; event.target.value = ''; uploadAvatar(file) }} /><small>{uploadingAvatar ? 'Uploading…' : 'JPEG, PNG, WebP, or GIF; maximum 10 MB.'}</small></label>
          <label className="admin-field admin-field-full">Avatar URL<input type="url" maxLength="500" value={profileDraft.avatarUrl} onChange={(event) => updateProfileField('avatarUrl', event.target.value)} placeholder="https://" /></label>
          <label className="admin-field">GitHub URL<input type="url" maxLength="500" value={profileDraft.githubUrl} onChange={(event) => updateProfileField('githubUrl', event.target.value)} placeholder="https://" /></label>
          <label className="admin-field">LinkedIn URL<input type="url" maxLength="500" value={profileDraft.linkedinUrl} onChange={(event) => updateProfileField('linkedinUrl', event.target.value)} placeholder="https://" /></label>
        </div>
        <div className="admin-form-actions"><button className="admin-primary-button" type="submit" disabled={savingProfile || uploadingAvatar}>{savingProfile ? 'Saving…' : profile ? 'Save profile' : 'Create profile'}</button></div>
      </form>
    </section>
    {items ? <div className="admin-table-wrap">
      <table className="admin-table">
        <thead><tr><th>Setting</th><th>Value</th><th>Last updated</th><th><span className="sr-only">Actions</span></th></tr></thead>
        <tbody>{items.map((item) => <tr key={item.settingKey}>
          <td><strong>{settingLabels[item.settingKey]}</strong><small>{item.settingKey}</small></td>
          <td>{item.settingValue || <span className="admin-optional-label">Not set</span>}</td>
          <td>{formatDate(item.updatedAt)}</td>
          <td><div className="admin-row-actions">
            <Link to={`/admin/settings/${encodeURIComponent(item.settingKey)}/edit`} aria-label={`Edit ${settingLabels[item.settingKey]}`}><Pencil size={16} /></Link>
            {item.id != null && <button type="button" onClick={() => deleteSetting(item)} disabled={deletingKey === item.settingKey} aria-label={`Remove ${settingLabels[item.settingKey]}`}><Trash2 size={16} /></button>}
          </div></td>
        </tr>)}</tbody>
      </table>
      {!items.length && <div className="admin-empty"><Settings2 size={28} /><h2>No settings available</h2></div>}
    </div> : !error && <p role="status">Loading settings…</p>}
  </section>
}

function toProfileDraft(profileData) {
  return {
    ...emptyProfile,
    ...profileData,
    phoneNumber: profileData.phoneNumber || '',
    location: profileData.location || '',
    avatarUrl: profileData.avatarUrl || '',
    githubUrl: profileData.githubUrl || '',
    linkedinUrl: profileData.linkedinUrl || '',
  }
}

function formatDate(value) {
  if (!value) return '—'
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(new Date(value))
}
