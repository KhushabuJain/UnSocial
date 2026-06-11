import { useState, useEffect, useRef } from 'react'
import { Plus, Pencil, Trash2, Phone, PhoneOff, Star, Zap } from 'lucide-react'
import { getTemplates, createTemplate, updateTemplate, deleteTemplate, setDefaultTemplate, triggerFakeCall } from '../../api/fakeCall.js'
import Button    from '../../components/ui/Button.jsx'
import Input     from '../../components/ui/Input.jsx'
import Alert     from '../../components/ui/Alert.jsx'
import Modal     from '../../components/ui/Modal.jsx'
import Badge     from '../../components/ui/Badge.jsx'
import EmptyState from '../../components/ui/EmptyState.jsx'

const EMPTY = { callerName: '', callerPhone: '', delaySeconds: 5, makeDefault: false }

export default function FakeCallPage() {
  const [templates, setTemplates] = useState([])
  const [loading, setLoading]     = useState(true)
  const [modal, setModal]         = useState(false)
  const [editing, setEditing]     = useState(null)
  const [form, setForm]           = useState(EMPTY)
  const [saving, setSaving]       = useState(false)
  const [error, setError]         = useState('')
  const [toast, setToast]         = useState('')
  // Call overlay state
  const [callDetails, setCallDetails]   = useState(null)
  const [countdown, setCountdown]       = useState(null)
  const [showCall, setShowCall]         = useState(false)
  const intervalRef = useRef(null)

  const load = async () => {
    try { const r = await getTemplates(); setTemplates(r.data.data || []) }
    catch { /* ignore */ } finally { setLoading(false) }
  }
  useEffect(() => { load() }, [])

  const showToast = (m) => { setToast(m); setTimeout(() => setToast(''), 3000) }
  const set = (f) => (e) => setForm(p => ({ ...p, [f]: e.target.type === 'checkbox' ? e.target.checked : e.target.value }))

  const openAdd  = () => { setEditing(null); setForm(EMPTY); setError(''); setModal(true) }
  const openEdit = (t) => { setEditing(t); setForm({ callerName: t.callerName, callerPhone: t.callerPhone || '', delaySeconds: t.delaySeconds, makeDefault: t.default }); setError(''); setModal(true) }

  const handleSave = async (e) => {
    e.preventDefault(); setSaving(true); setError('')
    try {
      const payload = { ...form, delaySeconds: parseInt(form.delaySeconds) || 0 }
      if (editing) { await updateTemplate(editing.id, payload); showToast('Template updated') }
      else { await createTemplate(payload); showToast('Template created') }
      setModal(false); load()
    } catch (err) { setError(err.response?.data?.message || 'Something went wrong') }
    finally { setSaving(false) }
  }

  const handleDelete = async (id) => {
    if (!confirm('Delete this template?')) return
    try { await deleteTemplate(id); showToast('Template deleted'); load() }
    catch { showToast('Failed to delete') }
  }

  const handleSetDefault = async (id) => {
    try { await setDefaultTemplate(id); showToast('Default updated'); load() }
    catch { showToast('Failed to update') }
  }

  const handleTrigger = async (templateId = null) => {
    try {
      const res = await triggerFakeCall({ templateId })
      const { callerName, callerPhone, delaySeconds } = res.data.data
      setCallDetails({ callerName, callerPhone })
      clearInterval(intervalRef.current)
      if (delaySeconds <= 0) { setShowCall(true) }
      else {
        setCountdown(delaySeconds)
        intervalRef.current = setInterval(() => {
          setCountdown(c => {
            if (c <= 1) { clearInterval(intervalRef.current); setShowCall(true); return null }
            return c - 1
          })
        }, 1000)
      }
    } catch (err) { showToast(err.response?.data?.message || 'No default template set') }
  }

  const dismissCall = () => {
    clearInterval(intervalRef.current)
    setShowCall(false); setCountdown(null); setCallDetails(null)
  }

  return (
    <div className="max-w-2xl mx-auto space-y-6 animate-slide-up">

      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-lg font-bold dark:text-white text-slate-900">Fake Call</h2>
          <p className="text-sm dark:text-slate-400 text-slate-500 mt-0.5">Trigger a fake incoming call to escape any situation</p>
        </div>
        <Button onClick={openAdd} size="sm"><Plus className="w-4 h-4" /> New Template</Button>
      </div>

      {toast && <Alert type="success" message={toast} />}

      {/* Quick trigger */}
      <div className="card p-5">
        <p className="text-sm font-medium dark:text-slate-300 text-slate-700 mb-3">Quick trigger</p>
        <Button onClick={() => handleTrigger(null)} size="lg" fullWidth>
          <Phone className="w-4 h-4" /> Trigger fake call now
        </Button>
        <p className="text-xs dark:text-slate-500 text-slate-400 text-center mt-2">Uses your default template</p>
      </div>

      {/* Templates */}
      {loading ? (
        <div className="space-y-3">
          {[1,2].map(i => <div key={i} className="card p-4 animate-pulse h-16" />)}
        </div>
      ) : templates.length === 0 ? (
        <EmptyState icon="📞" title="No call templates yet"
          desc="Create a template with a caller name and delay time."
          action="Create first template" onAction={openAdd} />
      ) : (
        <div className="card divide-y dark:divide-surface-border divide-slate-100">
          {templates.map(t => (
            <div key={t.id} className="flex items-center gap-3 p-4 group">
              <div className="w-10 h-10 rounded-full bg-brand-100 dark:bg-brand-900/30 flex items-center justify-center flex-shrink-0">
                <Phone className="w-4 h-4 text-brand-600 dark:text-brand-400" />
              </div>
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2">
                  <p className="font-semibold dark:text-slate-200 text-slate-800 text-sm">{t.callerName}</p>
                  {t.default && <Badge label="Default" type="default" />}
                </div>
                <p className="text-xs dark:text-slate-500 text-slate-400 mt-0.5">
                  {t.callerPhone || 'No number'} · Rings in {t.delaySeconds}s
                </p>
              </div>
              <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                <button onClick={() => handleTrigger(t.id)} title="Trigger call"
                  className="p-2 rounded-xl dark:hover:bg-surface-muted hover:bg-slate-100 text-brand-500 transition-colors">
                  <Zap className="w-3.5 h-3.5" />
                </button>
                {!t.default && (
                  <button onClick={() => handleSetDefault(t.id)} title="Set as default"
                    className="p-2 rounded-xl dark:hover:bg-surface-muted hover:bg-slate-100 dark:text-slate-400 text-slate-500 transition-colors">
                    <Star className="w-3.5 h-3.5" />
                  </button>
                )}
                <button onClick={() => openEdit(t)}
                  className="p-2 rounded-xl dark:hover:bg-surface-muted hover:bg-slate-100 dark:text-slate-400 text-slate-500 transition-colors">
                  <Pencil className="w-3.5 h-3.5" />
                </button>
                <button onClick={() => handleDelete(t.id)}
                  className="p-2 rounded-xl hover:bg-red-50 dark:hover:bg-red-900/20 text-red-500 transition-colors">
                  <Trash2 className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Template Modal */}
      {modal && (
        <Modal title={editing ? 'Edit Template' : 'New Call Template'} onClose={() => setModal(false)}>
          {error && <Alert type="error" message={error} className="mb-4" />}
          <form onSubmit={handleSave} className="space-y-4">
            <Input label="Caller name" placeholder="Mom, Boss, Doctor…" value={form.callerName} onChange={set('callerName')} required />
            <Input label="Phone number (optional)" type="tel" placeholder="9876543210" value={form.callerPhone} onChange={set('callerPhone')} />
            <Input label="Delay (seconds)" type="number" placeholder="5" value={form.delaySeconds} onChange={set('delaySeconds')}
              hint="How many seconds before the call starts ringing" />
            <label className="flex items-center gap-3 cursor-pointer">
              <input type="checkbox" checked={form.makeDefault} onChange={set('makeDefault')}
                className="w-4 h-4 rounded accent-brand-600" />
              <span className="text-sm dark:text-slate-300 text-slate-700">Set as default template</span>
            </label>
            <div className="flex gap-3 pt-1">
              <Button type="button" variant="secondary" fullWidth onClick={() => setModal(false)}>Cancel</Button>
              <Button type="submit" fullWidth loading={saving}>{editing ? 'Save changes' : 'Create template'}</Button>
            </div>
          </form>
        </Modal>
      )}

      {/* Countdown overlay */}
      {countdown !== null && !showCall && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 backdrop-blur-sm">
          <div className="text-center animate-fade-in">
            <div className="relative w-28 h-28 mx-auto mb-6">
              <div className="absolute inset-0 rounded-full border-4 border-brand-600/30 animate-ping-slow" />
              <div className="w-full h-full rounded-full bg-brand-600/20 border-2 border-brand-500 flex items-center justify-center">
                <span className="text-4xl font-extrabold text-white">{countdown}</span>
              </div>
            </div>
            <p className="text-white font-medium">Call incoming in…</p>
            <button onClick={dismissCall} className="mt-4 text-slate-400 text-sm hover:text-white">Cancel</button>
          </div>
        </div>
      )}

      {/* Fake call screen */}
      {showCall && callDetails && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-gradient-to-b from-slate-900 to-slate-800 animate-fade-in">
          <div className="text-center px-8">
            <p className="text-slate-400 text-sm font-medium mb-8 uppercase tracking-widest">Incoming call</p>
            <div className="w-24 h-24 rounded-full bg-brand-600 flex items-center justify-center mx-auto mb-4 shadow-2xl shadow-brand-500/40 animate-pulse-slow">
              <span className="text-white text-4xl font-bold">{callDetails.callerName?.charAt(0)}</span>
            </div>
            <h2 className="text-3xl font-extrabold text-white mb-1">{callDetails.callerName}</h2>
            <p className="text-slate-400 mb-14">{callDetails.callerPhone || 'Mobile'}</p>
            <div className="flex gap-12 justify-center">
              <button onClick={dismissCall}
                className="w-16 h-16 rounded-full bg-red-500 hover:bg-red-600 flex items-center justify-center transition-colors shadow-lg shadow-red-500/40">
                <PhoneOff className="w-7 h-7 text-white" />
              </button>
              <button onClick={dismissCall}
                className="w-16 h-16 rounded-full bg-emerald-500 hover:bg-emerald-600 flex items-center justify-center transition-colors shadow-lg shadow-emerald-500/40">
                <Phone className="w-7 h-7 text-white" />
              </button>
            </div>
            <p className="text-slate-600 text-xs mt-10">Tap either button to dismiss</p>
          </div>
        </div>
      )}
    </div>
  )
}
