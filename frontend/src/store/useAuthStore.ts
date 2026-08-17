import { create } from 'zustand'
import type { UserResponse } from '@/types/user'

interface AuthState {
  user: UserResponse | null
  accessToken: string | null
  isHydrated: boolean
  setAuth: (user: UserResponse, token: string) => void
  clearAuth: () => void
  setHydrated: () => void
  hasAnyRole: (allowedRoles: string[]) => boolean
}

export const useAuthStore = create<AuthState>((set, get) => ({
  user: null,
  accessToken: null,
  isHydrated: false,

  setAuth: (user, token) => set({ user, accessToken: token }),

  clearAuth: () => set({ user: null, accessToken: null }),
  
  setHydrated: () => set({ isHydrated: true }),

  hasAnyRole: (allowedRoles) => {
    const user = get().user

    if (!user) {
      return false
    }

    return allowedRoles.includes(user.role)
  }
}))
