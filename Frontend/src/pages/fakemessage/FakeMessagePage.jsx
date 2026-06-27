import { useState, useEffect } from 'react'
import { Plus, Pencil, Trash2, MessageSquare, Star, Zap, X } from 'lucide-react'
import { getTemplates, createTemplate, updateTemplate, deleteTemplate, setDefaultTemplate, triggerFakeMessage } from '../../api/fakeMessage.js'
import Button    from '../../components/ui/Button.jsx'
import Input     from '../../components/ui/Input.jsx'
import Alert     from '../../components/ui/Alert.jsx'
import Modal     from '../../components/ui/Modal.jsx'
import Badge     from '../../components/ui/Badge.jsx'
import EmptyState from '../../components/ui/EmptyState.jsx'

const TYPES = ['EMERGENCY','WORK','CASUAL','CUSTOM']
const EMPTY = { senderName: '', senderPhone: '', messageContent: '', messageType: 'CUSTOM', makeDefault: false }

export default function FakeMessagePage() {
  const [templates, setTemplates] = useState([])
  const [loading, setLoading]     = useState(true)
  const [modal, setModal]         = useState(false)
  const [editing, setEditing]     = useState(null)
  const [form, setForm]           = useState(EMPTY)
  const [saving, setSaving]       = useState(false)
  const [error, setError]         = useState('')
  const [toast, setToast]         = useState('')
  const [notification, setNotification] = useState(null)

  const load = async () => {
    try { const r = await getTemplates(); setTemplates(r.data.data || []) }
    catch { /* ignore */ } finally { setLoading(false) }
  }
  useEffect(() => { load() }, [])

  const showToast = (m) => { setToast(m); setTimeout(() => setToast(''), 3000) }
  const set = (f) => (e) => setForm(p => ({ ...p, [f]: e.target.type === 'checkbox' ? e.target.checked : e.target.value }))

  const openAdd  = () => { setEditing(null); setForm(EMPTY); setError(''); setModal(true) }
  const openEdit = (t) => { setEditing(t); setForm({ senderName: t.senderName, senderPhone: t.senderPhone||'', messageContent: t.messageContent, messageType: t.messageType, makeDefault: t.default }); setError(''); setModal(true) }

  const handleSave = async (e) => {
    e.preventDefault(); setSaving(true); setError('')
    try {
      if (editing) { await updateTemplate(editing.id, form); showToast('Template updated') }
      else { await createTemplate(form); showToast('Template created') }
      setModal(false); load()
    } catch (err) { setError(err.response?.data?.message || 'Something went wrong') }
    finally { setSaving(false) }
  }

  const handleDelete = async (id) => {
    if (!confirm('Delete this template?')) return
    try { await deleteTemplate(id); showToast('Deleted'); load() } catch { showToast('Failed') }
  }

  const handleSetDefault = async (id) => {
    try { await setDefaultTemplate(id); showToast('Default updated'); load() } catch { showToast('Failed') }
  }

  const handleTrigger = async (templateId = null) => {
    try {
      const res = await triggerFakeMessage({ templateId })
      setNotification(res.data.data)
      setTimeout(() => setNotification(null), 8000)
    } catch (err) { showToast(err.response?.data?.message || 'No default template set') }
  }

  return (
      <div className="max-w-2xl mx-auto space-y-6 animate-slide-up">

        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-lg font-bold dark:text-white text-slate-900">Fake Message</h2>
            <p className="text-sm dark:text-slate-400 text-slate-500 mt-0.5">Display a fake emergency text notification</p>
          </div>
          <Button onClick={openAdd} size="sm"><Plus className="w-4 h-4" /> New Template</Button>
        </div>

        {toast && <Alert type="success" message={toast} />}

        {/* Quick trigger */}
        <div className="card p-5">
          <p className="text-sm font-medium dark:text-slate-300 text-slate-700 mb-3">Quick trigger</p>
          <Button onClick={() => handleTrigger(null)} size="lg" fullWidth variant="secondary">
            <MessageSquare className="w-4 h-4" /> Show fake message now
          </Button>
          <p className="text-xs dark:text-slate-500 text-slate-400 text-center mt-2">Uses your default template</p>
        </div>

        {/* Templates */}
        {loading ? (
            <div className="space-y-3">{[1,2].map(i => <div key={i} className="card p-4 animate-pulse h-20" />)}</div>
        ) : templates.length === 0 ? (
            <EmptyState icon="💬" title="No message templates yet"
                        desc="Create fake message templates to display as notifications."
                        action="Create first template" onAction={openAdd} />
        ) : (
            <div className="card divide-y dark:divide-surface-border divide-slate-100">
              {templates.map(t => (
                  <div key={t.id} className="flex items-start gap-3 p-4 group">
                    <div className="w-10 h-10 rounded-full bg-emerald-100 dark:bg-emerald-900/30 flex items-center justify-center flex-shrink-0 mt-0.5">
                      <MessageSquare className="w-4 h-4 text-emerald-600 dark:text-emerald-400" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 flex-wrap">
                        <p className="font-semibold dark:text-slate-200 text-slate-800 text-sm">{t.senderName}</p>
                        <Badge label={t.messageType} type={t.messageType?.toLowerCase()} />
                        {t.default && <Badge label="Default" type="default" />}
                      </div>
                      <p className="text-xs dark:text-slate-400 text-slate-500 mt-1 line-clamp-2">{t.messageContent}</p>
                    </div>
                    <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity flex-shrink-0">
                      <button onClick={() => handleTrigger(t.id)} title="Trigger message"
                              className="p-2 rounded-xl dark:hover:bg-surface-muted hover:bg-slate-100 text-emerald-500 transition-colors">
                        <Zap className="w-3.5 h-3.5" />
                      </button>
                      {!t.default && (
                          <button onClick={() => handleSetDefault(t.id)}
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
            <Modal title={editing ? 'Edit Template' : 'New Message Template'} onClose={() => setModal(false)}>
              {error && <Alert type="error" message={error} className="mb-4" />}
              <form onSubmit={handleSave} className="space-y-4">
                <Input label="Sender name" placeholder="Mom, Boss, Doctor…" value={form.senderName} onChange={set('senderName')} required />
                <Input label="Phone number (optional)" type="tel" value={form.senderPhone} onChange={set('senderPhone')} />
                <div className="flex flex-col gap-1.5">
                  <label className="text-sm font-medium dark:text-slate-300 text-slate-700">Message type</label>
                  <select value={form.messageType} onChange={set('messageType')}
                          className="input-base">
                    {TYPES.map(t => <option key={t} value={t}>{t}</option>)}
                  </select>
                </div>
                <div className="flex flex-col gap-1.5">
                  <label className="text-sm font-medium dark:text-slate-300 text-slate-700">Message content <span className="text-red-500">*</span></label>
                  <textarea value={form.messageContent} onChange={set('messageContent')} required rows={3}
                            placeholder="There's been an accident, come home now!"
                            className="input-base resize-none" />
                </div>
                <label className="flex items-center gap-3 cursor-pointer">
                  <input type="checkbox" checked={form.makeDefault} onChange={set('makeDefault')} className="w-4 h-4 rounded accent-brand-600" />
                  <span className="text-sm dark:text-slate-300 text-slate-700">Set as default template</span>
                </label>
                <div className="flex gap-3 pt-1">
                  <Button type="button" variant="secondary" fullWidth onClick={() => setModal(false)}>Cancel</Button>
                  <Button type="submit" fullWidth loading={saving}>{editing ? 'Save changes' : 'Create template'}</Button>
                </div>
              </form>
            </Modal>
        )}

        {/* Fake notification banner */}
        {notification && (
            <div className="fixed top-4 left-1/2 -translate-x-1/2 z-50 w-full max-w-sm animate-slide-up">
              <div className="mx-4 bg-slate-900 rounded-2xl shadow-2xl p-4 flex items-start gap-3 border border-slate-700">
                <div className="w-9 h-9 rounded-xl bg-green-500 flex items-center justify-center flex-shrink-0">
                  <MessageSquare className="w-4 h-4 text-white" />
                </div>
                <div className="flex-1 min-w-0">
                  <div className="flex items-center justify-between">
                    <p className="text-white font-semibold text-sm">{notification.senderName}</p>
                    <span className="text-slate-500 text-xs">now</span>
                  </div>
                  <p className="text-slate-300 text-sm mt-0.5 line-clamp-2">{notification.messageContent}</p>
                </div>
                <button onClick={() => setNotification(null)} className="text-slate-500 hover:text-slate-300 flex-shrink-0">
                  <X className="w-4 h-4" />
                </button>
              </div>
            </div>
        )}
      </div>
  )
}
