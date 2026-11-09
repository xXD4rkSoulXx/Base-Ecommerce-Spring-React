const DEMO_SESSION_KEY = 'velociti-demo-session'

export const isDemoSession = () => sessionStorage.getItem(DEMO_SESSION_KEY) === 'true'

export const markDemoSession = () => sessionStorage.setItem(DEMO_SESSION_KEY, 'true')

export const unmarkDemoSession = () => sessionStorage.removeItem(DEMO_SESSION_KEY)
