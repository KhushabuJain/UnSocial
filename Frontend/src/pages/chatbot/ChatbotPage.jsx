import { useState, useEffect, useRef } from 'react'
import { Bot, Send, Trash2, BookOpen, ShieldAlert, Sparkles } from 'lucide-react'
import { askChatbot, getChatHistory, clearChatHistory } from '../../api/chatbot.js'
import Button from '../../components/ui/Button.jsx'

const STARTER_PROMPTS = [
  'How do I set up Live Tracking?',
  "What's the emergency number in India?",
  'How does the Safety Timer work?',
  "What should I do if I'm being followed?",
]

const fmtTime = (d) => new Date(d).toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' })

// "safety-general-tips.md" -> "Safety General Tips"
const prettySource = (filename) =>
  filename.replace(/\.md$/, '').split('-').map(w => w.charAt(0).toUpperCase() + w.slice(1)).join(' ')

export default function ChatbotPage() {
  const [messages, setMessages]   = useState([])
  const [loading, setLoading]     = useState(true)
  const [sending, setSending]     = useState(false)
  const [input, setInput]         = useState('')
  const [error, setError]         = useState('')
  const scrollRef  = useRef(null)
  const textareaRef = useRef(null)

  const load = async () => {
    try {
      const r = await getChatHistory()
      setMessages((r.data.data || []).map(m => ({
        role: m.role.toLowerCase(), content: m.content, createdAt: m.createdAt,
      })))
    } catch { /* ignore — start with empty history */ }
    finally { setLoading(false) }
  }
  useEffect(() => { load() }, [])

  useEffect(() => {
    scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: 'smooth' })
  }, [messages, sending])

  const autoGrow = (el) => {
    el.style.height = 'auto'
    el.style.height = Math.min(el.scrollHeight, 140) + 'px'
  }

  const send = async (text) => {
    const message = (text ?? input).trim()
    if (!message || sending) return
    setError('')
    setInput('')
    if (textareaRef.current) textareaRef.current.style.height = 'auto'
    setMessages(prev => [...prev, { role: 'user', content: message, createdAt: new Date().toISOString() }])
    setSending(true)
    try {
      const r = await askChatbot(message)
      const { reply, sources, createdAt } = r.data.data
      setMessages(prev => [...prev, { role: 'assistant', content: reply, sources, createdAt }])
    } catch (err) {
      const msg = err.response?.data?.message
        || "Couldn't reach the AI Safety Assistant. Make sure Ollama is running locally."
      setError(msg)
      setMessages(prev => [...prev, { role: 'error', content: msg, createdAt: new Date().toISOString() }])
    } finally {
      setSending(false)
    }
  }

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      send()
    }
  }

  const handleClear = async () => {
    if (!confirm('Clear your entire chat history? This cannot be undone.')) return
    try { await clearChatHistory(); setMessages([]) }
    catch { setError('Failed to clear history') }
  }

  return (
    <div className="max-w-2xl mx-auto h-full flex flex-col animate-slide-up">

      {/* Header */}
      <div className="flex items-center justify-between mb-4 flex-shrink-0">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-2xl bg-brand-600 shadow-glow-sm flex items-center justify-center flex-shrink-0">
            <Bot className="w-5 h-5 text-white" />
          </div>
          <div>
            <h2 className="text-lg font-bold dark:text-white text-slate-900">AI Safety Assistant</h2>
            <p className="text-xs dark:text-slate-400 text-slate-500">
              Safety guidance & app help · runs locally, grounded in UnSocial's knowledge base
            </p>
          </div>
        </div>
        {messages.length > 0 && (
          <button onClick={handleClear} title="Clear chat history"
            className="p-2 rounded-xl dark:hover:bg-surface-muted hover:bg-slate-100 dark:text-slate-400 text-slate-500 transition-colors flex-shrink-0">
            <Trash2 className="w-4 h-4" />
          </button>
        )}
      </div>

      {/* Messages */}
      <div ref={scrollRef} className="card flex-1 min-h-0 overflow-y-auto p-4 sm:p-5 space-y-4">
        {loading ? (
          <div className="space-y-3 animate-pulse">
            {[1, 2].map(i => <div key={i} className="h-12 rounded-2xl bg-slate-100 dark:bg-surface-muted w-2/3" />)}
          </div>
        ) : messages.length === 0 ? (
          <div className="h-full flex flex-col items-center justify-center text-center px-4 py-8">
            <div className="w-14 h-14 rounded-2xl bg-brand-100 dark:bg-brand-900/30 flex items-center justify-center mb-4">
              <Sparkles className="w-6 h-6 text-brand-600 dark:text-brand-400" />
            </div>
            <p className="font-semibold dark:text-slate-200 text-slate-800 mb-1">
              Ask me anything about safety or UnSocial
            </p>
            <p className="text-sm dark:text-slate-500 text-slate-400 mb-6 max-w-sm">
              I can help with personal safety guidance or explain how any UnSocial feature works.
            </p>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2 w-full max-w-md">
              {STARTER_PROMPTS.map(p => (
                <button key={p} onClick={() => send(p)}
                  className="text-left text-xs px-3 py-2.5 rounded-xl border dark:border-surface-border border-slate-200
                    dark:hover:bg-surface-muted hover:bg-slate-50 dark:text-slate-300 text-slate-600 transition-colors">
                  {p}
                </button>
              ))}
            </div>
          </div>
        ) : (
          messages.map((m, i) => <MessageBubble key={i} message={m} />)
        )}

        {sending && (
          <div className="flex items-center gap-2 dark:text-slate-500 text-slate-400">
            <div className="w-8 h-8 rounded-full bg-brand-100 dark:bg-brand-900/30 flex items-center justify-center flex-shrink-0">
              <Bot className="w-4 h-4 text-brand-600 dark:text-brand-400" />
            </div>
            <div className="flex gap-1 px-4 py-3 rounded-2xl rounded-bl-sm dark:bg-surface-muted bg-slate-100">
              {[0, 1, 2].map(i => (
                <span key={i} className="w-1.5 h-1.5 rounded-full bg-current animate-bounce"
                  style={{ animationDelay: `${i * 0.15}s` }} />
              ))}
            </div>
          </div>
        )}
      </div>

      {/* Input */}
      <div className="flex-shrink-0 mt-3">
        <div className="card flex items-end gap-2 p-2">
          <textarea
            ref={textareaRef}
            value={input}
            onChange={(e) => { setInput(e.target.value); autoGrow(e.target) }}
            onKeyDown={handleKeyDown}
            placeholder="Ask about safety or how to use UnSocial…"
            rows={1}
            className="flex-1 resize-none bg-transparent outline-none px-3 py-2.5 text-sm
              dark:text-slate-100 text-slate-900 placeholder:dark:text-slate-500 placeholder:text-slate-400"
          />
          <Button onClick={() => send()} disabled={!input.trim() || sending} size="md" className="!rounded-xl flex-shrink-0">
            <Send className="w-4 h-4" />
          </Button>
        </div>
        <p className="text-[11px] dark:text-slate-600 text-slate-400 mt-2 text-center">
          Not a substitute for emergency services — in immediate danger, call 112 or use SOS Alert.
        </p>
      </div>
    </div>
  )
}

