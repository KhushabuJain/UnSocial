import { NavLink } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext.jsx'
import {
  LayoutDashboard, Users, Phone, MessageSquare,
  AlertTriangle, MapPin, Timer, LogOut, ShieldCheck, X
} from 'lucide-react'

const navItems = [
  { icon: LayoutDashboard, label: 'Dashboard',          path: '/dashboard' },
  { icon: Users,           label: 'Emergency Contacts', path: '/contacts' },
  { icon: Phone,           label: 'Fake Call',          path: '/fake-call' },
  { icon: MessageSquare,   label: 'Fake Message',       path: '/fake-message' },
  { icon: AlertTriangle,   label: 'SOS Alert',          path: '/sos',      danger: true },
  { icon: MapPin,          label: 'Live Tracking',      path: '/tracking' },
  { icon: Timer,           label: 'Safety Timer',       path: '/timer' },
]

export default function Sidebar({ open, onClose }) {
  const { user, logout } = useAuth()

  return (
    <>
      {/* Mobile overlay */}
      {open && (
        <div
          className="fixed inset-0 z-20 bg-black/60 backdrop-blur-sm lg:hidden"
          onClick={onClose}
        />
      )}

      {/* Sidebar panel */}
      <aside className={`
        fixed inset-y-0 left-0 z-30 w-64 flex flex-col
        dark:bg-surface-card bg-white
        border-r dark:border-surface-border border-slate-200
        transition-transform duration-300 ease-in-out
        ${open ? 'translate-x-0' : '-translate-x-full'}
        lg:translate-x-0 lg:static lg:z-auto
      `}>

        {/* Logo */}
        <div className="flex items-center justify-between px-5 py-5 border-b dark:border-surface-border border-slate-200">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-brand-600 flex items-center justify-center flex-shrink-0">
              <ShieldCheck className="w-5 h-5 text-white" />
            </div>
            <span className="font-bold text-lg dark:text-white text-slate-900 tracking-tight">
              UnSocial
            </span>
          </div>
          <button
            onClick={onClose}
            className="lg:hidden p-1.5 rounded-lg dark:hover:bg-surface-muted hover:bg-slate-100 dark:text-slate-400 text-slate-500"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Navigation */}
        <nav className="flex-1 px-3 py-4 space-y-0.5 overflow-y-auto">
          {navItems.map(({ icon: Icon, label, path, danger }) => (
            <NavLink
              key={path}
              to={path}
              onClick={onClose}
              className={({ isActive }) => `
                flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium
                transition-all duration-150 group
                ${isActive
                  ? danger
                    ? 'bg-red-50 dark:bg-red-500/10 text-red-600 dark:text-red-400'
                    : 'bg-brand-50 dark:bg-brand-600/10 text-brand-600 dark:text-brand-400'
                  : 'dark:text-slate-400 text-slate-600 dark:hover:bg-surface-muted hover:bg-slate-100 dark:hover:text-slate-200 hover:text-slate-900'
                }
              `}
            >
              <Icon className={`w-4 h-4 flex-shrink-0 ${danger ? 'group-[.active]:text-red-500' : ''}`} />
              {label}
              {danger && (
                <span className="ml-auto w-2 h-2 rounded-full bg-red-500 animate-pulse" />
              )}
            </NavLink>
          ))}
        </nav>

        {/* User + Logout */}
        <div className="px-3 py-4 border-t dark:border-surface-border border-slate-200">
          <div className="flex items-center gap-3 px-3 py-2.5 mb-1">
            <div className="w-8 h-8 rounded-full bg-brand-600 flex items-center justify-center flex-shrink-0">
              <span className="text-white text-xs font-bold">
                {user?.fullName?.charAt(0)?.toUpperCase() || 'U'}
              </span>
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm font-medium dark:text-slate-200 text-slate-800 truncate">
                {user?.fullName || 'User'}
              </p>
              <p className="text-xs dark:text-slate-500 text-slate-400 truncate">
                {user?.email}
              </p>
            </div>
          </div>

          <button
            onClick={logout}
            className="flex items-center gap-3 w-full px-3 py-2.5 rounded-xl text-sm font-medium
              dark:text-slate-400 text-slate-600 dark:hover:bg-surface-muted hover:bg-slate-100
              dark:hover:text-slate-200 transition-all duration-150"
          >
            <LogOut className="w-4 h-4" />
            Sign out
          </button>
        </div>
      </aside>
    </>
  )
}
