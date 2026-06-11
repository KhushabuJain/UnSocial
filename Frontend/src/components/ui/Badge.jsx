const styles = {
  active:    'bg-emerald-100 dark:bg-emerald-900/30 text-emerald-700 dark:text-emerald-400',
  stopped:   'bg-slate-100 dark:bg-slate-700/40 text-slate-600 dark:text-slate-400',
  resolved:  'bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400',
  cancelled: 'bg-slate-100 dark:bg-slate-700/40 text-slate-500 dark:text-slate-500',
  expired:   'bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400',
  completed: 'bg-emerald-100 dark:bg-emerald-900/30 text-emerald-700 dark:text-emerald-400',
  emergency: 'bg-red-100 dark:bg-red-900/30 text-red-600 dark:text-red-400',
  work:      'bg-blue-100 dark:bg-blue-900/30 text-blue-700 dark:text-blue-400',
  casual:    'bg-amber-100 dark:bg-amber-900/30 text-amber-700 dark:text-amber-400',
  custom:    'bg-purple-100 dark:bg-purple-900/30 text-purple-700 dark:text-purple-400',
  default:   'bg-brand-100 dark:bg-brand-900/30 text-brand-700 dark:text-brand-400',
}

export default function Badge({ label, type = 'default' }) {
  const key = (label || type).toLowerCase()
  const cls = styles[key] || styles.default
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium ${cls}`}>
      {label}
    </span>
  )
}
