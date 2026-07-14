import { useNavigate } from 'react-router-dom'
import { AxiosError } from 'axios';
import { useMutation } from '@tanstack/react-query'

import { useAuthStore } from '@/store/useAuthStore'
import { api } from '@/services/api'

export const useLogoutMutation = () => {
  const clearAuth = useAuthStore((state) => state.clearAuth);
  const navigate = useNavigate();

  return useMutation<void, AxiosError<{ message: string }>, void>({
    mutationFn: async () => {
      await api.post<void>('/api/auth/logout');
    },
    onSuccess: () => {
      clearAuth();
      
      navigate('/login', { replace: true });
    },
    onError: (error) => {
      console.error('Falha de comunicação no encerramento da sessão:', error.response?.data?.message || error.message);
      
      clearAuth();
      navigate('/login', { replace: true });
    }
  });
};
