import { useLocation, useNavigate } from 'react-router-dom'
import type { AxiosError } from "axios"
import { useMutation } from "@tanstack/react-query"

import { api } from "@/services/api"
import { useAuthStore } from "@/store/useAuthStore"
import type { LoginRequest, LoginResponse } from "@/types/Auth"

interface LocationState {
  from?: {
    pathname: string;
  };
}

export const useLoginMutation = () => {
  const setAuth = useAuthStore((state) => state.setAuth);
  const navigate = useNavigate();
  const location = useLocation();

  return useMutation<LoginResponse, AxiosError<{ message: string }>, LoginRequest>({
    mutationFn: async (credentials: LoginRequest) => {
      const response = await api.post<LoginResponse>('/api/auth/login', credentials);
      
      return response.data;
    },
    onSuccess: (data) => {
      setAuth(data.accessToken, data.user);

      const state = location.state as LocationState;
      const from = state?.from?.pathname || '/';
      
      navigate(from, { replace: true }); 
    },
    onError: (error) => {
      console.error('Erro na autenticação:', error.response?.data?.message || error.message);
    }
  });
};
