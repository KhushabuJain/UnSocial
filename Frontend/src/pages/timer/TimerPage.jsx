import { useState, useEffect, useRef } from 'react'
import { Timer, CheckCircle, XCircle, Clock } from 'lucide-react'
import { startTimer, checkIn, cancelTimer, getActiveTimer, getHistory } from '../../api/timer.js'
import Button from '../../components/ui/Button.jsx'
import Input  from '../../components/ui/Input.jsx'
import Alert  from '../../components/ui/Alert.jsx'
import Badge  from '../../components/ui/Badge.jsx'

const fmt     = (d)       => new Date(d).toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' })
const fmtSecs = (seconds) => {
  const h = Math.floor(seconds / 3600)
  const m = Math.floor((seconds % 3600) / 60)
  const s = seconds % 60
  if (h > 0) return `${h}:${String(m).padStart(2,'0')}:${String(s).padStart(2,'0')}`
  return `${String(m).padStart(2,'0')}:${String(s).padStart(2,'0')}`
}

export default function TimerPage() {
  const [active,    setActive]   = useState(null)
  const [history,   setHistory]  = useState([])
  const [loading,   setLoading]  = useState(true)
  const [duration,  setDuration] = useState(30)
  const [note,      setNote]     = useState('')
  const [starting,  setStarting] = useState(false)
  const [remaining, setRemaining]= useState(0)
  const [toast,     setToast]    = useState({ msg: '', type: 'success' })
  const intervalRef = useRef(null)

  const showToast = (msg, type = 'success') => { setToast({ msg, type }); setTimeout(() => setToast({ msg:'', type:'success' }), 4000) }

  const load = async () => {
    setLoading(true)
    try {
      const [aRes, hRes] = await Promise.allSettled([getActiveTimer(), getHistory()])
      if (aRes.status === 'fulfilled') {
        const t = aRes.value.data.data
        setActive(t); setRemaining(t.remainingSeconds || 0)
      } else { setActive(null); setRemaining(0) }
      if (hRes.status === 'fulfilled') setHistory(hRes.value.data.data || [])
    } finally { setLoading(false) }
  }
  useEffect(() => { load() }, [])

  // Countdown tick
  useEffect(() => {
    clearInterval(intervalRef.current)
    if (active?.status === 'ACTIVE' && remaining > 0) {
      intervalRef.current = setInterval(() => setRemaining(r => {
        if (r <= 1) { clearInterval(intervalRef.current); load(); return 0 }
        return r - 1
      }), 1000)
    }
    return () => clearInterval(intervalRef.current)
  }, [active?.id])

  const pct = active ? Math.max(0, Math.min(100, (remaining / (active.durationMinutes * 60)) * 100)) : 0
  const circumference = 2 * Math.PI * 54
  const strokeDash    = circumference - (pct / 100) * circumference

  const urgency = pct > 50 ? 'text-emerald-500' : pct > 20 ? 'text-amber-500' : 'text-red-500'
  const ringColor = pct > 50 ? '#22c55e' : pct > 20 ? '#f59e0b' : '#ef4444'

  const handleStart = async (e) => {
    e.preventDefault(); setStarting(true)
    try {
      await startTimer({ durationMinutes: parseInt(duration), note })
      showToast(`Timer set for ${duration} minutes. Check in before it expires!`)
      setNote('')
      load()
    } catch (err) { showToast(err.response?.data?.message || 'Failed to start timer', 'error') }
    finally { setStarting(false) }
  }

  const handleCheckIn = async () => {
    try { await checkIn(active.id); showToast('✅ Checked in! You are marked safe.'); load() }
    catch (err) { showToast(err.response?.data?.message || 'Failed', 'error') }
  }

  const handleCancel = async () => {
    if (!confirm('Cancel this safety timer?')) return
    try { await cancelTimer(active.id); showToast('Timer cancelled'); load() }
    catch (err) { showToast(err.response?.data?.message || 'Failed', 'error') }
  }

  return (
      <div className="max-w-2xl mx-auto space-y-6 animate-slide-up">
        <div>
          <h2 className="text-lg font-bold dark:text-white text-slate-900">Safety Timer</h2>
          <p className="text-sm dark:text-slate-400 text-slate-500 mt-0.5">
            If you don't check in before the timer expires, your contacts are alerted automatically
          </p>
        </div>

        {toast.msg && <Alert type={toast.type} message={toast.msg} />}

        {loading ? (
            <div className="card p-8 animate-pulse flex justify-center">
              <div className="w-40 h-40 rounded-full bg-slate-200 dark:bg-surface-muted" />
            </div>
        ) : active?.status === 'ACTIVE' ? (
            /* ── Active timer ──────────────────── */
            <div className="card p-6 flex flex-col items-center gap-5">
              {/* Circular countdown */}
              <div className="relative w-44 h-44">
                <svg className="w-full h-full -rotate-90" viewBox="0 0 120 120">
                  <circle cx="60" cy="60" r="54" fill="none" stroke="currentColor"
                          className="dark:text-surface-muted text-slate-100" strokeWidth="8" />
                  <circle cx="60" cy="60" r="54" fill="none"
                          stroke={ringColor} strokeWidth="8"
                          strokeLinecap="round"
                          strokeDasharray={circumference}
                          strokeDashoffset={strokeDash}
                          style={{ transition: 'stroke-dashoffset 1s linear, stroke 0.5s' }} />
                </svg>
                <div className="absolute inset-0 flex flex-col items-center justify-center">
              <span className={`text-3xl font-mono font-extrabold ${urgency}`}>
                {fmtSecs(remaining)}
              </span>
                  <span className="text-xs dark:text-slate-500 text-slate-400 mt-1">remaining</span>
                </div>
              </div>

              {active.note && (
                  <p className="text-sm dark:text-slate-400 text-slate-500 italic text-center">"{active.note}"</p>
              )}

              <div className="flex gap-3 w-full">
                <Button onClick={handleCheckIn} fullWidth size="lg"
                        className="!bg-emerald-500 hover:!bg-emerald-600 text-white">
                  <CheckCircle className="w-4 h-4" /> I'm safe — Check in
                </Button>
                <Button onClick={handleCancel} variant="ghost" size="lg">
                  <XCircle className="w-4 h-4" />
                </Button>
              </div>
              <p className="text-xs dark:text-slate-500 text-slate-400 text-center">
                Expires at {fmt(active.expiresAt)}
              </p>
            </div>
        ) : (
            /* ── Start timer form ──────────────── */
            <div className="card p-6">
              <form onSubmit={handleStart} className="space-y-5">
                <div>
                  <label className="text-sm font-medium dark:text-slate-300 text-slate-700 block mb-3">
                    Duration
                  </label>
                  <div className="grid grid-cols-4 gap-2 mb-3">
                    {[15, 30, 60, 120].map(m => (
                        <button key={m} type="button" onClick={() => setDuration(m)}
                                className={`py-2 rounded-xl text-sm font-semibold transition-all
                      ${duration === m
                                    ? 'bg-brand-600 text-white'
                                    : 'dark:bg-surface-muted bg-slate-100 dark:text-slate-300 text-slate-600 dark:hover:bg-surface-border hover:bg-slate-200'}`}>
                          {m < 60 ? `${m}m` : `${m/60}h`}
                        </button>
                    ))}
                  </div>
                  <Input type="number" placeholder="Custom duration in minutes"
                         value={duration} onChange={e => setDuration(e.target.value)}
                         hint="Between 1 and 480 minutes" />
                </div>

                <Input label="Note (optional)" placeholder="Walking home from the station alone…"
                       value={note} onChange={e => setNote(e.target.value)} />

                <Button type="submit" fullWidth size="lg" loading={starting}>
                  <Timer className="w-4 h-4" /> Start safety timer
                </Button>
              </form>
            </div>
        )}

        {/* History */}
        {history.filter(t => t.status !== 'ACTIVE').length > 0 && (
            <section>
              <h3 className="text-xs font-semibold uppercase tracking-widest dark:text-slate-500 text-slate-400 mb-3">
                Timer history
              </h3>
              <div className="card divide-y dark:divide-surface-border divide-slate-100">
                {history.filter(t => t.status !== 'ACTIVE').map(t => (
                    <div key={t.id} className="flex items-center gap-3 px-4 py-3">
                      <Clock className="w-4 h-4 dark:text-slate-500 text-slate-400 flex-shrink-0" />
                      <div className="flex-1">
                        <p className="text-sm dark:text-slate-300 text-slate-700 font-medium">
                          {t.durationMinutes < 60 ? `${t.durationMinutes}m` : `${t.durationMinutes/60}h`}
                          {t.note && <span className="font-normal dark:text-slate-500 text-slate-400"> · {t.note}</span>}
                        </p>
                        <p className="text-xs dark:text-slate-500 text-slate-400">{fmt(t.startedAt)}</p>
                      </div>
                      <Badge label={t.status} type={t.status.toLowerCase()} />
                    </div>
                ))}
              </div>
            </section>
        )}
      </div>
  )
}