function MessageBubble({ message }) {
  const { role, content, sources, createdAt } = message

  if (role === 'error') {
    return (
      <div className="flex items-start gap-2.5">
        <div className="w-8 h-8 rounded-full bg-red-100 dark:bg-red-900/30 flex items-center justify-center flex-shrink-0">
          <ShieldAlert className="w-4 h-4 text-red-500" />
        </div>
        <div className="max-w-[80%] px-4 py-2.5 rounded-2xl rounded-bl-sm bg-red-50 dark:bg-red-900/20
          border border-red-200 dark:border-red-800 text-red-700 dark:text-red-400 text-sm">
          {content}
        </div>
      </div>
    )
  }

  const isUser = role === 'user'
  return (
    <div className={`flex items-start gap-2.5 ${isUser ? 'flex-row-reverse' : ''}`}>
      <div className={`w-8 h-8 rounded-full flex items-center justify-center flex-shrink-0
        ${isUser ? 'bg-brand-600' : 'bg-brand-100 dark:bg-brand-900/30'}`}>
        {isUser
          ? <span className="text-white text-xs font-bold">You</span>
          : <Bot className="w-4 h-4 text-brand-600 dark:text-brand-400" />}
      </div>
      <div className={`max-w-[80%] flex flex-col ${isUser ? 'items-end' : 'items-start'}`}>
        <div className={`px-4 py-2.5 text-sm whitespace-pre-wrap leading-relaxed
          ${isUser
            ? 'bg-brand-600 text-white rounded-2xl rounded-br-sm'
            : 'dark:bg-surface-muted bg-slate-100 dark:text-slate-200 text-slate-700 rounded-2xl rounded-bl-sm'}`}>
          {content}
        </div>
        {!isUser && sources?.length > 0 && (
          <div className="flex items-center gap-1.5 flex-wrap mt-1.5 px-1">
            <BookOpen className="w-3 h-3 dark:text-slate-600 text-slate-400" />
            {sources.map(s => (
              <span key={s} className="text-[10px] px-1.5 py-0.5 rounded dark:bg-surface-muted bg-slate-100 dark:text-slate-500 text-slate-400">
                {prettySource(s)}
              </span>
            ))}
          </div>
        )}
        {createdAt && (
          <span className="text-[10px] dark:text-slate-600 text-slate-400 mt-1 px-1">{fmtTime(createdAt)}</span>
        )}
      </div>
    </div>
  )
}
