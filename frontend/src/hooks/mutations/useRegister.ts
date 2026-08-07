import { useNavigate } from 'react-router-dom'
import type { AxiosError } from 'axios'
import { useMutation } from '@tanstack/react-query'

import type { RegisterRequest } from '@/types/Auth'

import { authService } from '@/services/authService'

export const useRegister = () => {
  const navigate = useNavigate();

  return useMutation<void, AxiosError<{ message: string }>, RegisterRequest>({
    mutationFn: authService.register,
    onSuccess: () => { navigate("/login"); },
    onError: (error) => {
      console.error("Registration error: ", error.response?.data?.message || error.message);
    }
  });
};
