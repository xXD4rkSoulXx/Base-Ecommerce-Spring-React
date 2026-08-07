import { useLocation, useNavigate } from 'react-router-dom'
import type { AxiosError } from 'axios'
import { useMutation } from '@tanstack/react-query'

import type { LoginRequest, LoginResponse } from '@/types/Auth'

import { authService } from '@/services/authService'
import { useAuthStore } from '@/store/useAuthStore'

interface LocationState {
  from?: {
    pathname: string;
  };
}

export const useLogin = () => {
  const setAuth = useAuthStore((state) => state.setAuth);
  const navigate = useNavigate();
  const location = useLocation();

  return useMutation<LoginResponse, AxiosError<{ message: string }>, LoginRequest>({
    mutationFn: authService.login,
    onSuccess: (data) => {
      const state = location.state as LocationState;
      const from = state?.from?.pathname || "/";

      setAuth(data.user, data.accessToken);
      
      navigate(from, { replace: true }); 
    },
    onError: (error) => {
      console.error("Authentication error: ", error.response?.data?.message || error.message);
    }
  });
};
