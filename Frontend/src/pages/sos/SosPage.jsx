import { useState, useEffect } from 'react'
import { AlertTriangle, MapPin, CheckCircle, XCircle, Clock, ExternalLink } from 'lucide-react'
import { triggerSos, getActiveSos, getSosHistory, resolveSos, cancelSos, updateLocation } from '../../api/sos.js'
import Button from '../../components/ui/Button.jsx'
import Alert  from '../../components/ui/Alert.jsx'
import Badge  from '../../components/ui/Badge.jsx'

const getCoords = () => new Promise((resolve, reject) =>
  navigator.geolocation
    ? navigator.geolocation.getCurrentPosition(
        p => resolve({ latitude: p.coords.latitude, longitude: p.coords.longitude }),
        () => reject(new Error('Location access denied. Please enable GPS.'))
      )
    : reject(new Error('Geolocation not supported by your browser.'))
)

const fmt = (d) => new Date(d).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' })

export default function SosPage() {
  const [active,  setActive]   = useState(null)
  const [history, setHistory]  = useState([])
  const [loading, setLoading]  = useState(true)
  const [sending, setSending]  = useState(false)
  const [updating, setUpdating]= useState(false)
  const [error,   setError]    = useState('')
  const [toast,   setToast]    = useState({ msg: '', type: 'success' })

  const showToast = (msg, type = 'success') => { setToast({ msg, type }); setTimeout(() => setToast({ msg: '', type: 'success' }), 4000) }

  const load = async () => {
    setLoading(true)
    try {
      const [activeRes, histRes] = await Promise.allSettled([getActiveSos(), getSosHistory()])
      if (activeRes.status === 'fulfilled') setActive(activeRes.value.data.data)
      else setActive(null)
      if (histRes.status === 'fulfilled')  setHistory(histRes.value.data.data || [])
    } finally { setLoading(false) }
  }
  useEffect(() => { load() }, [])

  const handleTrigger = async () => {
    setSending(true); setError('')
    try {
      const coords = await getCoords()
      await triggerSos({ ...coords, message: 'I need help!' })
      showToast('🚨 SOS alert sent. Your contacts have been notified.')
      load()
    } catch (err) {
      setError(err.message || err.response?.data?.message || 'Failed to send SOS')
    } finally { setSending(false) }
  }

  const handleUpdateLocation = async () => {
    if (!active) return
    setUpdating(true)
    try {
      const coords = await getCoords()
      await updateLocation(active.id, coords)
      showToast('Location updated')
      load()
    } catch (err) { showToast(err.message || 'Failed to update location', 'error') }
    finally { setUpdating(false) }
  }

  const handleResolve = async () => {
    try { await resolveSos(active.id); showToast('✅ Marked safe. Contacts notified.'); load() }
    catch (err) { showToast(err.response?.data?.message || 'Failed', 'error') }
  }

  const handleCancel = async () => {
    if (!confirm('Cancel this SOS as a false alarm?')) return
    try { await cancelSos(active.id); showToast('SOS cancelled (false alarm)'); load() }
    catch (err) { showToast(err.response?.data?.message || 'Failed', 'error') }
  }

  return (
    <div className="max-w-2xl mx-auto space-y-6 animate-slide-up">

      <div>
        <h2 className="text-lg font-bold dark:text-white text-slate-900">SOS Alert</h2>
        <p className="text-sm dark:text-slate-400 text-slate-500 mt-0.5">
          One tap sends your location to all emergency contacts
        </p>
      </div>

      {toast.msg && <Alert type={toast.type} message={toast.msg} />}
      {error     && <Alert type="error" message={error} />}

      {loading ? (
        <div className="card p-8 animate-pulse flex items-center justify-center">
          <div className="w-32 h-32 rounded-full bg-slate-200 dark:bg-surface-muted" />
        </div>
      ) : active ? (
        /* ── Active SOS card ─────────────── */
        <div className="card p-6 border-red-500/50 dark:border-red-500/30 border-2 space-y-4">
          <div className="flex items-center gap-3">
            <div className="relative">
              <div className="absolute inset-0 rounded-full bg-red-500/20 animate-ping" />
              <div className="relative w-10 h-10 rounded-full bg-red-500 flex items-center justify-center">
                <AlertTriangle className="w-5 h-5 text-white" />
              </div>
            </div>
            <div>
              <p className="font-bold text-red-600 dark:text-red-400">SOS Active</p>
              <p className="text-xs dark:text-slate-400 text-slate-500">{fmt(active.createdAt)}</p>
            </div>
          </div>

          {active.message && (
            <p className="text-sm dark:text-slate-300 text-slate-600 italic">"{active.message}"</p>
          )}

          <a href={active.googleMapsLink} target="_blank" rel="noreferrer"
            className="flex items-center gap-2 text-sm text-brand-500 hover:text-brand-400 font-medium">
            <MapPin className="w-4 h-4" />
            View your location on Maps
            <ExternalLink className="w-3 h-3" />
          </a>

          <div className="grid grid-cols-3 gap-2 pt-1">
            <Button onClick={handleUpdateLocation} loading={updating} variant="outline" size="sm">
              <MapPin className="w-3.5 h-3.5" /> Update location
            </Button>
            <Button onClick={handleResolve} size="sm"
              className="!bg-emerald-500 hover:!bg-emerald-600 text-white">
              <CheckCircle className="w-3.5 h-3.5" /> I'm safe
            </Button>
            <Button onClick={handleCancel} danger size="sm">
              <XCircle className="w-3.5 h-3.5" /> False alarm
            </Button>
          </div>
        </div>
      ) : (
        /* ── SOS trigger button ──────────── */
        <div className="card p-8 flex flex-col items-center gap-6">
          <div className="relative">
            <div className="absolute inset-0 rounded-full bg-red-500/10 scale-150 animate-ping-slow" />
            <div className="absolute inset-0 rounded-full bg-red-500/15 scale-125 animate-ping-slower" />
            <button onClick={handleTrigger} disabled={sending}
              className="relative w-40 h-40 rounded-full bg-red-500 hover:bg-red-600
                active:scale-95 disabled:opacity-70 disabled:cursor-not-allowed
                shadow-2xl shadow-red-500/40
                flex flex-col items-center justify-center gap-2
                transition-all duration-150 focus:outline-none focus:ring-4 focus:ring-red-400/50">
              {sending
                ? <div className="w-8 h-8 border-4 border-white/30 border-t-white rounded-full animate-spin" />
                : <>
                  <AlertTriangle className="w-10 h-10 text-white" />
                  <span className="text-white font-extrabold text-lg tracking-wide">SOS</span>
                </>
              }
            </button>
          </div>
          <div className="text-center">
            <p className="font-semibold dark:text-slate-300 text-slate-700">
              {sending ? 'Sending alert…' : 'Tap to send SOS'}
            </p>
            <p className="text-sm dark:text-slate-500 text-slate-400 mt-1">
              Your GPS location will be shared with your emergency contacts
            </p>
          </div>
        </div>
      )}

      {/* History */}
      {history.length > 0 && (
        <section>
          <h3 className="text-xs font-semibold uppercase tracking-widest dark:text-slate-500 text-slate-400 mb-3">
            Alert history
          </h3>
          <div className="card divide-y dark:divide-surface-border divide-slate-100">
            {history.filter(a => a.status !== 'ACTIVE').map(a => (
              <div key={a.id} className="flex items-center gap-3 px-4 py-3">
                <Clock className="w-4 h-4 dark:text-slate-500 text-slate-400 flex-shrink-0" />
                <div className="flex-1 min-w-0">
                  <p className="text-xs dark:text-slate-400 text-slate-500">{fmt(a.createdAt)}</p>
                  {a.message && <p className="text-xs dark:text-slate-500 text-slate-400 truncate mt-0.5">"{a.message}"</p>}
                </div>
                <Badge label={a.status} type={a.status.toLowerCase()} />
              </div>
            ))}
          </div>
        </section>
      )}
    </div>
  )
}
