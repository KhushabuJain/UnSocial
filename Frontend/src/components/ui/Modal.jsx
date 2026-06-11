import { useEffect } from 'react'
import { X } from 'lucide-react'

export default function Modal({ title, children, onClose, size = 'md' }) {
  useEffect(() => {
    const onKey = (e) => e.key === 'Escape' && onClose()
    document.addEventListener('keydown', onKey)
    return () => document.removeEventListener('keydown', onKey)
  }, [onClose])

  return (
    <div
      className="fixed inset-0 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4
        bg-black/60 backdrop-blur-sm animate-fade-in"
      onClick={onClose}
    >
      <div
        onClick={e => e.stopPropagation()}
        className={`card w-full ${size === 'lg' ? 'sm:max-w-lg' : 'sm:max-w-md'}
          rounded-t-2xl sm:rounded-2xl animate-slide-up`}
      >
        <div className="flex items-center justify-between px-5 pt-5 pb-4
          border-b dark:border-surface-border border-slate-100">
          <h3 className="font-semibold dark:text-white text-slate-900">{title}</h3>
          <button onClick={onClose}
            className="p-1.5 rounded-xl dark:hover:bg-surface-muted hover:bg-slate-100
              dark:text-slate-400 text-slate-500 transition-colors">
            <X className="w-4 h-4" />
          </button>
        </div>
        <div className="p-5">{children}</div>
      </div>
    </div>
  )
}
