import { apiFetch, initializeCsrf } from './api'

const consentKey = 'portfolio-analytics-consent'
const sessionKey = 'portfolio-analytics-session'
const supportedEvents = new Set([
  'page_view',
  'project_view',
  'project_github_click',
  'project_demo_click',
  'resume_view',
  'resume_download',
  'contact_form_open',
  'contact_form_submit',
  'social_link_click',
])

export function getAnalyticsConsent() {
  return window.localStorage.getItem(consentKey) || 'unset'
}

export function setAnalyticsConsent(consent) {
  if (!['accepted', 'declined'].includes(consent)) {
    throw new TypeError('Analytics consent must be accepted or declined.')
  }
  window.localStorage.setItem(consentKey, consent)
}

export async function recordAnalyticsEvent(eventName, path = window.location.pathname) {
  if (!supportedEvents.has(eventName) || getAnalyticsConsent() !== 'accepted') return
  const safePath = typeof path === 'string' && path.startsWith('/') && !path.startsWith('//')
    ? path.split(/[?#]/, 1)[0].slice(0, 500)
    : '/'
  let sessionId = window.sessionStorage.getItem(sessionKey)
  if (!sessionId) {
    sessionId = window.crypto.randomUUID()
    window.sessionStorage.setItem(sessionKey, sessionId)
  }
  try {
    await initializeCsrf()
    await apiFetch('/api/analytics/events', {
      method: 'POST',
      body: JSON.stringify({ sessionId, eventName, path: safePath }),
    })
  } catch (error) {
    console.warn('Anonymous analytics event could not be recorded.', error.message)
  }
}
