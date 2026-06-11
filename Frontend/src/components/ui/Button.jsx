export default function Button({
  children, type = 'button', onClick,
  variant = 'primary', size = 'md',
  fullWidth = false, loading = false,
  disabled = false, className = '', danger = false,
}) {
  const base = 'inline-flex items-center justify-center gap-2 font-semibold rounded-xl transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed'

  const variants = {
    primary: 'bg-brand-600 hover:bg-brand-700 text-white focus:ring-brand-500 dark:focus:ring-offset-surface',
    secondary: 'bg-slate-100 hover:bg-slate-200 text-slate-700 dark:bg-surface-muted dark:hover:bg-surface-border dark:text-slate-200 focus:ring-slate-400',
    ghost: 'bg-transparent hover:bg-slate-100 dark:hover:bg-surface-muted text-slate-600 dark:text-slate-300 focus:ring-slate-400',
    danger: 'bg-red-500 hover:bg-red-600 text-white focus:ring-red-500 dark:focus:ring-offset-surface',
    outline: 'border border-slate-200 dark:border-surface-border hover:bg-slate-50 dark:hover:bg-surface-muted text-slate-700 dark:text-slate-200 focus:ring-brand-500',
  }

  const sizes = {
    sm: 'px-3 py-2 text-xs',
    md: 'px-5 py-3 text-sm',
    lg: 'px-6 py-3.5 text-base',
  }

  const finalVariant = danger ? 'danger' : variant

  return (
    <button
      type={type}
      onClick={onClick}
      disabled={disabled || loading}
      className={`${base} ${variants[finalVariant]} ${sizes[size]} ${fullWidth ? 'w-full' : ''} ${className}`}
    >
      {loading && (
        <svg className="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
        </svg>
      )}
      {children}
    </button>
  )
}
