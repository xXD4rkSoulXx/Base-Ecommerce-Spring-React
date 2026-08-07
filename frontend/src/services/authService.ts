import { api } from './api'

import type { LoginRequest, LoginResponse, RegisterRequest } from '@/types/Auth'

interface AuthService {
  register: (request: RegisterRequest) => Promise<void>;
  login: (request: LoginRequest) => Promise<LoginResponse>;
  logout: () => Promise<void>;
}

export const authService: AuthService = {
  register: async (request: RegisterRequest) => {
    await api.post<void>("/api/auth/register", request);
  },

  login: async (request: LoginRequest): Promise<LoginResponse> => {
    const response = await api.post<LoginResponse>("/api/auth/login", request);

    return response.data;
  },
  
  logout: async () => {
    await api.delete<void>("/api/auth/logout");
  }
};
