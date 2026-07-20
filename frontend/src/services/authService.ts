import { api } from './api'

import type { LoginRequest, LoginResponse, RegisterRequest } from '@/types/Auth'

interface AuthService {
  register: (userData: RegisterRequest) => Promise<void>;
  login: (credentials: LoginRequest) => Promise<LoginResponse>;
  logout: () => Promise<void>;
}

export const authService: AuthService = {
  register: async (userData: RegisterRequest) => {
    await api.post<void>("/api/auth/register", userData);
  },

  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response = await api.post<LoginResponse>("/api/auth/login", credentials);

    return response.data;
  },
  
  logout: async () => {
    await api.delete<void>("/api/auth/logout");
  }
};
