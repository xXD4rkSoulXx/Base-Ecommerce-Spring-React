import { create } from 'zustand'
import { clearSessionHint, markSessionHint } from '@/lib/sessionHint'
import type { UserResponse } from '@/types/user'

interface AuthState {
  user: UserResponse | null
  accessToken: string | null
  isHydrated: boolean
  setAuth: (user: UserResponse, token: string) => void
  setUser: (user: UserResponse) => void
  clearAuth: () => void
  setHydrated: () => void
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  accessToken: null,
  isHydrated: false,

  setAuth: (user, token) => {
    markSessionHint()
    set({ user, accessToken: token })
  },

  setUser: (user) => set({ user }),

  clearAuth: () => {
    clearSessionHint()
    set({ user: null, accessToken: null })
  },

  setHydrated: () => set({ isHydrated: true })
}))
