import axios from 'axios'
import { useAuthStore } from '@/store/useAuthStore'
import type { LoginResponse } from '@/types/auth'

let inFlightRefresh: Promise<LoginResponse> | null = null

export const refreshAuth = (): Promise<LoginResponse> => {
  if (!inFlightRefresh) {
    inFlightRefresh = axios
      .post<LoginResponse>(
        `${import.meta.env.VITE_API_URL}/api/auth/refresh`,
        {},
        {
          headers: {
            'Content-Type': 'application/json',
            'X-Requested-With': 'XMLHttpRequest'
          },
          withCredentials: true
        }
      )
      .then((response) => {
        useAuthStore.getState().setAuth(response.data.user, response.data.accessToken)

        return response.data
      })
      .catch((error) => {
        useAuthStore.getState().clearAuth()

        throw error
      })
      .finally(() => {
        inFlightRefresh = null
      })
  }

  return inFlightRefresh
}
