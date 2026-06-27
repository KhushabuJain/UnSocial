import { Menu } from 'lucide-react'
import ThemeToggle from '../ui/ThemeToggle.jsx'
import { useLocation } from 'react-router-dom'

const pageTitles = {
  '/dashboard':    'Dashboard',
  '/contacts':     'Emergency Contacts',
  '/fake-call':    'Fake Call',
  '/fake-message': 'Fake Message',
  '/sos':          'SOS Alert',
  '/tracking':     'Live Tracking',
  '/timer':        'Safety Timer',
}

export default function Navbar({ onMenuClick }) {
  const { pathname } = useLocation()
  const title = pageTitles[pathname] || 'UnSocial'

  return (
      <header className="sticky top-0 z-10 flex items-center justify-between
      px-5 h-16 border-b
      dark:bg-surface/80 bg-white/70 backdrop-blur-md
      dark:border-surface-border border-slate-200/70">

        {/* Left: hamburger + page title */}
        <div className="flex items-center gap-4">
          <button
              onClick={onMenuClick}
              className="lg:hidden p-2 rounded-xl dark:hover:bg-surface-muted hover:bg-slate-100
            dark:text-slate-400 text-slate-600 transition-colors"
              aria-label="Open menu"
          >
            <Menu className="w-5 h-5" />
          </button>
          <h1 className="text-base font-semibold dark:text-white text-slate-900">
            {title}
          </h1>
        </div>

        {/* Right: theme toggle */}
        <ThemeToggle />
      </header>
  )
}
