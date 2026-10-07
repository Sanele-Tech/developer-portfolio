const API_BASE_URL = import.meta.env.VITE_API_BASE_URL
  || (import.meta.env.DEV ? '' : `${window.location.protocol}//${window.location.hostname}:8080`)
const ASSET_BASE_URL = API_BASE_URL || window.location.origin

export function resolvePublicAssetUrl(value) {
  if (!value) return null
  let url
  try {
    url = new URL(value, ASSET_BASE_URL)
  } catch {
    return null
  }
  if (url.protocol !== 'http:' && url.protocol !== 'https:') return null
  if (['localhost', '127.0.0.1', '0.0.0.0'].includes(url.hostname)) {
    url.host = new URL(ASSET_BASE_URL).host
  }
  return url.href
}

export function resolvePublicImageUrl(value) {
  if (!value) return null
  if (value.startsWith('/images/')) return new URL(value, window.location.origin).href
  return resolvePublicAssetUrl(value)
}

export function resolveExternalUrl(value) {
  if (!value) return null
  try {
    const url = new URL(value)
    return url.protocol === 'http:' || url.protocol === 'https:' ? url.href : null
  } catch {
    return null
  }
}

function readCookie(name) {
  return document.cookie.split('; ').find((part) => part.startsWith(`${name}=`))?.split('=')[1]
}

export async function apiFetch(path, options = {}) {
  const method = options.method || 'GET'
  const headers = new Headers(options.headers || {})
  headers.set('Accept', 'application/json')
  if (options.body && !(options.body instanceof FormData) && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }
  if (method !== 'GET' && method !== 'HEAD') {
    const csrfToken = readCookie('XSRF-TOKEN')
    if (csrfToken) headers.set('X-XSRF-TOKEN', decodeURIComponent(csrfToken))
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
    credentials: 'include',
  })
  const body = await response.json().catch(() => null)
  if (!response.ok) {
    const message = body?.message === 'Profile not found: default'
      ? 'Create your public profile in Profile & settings before adding portfolio content.'
      : body?.message || 'The request could not be completed.'
    const error = new Error(message)
    error.status = response.status
    error.code = body?.code || 'REQUEST_FAILED'
    error.details = body
    throw error
  }
  return body
}

export async function initializeCsrf() {
  return apiFetch('/api/auth/csrf')
}
