import { useAuth } from '../../context/AuthContext.jsx'
import { useNavigate } from 'react-router-dom'
import {
  AlertTriangle, Phone, MapPin, Timer,
  MessageSquare, Users, ShieldCheck, ChevronRight
} from 'lucide-react'

const quickActions = [
  {
    icon: AlertTriangle,
    label: 'SOS Alert',
    desc:  'Send emergency alert instantly',
    path:  '/sos',
    color: 'bg-red-500 hover:bg-red-600',
    ring:  'focus:ring-red-400',
    glow:  'shadow-red-500/20',
    dark:  'dark:shadow-red-500/10',
  },
  {
    icon: Phone,
    label: 'Fake Call',
    desc:  'Trigger a fake incoming call',
    path:  '/fake-call',
    color: 'bg-brand-600 hover:bg-brand-700',
    ring:  'focus:ring-brand-400',
    glow:  'shadow-brand-500/20',
    dark:  'dark:shadow-brand-500/10',
  },
  {
    icon: MapPin,
    label: 'Live Tracking',
    desc:  'Share your location live',
    path:  '/tracking',
    color: 'bg-emerald-500 hover:bg-emerald-600',
    ring:  'focus:ring-emerald-400',
    glow:  'shadow-emerald-500/20',
    dark:  'dark:shadow-emerald-500/10',
  },
  {
    icon: Timer,
    label: 'Safety Timer',
    desc:  'Set a check-in countdown',
    path:  '/timer',
    color: 'bg-amber-500 hover:bg-amber-600',
    ring:  'focus:ring-amber-400',
    glow:  'shadow-amber-500/20',
    dark:  'dark:shadow-amber-500/10',
  },
]

const moreFeatures = [
  { icon: MessageSquare, label: 'Fake Message',       path: '/fake-message', desc: 'Send a fake emergency text' },
  { icon: Users,         label: 'Emergency Contacts', path: '/contacts',     desc: 'Manage your trusted contacts' },
]

export default function Dashboard() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const hour     = new Date().getHours()
  const greeting = hour < 12 ? 'Good morning' : hour < 17 ? 'Good afternoon' : 'Good evening'

  return (
    <div className="max-w-4xl mx-auto space-y-8 animate-slide-up">

      {/* ── Header ─────────────────────────────── */}
      <div className="flex items-center gap-4">
        <div className="w-12 h-12 rounded-2xl bg-brand-600 flex items-center justify-center flex-shrink-0 shadow-lg shadow-brand-500/25">
          <ShieldCheck className="w-6 h-6 text-white" />
        </div>
        <div>
          <h1 className="text-xl font-bold dark:text-white text-slate-900">
            {greeting}, {user?.fullName?.split(' ')[0]} 👋
          </h1>
          <p className="text-sm dark:text-slate-400 text-slate-500">
            Your safety tools are ready. Stay safe out there.
          </p>
        </div>
      </div>

      {/* ── Quick Actions ──────────────────────── */}
      <section>
        <h2 className="text-xs font-semibold uppercase tracking-widest dark:text-slate-500 text-slate-400 mb-3">
          Quick Actions
        </h2>
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
          {quickActions.map(({ icon: Icon, label, desc, path, color, ring, glow, dark }) => (
            <button
              key={path}
              onClick={() => navigate(path)}
              className={`
                flex flex-col items-center text-center gap-3 p-5 rounded-2xl text-white
                transition-all duration-200 active:scale-95
                shadow-lg ${glow} ${dark}
                focus:outline-none focus:ring-2 ${ring} focus:ring-offset-2
                dark:focus:ring-offset-surface
                ${color}
              `}
            >
              <Icon className="w-6 h-6" />
              <div>
                <p className="font-semibold text-sm">{label}</p>
                <p className="text-white/70 text-xs mt-0.5 hidden sm:block">{desc}</p>
              </div>
            </button>
          ))}
        </div>
      </section>

      {/* ── Status Cards ──────────────────────── */}
      <section>
        <h2 className="text-xs font-semibold uppercase tracking-widest dark:text-slate-500 text-slate-400 mb-3">
          Active Status
        </h2>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
          {[
            { label: 'Active Tracker',   value: 'None',     dot: 'bg-slate-400' },
            { label: 'Safety Timer',     value: 'Not set',  dot: 'bg-slate-400' },
            { label: 'SOS Alert',        value: 'All clear',dot: 'bg-emerald-500' },
          ].map(({ label, value, dot }) => (
            <div key={label} className="card p-4 flex items-center gap-3">
              <div className={`w-2.5 h-2.5 rounded-full flex-shrink-0 ${dot}`} />
              <div>
                <p className="text-xs dark:text-slate-500 text-slate-400">{label}</p>
                <p className="text-sm font-semibold dark:text-slate-200 text-slate-700 mt-0.5">{value}</p>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* ── More Features ─────────────────────── */}
      <section>
        <h2 className="text-xs font-semibold uppercase tracking-widest dark:text-slate-500 text-slate-400 mb-3">
          More Features
        </h2>
        <div className="card divide-y dark:divide-surface-border divide-slate-100">
          {moreFeatures.map(({ icon: Icon, label, path, desc }) => (
            <button
              key={path}
              onClick={() => navigate(path)}
              className="flex items-center gap-4 p-4 w-full text-left
                dark:hover:bg-surface-muted hover:bg-slate-50
                transition-colors duration-150 first:rounded-t-2xl last:rounded-b-2xl"
            >
              <div className="w-9 h-9 rounded-xl dark:bg-surface-muted bg-slate-100
                flex items-center justify-center flex-shrink-0">
                <Icon className="w-4 h-4 dark:text-slate-300 text-slate-600" />
              </div>
              <div className="flex-1">
                <p className="text-sm font-semibold dark:text-slate-200 text-slate-800">{label}</p>
                <p className="text-xs dark:text-slate-500 text-slate-400 mt-0.5">{desc}</p>
              </div>
              <ChevronRight className="w-4 h-4 dark:text-slate-600 text-slate-400" />
            </button>
          ))}
        </div>
      </section>
    </div>
  )
}
