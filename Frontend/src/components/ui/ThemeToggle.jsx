import { useTheme } from '../../context/ThemeContext.jsx'
import { Sun, Moon } from 'lucide-react'

export default function ThemeToggle({ className = '' }) {
  const { toggleTheme, isDark } = useTheme()

  return (
      <button
          onClick={toggleTheme}
          aria-label="Toggle theme"
          className={`p-2 rounded-xl transition-all duration-200 
        dark:bg-surface-muted dark:hover:bg-surface-border dark:text-slate-300
        bg-slate-100 hover:bg-slate-200 text-slate-600
        focus:outline-none focus:ring-2 focus:ring-brand-500 ${className}`}
      >
        {isDark
            ? <Sun  className="w-4 h-4" />
            : <Moon className="w-4 h-4" />}
      </button>
  )
}
