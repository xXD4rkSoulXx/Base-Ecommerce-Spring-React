import { useNavigate } from 'react-router-dom'
import { AxiosError } from 'axios';
import { useMutation } from '@tanstack/react-query'

import { useAuthStore } from '@/store/useAuthStore'

import { authService } from '@/services/authService'

export const useLogout = () => {
  const clearAuth = useAuthStore((state) => state.clearAuth);
  const navigate = useNavigate();

  return useMutation<void, AxiosError<{ message: string }>, void>({
    mutationFn: authService.logout,
    onSuccess: () => {
      clearAuth();
      navigate("/login", { replace: true });
    },
    onError: (error) => {
      console.error("Communication failure at session termination: ", error.response?.data?.message || error.message);
      clearAuth();
      navigate("/login", { replace: true });
    }
  });
};
