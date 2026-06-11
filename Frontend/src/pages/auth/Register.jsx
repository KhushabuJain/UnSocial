import { useState } from 'react'
import { Link, useNavigate, Navigate } from 'react-router-dom'
import { ShieldCheck } from 'lucide-react'
import { useAuth } from '../../context/AuthContext.jsx'
import { registerUser } from '../../api/auth.js'
import Button     from '../../components/ui/Button.jsx'
import Input      from '../../components/ui/Input.jsx'
import Alert      from '../../components/ui/Alert.jsx'
import ThemeToggle from '../../components/ui/ThemeToggle.jsx'

export default function Register() {
  const { login, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const [form, setForm]         = useState({ fullName: '', email: '', password: '', phone: '' })
  const [fieldErrors, setFieldErrors] = useState({})
  const [error, setError]       = useState('')
  const [loading, setLoading]   = useState(false)

  if (isAuthenticated) return <Navigate to="/dashboard" replace />

  const set = (field) => (e) => {
    setForm(f => ({ ...f, [field]: e.target.value }))
    setFieldErrors(fe => ({ ...fe, [field]: '' }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setFieldErrors({})
    setLoading(true)
    try {
      const res = await registerUser(form)
      login(res.data.data)
      navigate('/dashboard')
    } catch (err) {
      const data = err.response?.data
      if (data?.errors) {
        setFieldErrors(data.errors)
      } else {
        setError(data?.message || 'Registration failed. Please try again.')
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex dark:bg-surface bg-slate-50">

      {/* ── Left panel ───────────────────────── */}
      <div className="hidden lg:flex lg:w-[45%] flex-col items-center justify-center relative
        overflow-hidden bg-indigo-950 dark:bg-[#0b0b18]">
        <div className="absolute inset-0 flex items-center justify-center pointer-events-none">
          {[96, 72, 52].map((size, i) => (
            <div key={i}
              className="absolute rounded-full border border-violet-500/20 animate-ping-slow"
              style={{ width: `${size * 4}px`, height: `${size * 4}px`, animationDelay: `${i * 1.2}s` }}
            />
          ))}
        </div>
        <div className="relative z-10 text-center px-10">
          <div className="mx-auto mb-8 w-20 h-20 rounded-2xl
            bg-brand-600/20 border border-brand-500/30
            flex items-center justify-center backdrop-blur-sm">
            <ShieldCheck className="w-10 h-10 text-brand-400" />
          </div>
          <h1 className="text-4xl font-extrabold text-white mb-3 tracking-tight">Join UnSocial</h1>
          <p className="text-indigo-300 text-lg mb-10 max-w-xs">
            Set up your safety profile in under a minute.
          </p>
          <div className="space-y-3 text-sm text-left">
            {[
              ['✓', 'Free fake call & message templates'],
              ['✓', 'One-tap SOS to trusted contacts'],
              ['✓', 'Live location sharing links'],
              ['✓', 'Auto-expiring safety timers'],
            ].map(([icon, text]) => (
              <div key={text} className="flex items-center gap-3 text-indigo-300">
                <span className="text-brand-400 font-bold">{icon}</span>
                {text}
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* ── Right panel — form ───────────────── */}
      <div className="flex-1 flex flex-col items-center justify-center p-6 relative">
        <div className="absolute top-4 right-4">
          <ThemeToggle />
        </div>

        <div className="w-full max-w-md animate-slide-up">

          <div className="flex lg:hidden items-center gap-3 mb-8">
            <div className="w-10 h-10 rounded-xl bg-brand-600 flex items-center justify-center">
              <ShieldCheck className="w-5 h-5 text-white" />
            </div>
            <span className="text-xl font-bold dark:text-white text-slate-900">UnSocial</span>
          </div>

          <h2 className="text-3xl font-extrabold dark:text-white text-slate-900 mb-1">
            Create your account
          </h2>
          <p className="dark:text-slate-400 text-slate-500 text-sm mb-8">
            Start protecting yourself today
          </p>

          {error && <Alert type="error" message={error} className="mb-5" />}

          <form onSubmit={handleSubmit} className="space-y-4">
            <Input label="Full name" placeholder="Jane Doe"
              value={form.fullName} onChange={set('fullName')}
              error={fieldErrors.fullName} required autoComplete="name" />

            <Input label="Email address" type="email" placeholder="you@example.com"
              value={form.email} onChange={set('email')}
              error={fieldErrors.email} required autoComplete="email" />

            <Input label="Password" type="password" placeholder="Min. 8 characters"
              value={form.password} onChange={set('password')}
              error={fieldErrors.password} required autoComplete="new-password"
              hint="Must contain uppercase, lowercase, and a number" />

            <Input label="Phone number" type="tel" placeholder="9876543210"
              value={form.phone} onChange={set('phone')}
              error={fieldErrors.phone} required autoComplete="tel" />

            <Button type="submit" fullWidth size="lg" loading={loading} className="mt-2">
              Create account
            </Button>
          </form>

          <p className="mt-6 text-center text-sm dark:text-slate-400 text-slate-500">
            Already have an account?{' '}
            <Link to="/login" className="text-brand-500 hover:text-brand-400 font-semibold">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
