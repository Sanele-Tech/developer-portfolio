import { useEffect, useState } from 'react'
import { ArrowRight, LockKeyhole } from 'lucide-react'
import { Link, useNavigate } from 'react-router-dom'
import { apiFetch, initializeCsrf } from '../../services/api'

export default function AdminLogin() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [checkingSession, setCheckingSession] = useState(true)
  const [sessionError, setSessionError] = useState('')
  const navigate = useNavigate()

  useEffect(() => {
    let active = true
    document.title = 'Admin sign in | Sanele Dlomo'
    apiFetch('/api/auth/me')
      .then(() => { if (active) navigate('/admin', { replace: true }) })
      .catch((requestError) => {
        if (!active) return
        setCheckingSession(false)
        if (requestError.status !== 401) setSessionError(requestError.message)
      })
    return () => { active = false }
  }, [navigate])

  async function submit(event) {
    event.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      await initializeCsrf()
      await apiFetch('/api/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username, password }),
      })
      navigate('/admin', { replace: true })
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setSubmitting(false)
    }
  }

  if (checkingSession) return <main className="admin-auth-status" role="status">Checking your session…</main>
  return <main className="admin-login-page">
    <section className="admin-login-card">
      <Link to="/" className="admin-login-brand"><span>SD</span>Sanele Dlomo</Link>
      <div className="admin-login-icon"><LockKeyhole size={22} /></div>
      <span className="eyebrow">Portfolio CMS</span>
      <h1>Welcome back</h1>
      <p>Sign in to manage your portfolio content.</p>
      {sessionError && <p className="admin-form-error" role="alert">Could not check for an existing session: {sessionError}</p>}
      <form onSubmit={submit} className="admin-form">
        <label htmlFor="admin-username">Username</label>
        <input id="admin-username" autoComplete="username" required value={username} onChange={(event) => setUsername(event.target.value)} />
        <label htmlFor="admin-password">Password</label>
        <input id="admin-password" type="password" autoComplete="current-password" required value={password} onChange={(event) => setPassword(event.target.value)} />
        {error && <p className="admin-form-error" role="alert">{error}</p>}
        <button className="admin-primary-button" type="submit" disabled={submitting}>{submitting ? 'Signing in…' : 'Sign in'} <ArrowRight size={17} /></button>
      </form>
    </section>
  </main>
}
