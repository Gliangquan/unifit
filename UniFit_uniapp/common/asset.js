import { BASE_URL } from './config'

function getApiOrigin() {
  return (BASE_URL || '').replace(/\/api\/?$/, '')
}

export function resolveAssetUrl(url) {
  if (!url) return ''
  const raw = String(url).trim()
  if (!raw) return ''

  const apiOrigin = getApiOrigin()
  if (!/^https?:\/\//i.test(raw)) {
    if (!apiOrigin) return raw
    return raw.startsWith('/') ? `${apiOrigin}${raw}` : `${apiOrigin}/${raw}`
  }

  try {
    const apiUrl = apiOrigin ? new URL(apiOrigin) : null
    const assetUrl = new URL(raw)
    if (apiUrl && /^(localhost|127\.0\.0\.1)$/i.test(assetUrl.hostname)) {
      assetUrl.hostname = apiUrl.hostname
      if (apiUrl.protocol) {
        assetUrl.protocol = apiUrl.protocol
      }
    }
    return assetUrl.toString()
  } catch (e) {
    return raw
  }
}
