const SESSION_HINT_KEY = 'velociti-session-hint'

export const hasSessionHint = () => localStorage.getItem(SESSION_HINT_KEY) === 'true'

export const markSessionHint = () => localStorage.setItem(SESSION_HINT_KEY, 'true')

export const clearSessionHint = () => localStorage.removeItem(SESSION_HINT_KEY)
