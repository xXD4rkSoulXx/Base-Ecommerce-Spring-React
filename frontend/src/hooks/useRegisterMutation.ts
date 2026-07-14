import { useNavigate } from "react-router-dom"
import type { AxiosError } from "axios"
import { useMutation } from "@tanstack/react-query"

import { api } from "@/services/api"
import type { RegisterRequest } from "@/types/Auth"

export const useRegisterMutation = () => {
  const navigate = useNavigate();

  return useMutation<void, AxiosError<{ message: string }>, RegisterRequest>({
    mutationFn: async (userData: RegisterRequest) => {
      await api.post<void>('/api/auth/register', userData);
    },
    onSuccess: () => {
      console.log('Utilizador registado com sucesso!');

      navigate('/login');
    },
    onError: (error) => {
      console.error('Erro no registo:', error.response?.data?.message || error.message);
    }
  });
};

