import { api } from '@/lib/api'
import type { RegisterRequest, LoginRequest, LoginResponse, ChangePasswordRequest } from '@/types/auth'

export const authService = {
  register: async (request: RegisterRequest) => {
    await api.post<void>('/api/auth/register', request)
  },

  login: async (request: LoginRequest): Promise<LoginResponse> => {
    const response = await api.post<LoginResponse>('/api/auth/login', request)

    return response.data
  },

  changePassword: async (request: ChangePasswordRequest) => {
    await api.put<void>('/api/auth/password', request)
  },
  
  logout: async () => {
    await api.delete<void>('/api/auth/logout')
  },

  logoutAll: async () => {
    await api.delete<void>('/api/auth/logout-all')
  }
}
