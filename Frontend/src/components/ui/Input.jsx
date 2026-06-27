export default function Input({
                                  label, type = 'text', placeholder, value,
                                  onChange, required = false, error = '',
                                  hint = '', name, autoComplete,
                              }) {
    return (
        <div className="flex flex-col gap-1.5">
            {label && (
                <label className="text-sm font-medium dark:text-slate-300 text-slate-700">
                    {label} {required && <span className="text-red-500">*</span>}
                </label>
            )}
            <input
                type={type}
                name={name}
                placeholder={placeholder}
                value={value}
                onChange={onChange}
                required={required}
                autoComplete={autoComplete}
                className={`input-base ${error ? 'border-red-500 focus:border-red-500 focus:ring-red-500/20' : ''}`}
            />
            {error && <p className="text-xs text-red-500 mt-0.5">{error}</p>}
            {hint && !error && <p className="text-xs dark:text-slate-500 text-slate-400">{hint}</p>}
        </div>
    )
}
