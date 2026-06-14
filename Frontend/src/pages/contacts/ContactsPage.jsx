import { useState, useEffect } from 'react'
import { Plus, Pencil, Trash2, Phone, Mail } from 'lucide-react'
import { getContacts, addContact, updateContact, deleteContact } from '../../api/contacts.js'
import Button     from '../../components/ui/Button.jsx'
import Input      from '../../components/ui/Input.jsx'
import Alert      from '../../components/ui/Alert.jsx'
import Modal      from '../../components/ui/Modal.jsx'
import EmptyState from '../../components/ui/EmptyState.jsx'

const EMPTY = { name: '', phone: '', email: '', relationship: '' }

const avatarColor = (name) => {
  const colors = ['bg-brand-600','bg-emerald-500','bg-amber-500','bg-red-500','bg-pink-500','bg-cyan-500']
  return colors[(name?.charCodeAt(0) || 0) % colors.length]
}

export default function ContactsPage() {
  const [contacts, setContacts] = useState([])
  const [loading, setLoading]   = useState(true)
  const [modal, setModal]       = useState(false)
  const [editing, setEditing]   = useState(null)
  const [form, setForm]         = useState(EMPTY)
  const [saving, setSaving]     = useState(false)
  const [error, setError]       = useState('')
  const [toast, setToast]       = useState('')

  const load = async () => {
    try { const r = await getContacts(); setContacts(r.data.data || []) }
    catch { /* ignore */ } finally { setLoading(false) }
  }
  useEffect(() => { load() }, [])

  const showToast = (m) => { setToast(m); setTimeout(() => setToast(''), 3000) }
  const set = (f) => (e) => setForm(p => ({ ...p, [f]: e.target.value }))

  const openAdd  = () => { setEditing(null); setForm(EMPTY); setError(''); setModal(true) }
  const openEdit = (c) => {
    setEditing(c)
    setForm({ name: c.name, phone: c.phone, email: c.email || '', relationship: c.relationship || '' })
    setError('')
    setModal(true)
  }

  const handleSave = async (e) => {
    e.preventDefault(); setSaving(true); setError('')
    try {
      if (editing) { await updateContact(editing.id, form); showToast('Contact updated') }
      else { await addContact(form); showToast('Contact added') }
      setModal(false); load()
    } catch (err) { setError(err.response?.data?.message || 'Something went wrong') }
    finally { setSaving(false) }
  }

  const handleDelete = async (id) => {
    if (!confirm('Remove this contact?')) return
    try { await deleteContact(id); showToast('Contact removed'); load() }
    catch { showToast('Failed to delete contact') }
  }

  return (
      <div className="max-w-2xl mx-auto space-y-6 animate-slide-up">

        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-lg font-bold dark:text-white text-slate-900">Emergency Contacts</h2>
            <p className="text-sm dark:text-slate-400 text-slate-500 mt-0.5">
              These people receive real email & SMS when you trigger SOS
            </p>
          </div>
          <Button onClick={openAdd} size="sm"><Plus className="w-4 h-4" /> Add Contact</Button>
        </div>

        {/* Info banner */}
        <div className="dark:bg-brand-900/20 bg-brand-50 border dark:border-brand-800 border-brand-200 rounded-xl p-4">
          <p className="text-sm dark:text-brand-300 text-brand-700 font-medium">
            📧 Add your contact's email address to receive SOS alerts by email in real-time.
            Their phone number is used for SMS alerts (requires Twilio setup).
          </p>
        </div>

        {toast && <Alert type="success" message={toast} />}

        {loading ? (
            <div className="space-y-3">
              {[1,2,3].map(i => (
                  <div key={i} className="card p-4 animate-pulse flex items-center gap-4">
                    <div className="w-11 h-11 rounded-full bg-slate-200 dark:bg-surface-muted" />
                    <div className="flex-1 space-y-2">
                      <div className="h-3 bg-slate-200 dark:bg-surface-muted rounded w-32" />
                      <div className="h-3 bg-slate-200 dark:bg-surface-muted rounded w-48" />
                    </div>
                  </div>
              ))}
            </div>
        ) : contacts.length === 0 ? (
            <EmptyState icon="👥" title="No emergency contacts yet"
                        desc="Add your brother, parents, or friends. They'll be notified the moment you trigger SOS."
                        action="Add your first contact" onAction={openAdd} />
        ) : (
            <div className="card divide-y dark:divide-surface-border divide-slate-100">
              {contacts.map((c) => (
                  <div key={c.id} className="flex items-center gap-4 p-4 group">
                    <div className={`w-11 h-11 rounded-full flex items-center justify-center
                flex-shrink-0 text-white font-bold text-sm ${avatarColor(c.name)}`}>
                      {c.name?.charAt(0)?.toUpperCase()}
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2">
                        <p className="font-semibold dark:text-slate-200 text-slate-800 text-sm">{c.name}</p>
                        {c.relationship && (
                            <span className="text-xs dark:text-slate-500 text-slate-400">· {c.relationship}</span>
                        )}
                      </div>
                      <div className="flex flex-col gap-0.5 mt-0.5">
                  <span className="text-xs dark:text-slate-500 text-slate-400 flex items-center gap-1">
                    <Phone className="w-3 h-3" />{c.phone}
                    <span className="ml-1 text-xs px-1.5 py-0.5 rounded bg-amber-100 dark:bg-amber-900/30 text-amber-700 dark:text-amber-400">SMS</span>
                  </span>
                        {c.email ? (
                            <span className="text-xs dark:text-slate-500 text-slate-400 flex items-center gap-1">
                      <Mail className="w-3 h-3" />{c.email}
                              <span className="ml-1 text-xs px-1.5 py-0.5 rounded bg-emerald-100 dark:bg-emerald-900/30 text-emerald-700 dark:text-emerald-400">Email ✓</span>
                    </span>
                        ) : (
                            <span className="text-xs text-red-400 flex items-center gap-1">
                      <Mail className="w-3 h-3" /> No email — add one for email alerts
                    </span>
                        )}
                      </div>
                    </div>
                    <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                      <button onClick={() => openEdit(c)}
                              className="p-2 rounded-xl dark:hover:bg-surface-muted hover:bg-slate-100
                    dark:text-slate-400 text-slate-500 transition-colors">
                        <Pencil className="w-3.5 h-3.5" />
                      </button>
                      <button onClick={() => handleDelete(c.id)}
                              className="p-2 rounded-xl hover:bg-red-50 dark:hover:bg-red-900/20 text-red-500 transition-colors">
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  </div>
              ))}
            </div>
        )}

        {modal && (
            <Modal title={editing ? 'Edit Contact' : 'Add Emergency Contact'} onClose={() => setModal(false)}>
              {error && <Alert type="error" message={error} className="mb-4" />}
              <form onSubmit={handleSave} className="space-y-4">
                <Input label="Full name" placeholder="Brother, Mom, Best Friend…"
                       value={form.name} onChange={set('name')} required />
                <Input label="Phone number" type="tel" placeholder="9876543210"
                       value={form.phone} onChange={set('phone')} required
                       hint="Used for SMS alerts (requires Twilio in application.properties)" />
                <Input label="Email address" type="email" placeholder="brother@gmail.com"
                       value={form.email} onChange={set('email')}
                       hint="Required for email SOS alerts — strongly recommended" />
                <Input label="Relationship" placeholder="Brother, Mother, Friend…"
                       value={form.relationship} onChange={set('relationship')} />
                <div className="flex gap-3 pt-1">
                  <Button type="button" variant="secondary" fullWidth onClick={() => setModal(false)}>Cancel</Button>
                  <Button type="submit" fullWidth loading={saving}>
                    {editing ? 'Save changes' : 'Add contact'}
                  </Button>
                </div>
              </form>
            </Modal>
        )}
      </div>
  )
}
