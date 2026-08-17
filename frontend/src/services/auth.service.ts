import { api } from '@/lib/api'
import type { RegisterRequest, LoginRequest, LoginResponse } from '@/types/auth'

export const authService = {
  register: async (request: RegisterRequest) => {
    await api.post<void>('/api/auth/register', request)
  },

  login: async (request: LoginRequest): Promise<LoginResponse> => {
    const response = await api.post<LoginResponse>('/api/auth/login', request)

    return response.data
  },
  
  logout: async () => {
    await api.delete<void>('/api/auth/logout')
  }
}
