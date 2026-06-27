import Button from './Button.jsx'

export default function EmptyState({ icon, title, desc, action, onAction }) {
    return (
        <div className="flex flex-col items-center justify-center py-16 text-center">
            <div className="text-5xl mb-4">{icon}</div>
            <h3 className="text-base font-semibold dark:text-slate-300 text-slate-700 mb-1">{title}</h3>
            <p className="text-sm dark:text-slate-500 text-slate-400 mb-5 max-w-xs">{desc}</p>
            {action && <Button onClick={onAction}>{action}</Button>}
        </div>
    )
}
