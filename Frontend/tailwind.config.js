/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{js,jsx}'],
  theme: {
    extend: {
      colors: {
        // Fresh teal — calm, trustworthy, modern. Replaces the old violet brand.
        brand: {
          50:  '#f0fdfa',
          100: '#ccfbf1',
          200: '#99f6e4',
          300: '#5eead4',
          400: '#2dd4bf',
          500: '#14b8a6',
          600: '#0d9488',
          700: '#0f766e',
          800: '#115e59',
          900: '#134e4a',
        },
        // Warm, airy neutral — replaces the cool blue-gray default slate
        // so every existing `slate-*` class automatically picks up the
        // softer, friendlier look without touching each page.
        slate: {
          50:  '#fafaf9',
          100: '#f5f5f4',
          200: '#e7e5e4',
          300: '#d6d3d1',
          400: '#a8a29e',
          500: '#78716c',
          600: '#57534e',
          700: '#44403c',
          800: '#292524',
          900: '#1c1917',
        },
        surface: {
          DEFAULT: '#0d1413',
          card:    '#131b1a',
          border:  '#20292a',
          muted:   '#263130',
        }
      },
      fontFamily: {
        sans: ['Plus Jakarta Sans', 'Inter', 'system-ui', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      },
      boxShadow: {
        soft:    '0 2px 10px -2px rgba(28, 25, 23, 0.06), 0 6px 20px -6px rgba(28, 25, 23, 0.07)',
        'soft-lg': '0 8px 30px -8px rgba(28, 25, 23, 0.10), 0 16px 40px -16px rgba(28, 25, 23, 0.10)',
        glow:    '0 10px 28px -8px rgba(13, 148, 136, 0.38)',
        'glow-sm': '0 6px 16px -4px rgba(13, 148, 136, 0.30)',
      },
      backgroundImage: {
        'airy-light': 'radial-gradient(circle at 0% 0%, rgba(204,251,241,0.55) 0%, rgba(250,250,249,0) 45%), radial-gradient(circle at 100% 0%, rgba(94,234,212,0.18) 0%, rgba(250,250,249,0) 40%)',
      },
      animation: {
        'ping-slow':   'ping 3s cubic-bezier(0,0,0.2,1) infinite',
        'ping-slower': 'ping 4s cubic-bezier(0,0,0.2,1) infinite',
        'fade-in':     'fadeIn 0.4s ease-out',
        'slide-up':    'slideUp 0.4s ease-out',
        'slide-in':    'slideIn 0.3s ease-out',
        'float':       'float 5s ease-in-out infinite',
        'pulse-slow':  'pulseSlow 1.8s ease-in-out infinite',
      },
      keyframes: {
        fadeIn:  { '0%': { opacity: '0' }, '100%': { opacity: '1' } },
        slideUp: { '0%': { opacity: '0', transform: 'translateY(16px)' }, '100%': { opacity: '1', transform: 'translateY(0)' } },
        slideIn: { '0%': { opacity: '0', transform: 'translateX(-12px)' }, '100%': { opacity: '1', transform: 'translateX(0)' } },
        float:   { '0%,100%': { transform: 'translateY(0)' }, '50%': { transform: 'translateY(-8px)' } },
        pulseSlow: { '0%,100%': { transform: 'scale(1)', opacity: '1' }, '50%': { transform: 'scale(1.06)', opacity: '0.85' } },
      },
    },
  },
  plugins: [],
}
