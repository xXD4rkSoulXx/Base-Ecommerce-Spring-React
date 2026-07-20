import { create } from 'zustand'

import type { UserResponse, UserRole } from '@/types/User'

interface AuthState {
  user: UserResponse | null;
  accessToken: string | null;
  isHydrated: boolean;
  setAuth: (user: UserResponse, token: string) => void;
  clearAuth: () => void;
  setHydrated: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  accessToken: null,
  isHydrated: false,

  setAuth: (user, token) => set({ user, accessToken: token }),

  clearAuth: () => set({ user: null, accessToken: null }),
  
  setHydrated: () => set({ isHydrated: true })
}));

export const hasAnyRole = (user: UserResponse | null, allowedRoles: UserRole[]): boolean => {
  if (!user) {
    return false;
  }

  return allowedRoles.includes(user.role);
};
