import { useState, useEffect } from 'react'
import { MapPin, Copy, ExternalLink, StopCircle, RefreshCw, Clock } from 'lucide-react'
import { startTracking, updateLocation, stopTracking, getActiveSession, getHistory } from '../../api/tracking.js'
import Button from '../../components/ui/Button.jsx'
import Alert  from '../../components/ui/Alert.jsx'
import Badge  from '../../components/ui/Badge.jsx'

const getCoords = () => new Promise((resolve, reject) =>
  navigator.geolocation
    ? navigator.geolocation.getCurrentPosition(
        p => resolve({ latitude: p.coords.latitude, longitude: p.coords.longitude }),
        () => reject(new Error('Location access denied. Please enable GPS.'))
      )
    : reject(new Error('Geolocation not supported.'))
)

const fmt = (d) => new Date(d).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' })

export default function TrackingPage() {
  const [session,  setSession]  = useState(null)
  const [history,  setHistory]  = useState([])
  const [loading,  setLoading]  = useState(true)
  const [starting, setStarting] = useState(false)
  const [updating, setUpdating] = useState(false)
  const [stopping, setStopping] = useState(false)
  const [copied,   setCopied]   = useState(false)
  const [error,    setError]    = useState('')
  const [toast,    setToast]    = useState('')

  const showToast = (m) => { setToast(m); setTimeout(() => setToast(''), 3000) }

  const load = async () => {
    setLoading(true)
    try {
      const [activeRes, histRes] = await Promise.allSettled([getActiveSession(), getHistory()])
      if (activeRes.status === 'fulfilled') setSession(activeRes.value.data.data)
      else setSession(null)
      if (histRes.status === 'fulfilled') setHistory(histRes.value.data.data || [])
    } finally { setLoading(false) }
  }
  useEffect(() => { load() }, [])

  const handleStart = async () => {
    setStarting(true); setError('')
    try {
      const coords = await getCoords()
      await startTracking(coords)
      showToast('Live tracking started!')
      load()
    } catch (err) { setError(err.message || err.response?.data?.message || 'Failed') }
    finally { setStarting(false) }
  }

  const handleUpdate = async () => {
    if (!session) return
    setUpdating(true)
    try {
      const coords = await getCoords()
      await updateLocation(session.id, coords)
      showToast('Location updated')
      load()
    } catch (err) { showToast(err.message || 'Failed to update location') }
    finally { setUpdating(false) }
  }

  const handleStop = async () => {
    if (!confirm('Stop tracking?')) return
    setStopping(true)
    try { await stopTracking(session.id); showToast('Tracking stopped'); load() }
    catch { showToast('Failed to stop') }
    finally { setStopping(false) }
  }

  const copyLink = () => {
    navigator.clipboard.writeText(session.shareLink)
    setCopied(true); setTimeout(() => setCopied(false), 2000)
  }

  return (
    <div className="max-w-2xl mx-auto space-y-6 animate-slide-up">
      <div>
        <h2 className="text-lg font-bold dark:text-white text-slate-900">Live Tracking</h2>
        <p className="text-sm dark:text-slate-400 text-slate-500 mt-0.5">
          Share a live location link with your trusted contacts
        </p>
      </div>

      {toast && <Alert type="success" message={toast} />}
      {error  && <Alert type="error"   message={error} />}

      {loading ? (
        <div className="card p-6 animate-pulse space-y-3">
          {[1,2,3].map(i => <div key={i} className="h-4 bg-slate-200 dark:bg-surface-muted rounded" />)}
        </div>
      ) : session ? (
        /* ── Active session card ─── */
        <div className="card p-5 space-y-4 border-emerald-500/40 border-2">
          <div className="flex items-center gap-3">
            <div className="relative">
              <div className="absolute inset-0 rounded-full bg-emerald-500/20 animate-ping" />
              <div className="relative w-9 h-9 rounded-full bg-emerald-500 flex items-center justify-center">
                <MapPin className="w-4 h-4 text-white" />
              </div>
            </div>
            <div>
              <p className="font-bold text-emerald-600 dark:text-emerald-400">Tracking Active</p>
              <p className="text-xs dark:text-slate-400 text-slate-500">Started {fmt(session.startedAt)}</p>
            </div>
          </div>

          <div className="dark:bg-surface-muted bg-slate-50 rounded-xl p-3 space-y-2">
            <p className="text-xs font-medium dark:text-slate-400 text-slate-500 uppercase tracking-wide">Share link</p>
            <div className="flex items-center gap-2">
              <p className="text-xs font-mono dark:text-slate-300 text-slate-600 flex-1 truncate">{session.shareLink}</p>
              <button onClick={copyLink}
                className="flex-shrink-0 px-3 py-1.5 rounded-lg text-xs font-medium
                  dark:bg-surface-border bg-slate-200 dark:text-slate-300 text-slate-700
                  hover:bg-brand-600 hover:text-white transition-all duration-150">
                {copied ? '✓ Copied' : <><Copy className="w-3 h-3 inline mr-1" />Copy</>}
              </button>
            </div>
          </div>

          <div className="text-xs dark:text-slate-400 text-slate-500 space-y-1">
            <p>📍 Current: {session.currentLatitude?.toFixed(4)}, {session.currentLongitude?.toFixed(4)}</p>
            {session.currentAddress && <p>🏠 {session.currentAddress}</p>}
          </div>

          <a href={session.googleMapsLink} target="_blank" rel="noreferrer"
            className="flex items-center gap-1 text-xs text-brand-500 hover:text-brand-400 font-medium">
            <ExternalLink className="w-3 h-3" /> View on Google Maps
          </a>

          <div className="flex gap-2">
            <Button onClick={handleUpdate} loading={updating} variant="outline" size="sm" fullWidth>
              <RefreshCw className="w-3.5 h-3.5" /> Update location
            </Button>
            <Button onClick={handleStop} loading={stopping} danger size="sm" fullWidth>
              <StopCircle className="w-3.5 h-3.5" /> Stop tracking
            </Button>
          </div>
        </div>
      ) : (
        /* ── Start tracking ─────── */
        <div className="card p-8 flex flex-col items-center gap-5">
          <div className="w-20 h-20 rounded-2xl bg-emerald-100 dark:bg-emerald-900/30 flex items-center justify-center">
            <MapPin className="w-10 h-10 text-emerald-500" />
          </div>
          <div className="text-center">
            <p className="font-semibold dark:text-slate-200 text-slate-800">Start live tracking</p>
            <p className="text-sm dark:text-slate-400 text-slate-500 mt-1 max-w-xs">
              A shareable link will be generated. Send it to your contacts so they can follow your location in real time.
            </p>
          </div>
          <Button onClick={handleStart} loading={starting} size="lg" className="!bg-emerald-500 hover:!bg-emerald-600 text-white">
            <MapPin className="w-4 h-4" /> Start tracking
          </Button>
        </div>
      )}

      {/* History */}
      {history.filter(s => s.status === 'STOPPED').length > 0 && (
        <section>
          <h3 className="text-xs font-semibold uppercase tracking-widest dark:text-slate-500 text-slate-400 mb-3">
            Past sessions
          </h3>
          <div className="card divide-y dark:divide-surface-border divide-slate-100">
            {history.filter(s => s.status === 'STOPPED').map(s => (
              <div key={s.id} className="flex items-center gap-3 px-4 py-3">
                <Clock className="w-4 h-4 dark:text-slate-500 text-slate-400 flex-shrink-0" />
                <div className="flex-1">
                  <p className="text-xs dark:text-slate-400 text-slate-500">
                    {fmt(s.startedAt)} → {s.stoppedAt ? fmt(s.stoppedAt) : '—'}
                  </p>
                </div>
                <Badge label="Stopped" type="stopped" />
              </div>
            ))}
          </div>
        </section>
      )}
    </div>
  )
}
