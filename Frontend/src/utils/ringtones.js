// Synthesized ringtones for the Fake Call feature.
//
// These are generated entirely with the Web Audio API (oscillators + gain
// envelopes) rather than bundled audio files, so there's nothing to host,
// nothing to license, and they work the instant the app loads.

let audioCtx = null
let activeNodes = []
let loopTimeoutId = null

function getCtx() {
    if (!audioCtx) {
        const Ctx = window.AudioContext || window.webkitAudioContext
        audioCtx = new Ctx()
    }
    return audioCtx
}

function tone(ctx, startTime, duration, freq, peakGain = 0.2, type = 'sine') {
    const osc  = ctx.createOscillator()
    const gain = ctx.createGain()
    osc.type = type
    osc.frequency.value = freq
    gain.gain.setValueAtTime(0, startTime)
    gain.gain.linearRampToValueAtTime(peakGain, startTime + 0.02)
    gain.gain.linearRampToValueAtTime(0, startTime + duration)
    osc.connect(gain)
    gain.connect(ctx.destination)
    osc.start(startTime)
    osc.stop(startTime + duration + 0.03)
    activeNodes.push(osc, gain)
}

// Each pattern schedules one "cycle" of the ring starting at time `t`,
// and returns the cycle length in seconds so the loop knows when to
// schedule the next repetition.
const patterns = {
    // Classic dual-tone phone bell — "ring… ring…"
    classic: (ctx, t) => {
        tone(ctx, t,       0.4, 950,  0.18)
        tone(ctx, t,       0.4, 1400, 0.10)
        tone(ctx, t + 0.5, 0.4, 950,  0.18)
        tone(ctx, t + 0.5, 0.4, 1400, 0.10)
        return 2.4
    },
    // Soft pulsing triplet, like a modern smartphone ringtone
    pulse: (ctx, t) => {
        for (let i = 0; i < 3; i++) tone(ctx, t + i * 0.35, 0.25, 600, 0.16)
        return 2.0
    },
    // Gentle ascending three-note chime
    chime: (ctx, t) => {
        tone(ctx, t,        0.30, 523.25, 0.16, 'triangle') // C5
        tone(ctx, t + 0.28, 0.30, 659.25, 0.16, 'triangle') // E5
        tone(ctx, t + 0.56, 0.45, 783.99, 0.16, 'triangle') // G5
        return 2.2
    },
    // Quick modern triple-beep
    digital: (ctx, t) => {
        for (let i = 0; i < 3; i++) tone(ctx, t + i * 0.15, 0.10, 1100, 0.15, 'square')
        return 1.4
    },
}

export const RINGTONES = [
    { id: 'classic', label: 'Classic Ring' },
    { id: 'pulse',   label: 'Pulse' },
    { id: 'chime',   label: 'Chime' },
    { id: 'digital', label: 'Digital Beep' },
]

/** Call this synchronously inside a click handler to satisfy browser autoplay
 *  policies — it creates/resumes the AudioContext while still inside the
 *  user-gesture call stack, before any async work happens. */
export function unlockAudio() {
    const ctx = getCtx()
    if (ctx.state === 'suspended') ctx.resume()
}

/** Starts looping the given ringtone until stopRingtone() is called. */
export function playRingtone(id = 'classic') {
    stopRingtone()
    const ctx = getCtx()
    if (ctx.state === 'suspended') ctx.resume()
    const pattern = patterns[id] || patterns.classic

    const scheduleNext = () => {
        const cycleLength = pattern(ctx, ctx.currentTime + 0.05)
        loopTimeoutId = setTimeout(scheduleNext, cycleLength * 1000)
    }
    scheduleNext()
}

/** Stops any currently looping or playing ringtone. */
export function stopRingtone() {
    if (loopTimeoutId) { clearTimeout(loopTimeoutId); loopTimeoutId = null }
    activeNodes.forEach((node) => {
        try { node.stop && node.stop() } catch { /* already stopped */ }
        try { node.disconnect() } catch { /* already disconnected */ }
    })
    activeNodes = []
}

/** Plays a single cycle of a ringtone — used for the "preview" button. */
export function previewRingtone(id = 'classic') {
    stopRingtone()
    const ctx = getCtx()
    if (ctx.state === 'suspended') ctx.resume()
    const pattern = patterns[id] || patterns.classic
    pattern(ctx, ctx.currentTime + 0.05)
}
