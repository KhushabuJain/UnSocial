const styles = {
  error:   'bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800 text-red-700 dark:text-red-400',
  success: 'bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 text-green-700 dark:text-green-400',
  info:    'bg-brand-50 dark:bg-brand-900/20 border border-brand-200 dark:border-brand-800 text-brand-700 dark:text-brand-400',
  warning: 'bg-amber-50 dark:bg-amber-900/20 border border-amber-200 dark:border-amber-800 text-amber-700 dark:text-amber-400',
}

const icons = {
  error:   '✕',
  success: '✓',
  info:    'ℹ',
  warning: '⚠',
}

export default function Alert({ type = 'info', message, className = '' }) {
  if (!message) return null
  return (
    <div className={`flex items-start gap-3 p-3.5 rounded-xl text-sm font-medium animate-fade-in ${styles[type]} ${className}`}>
      <span className="flex-shrink-0 font-bold">{icons[type]}</span>
      <span>{message}</span>
    </div>
  )
}
