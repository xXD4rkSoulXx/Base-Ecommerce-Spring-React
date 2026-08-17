import axios, { type InternalAxiosRequestConfig, AxiosError  } from 'axios'
import { useAuthStore } from '@/store/useAuthStore'
import type { LoginResponse } from '@/types/auth'

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN'
})

api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = useAuthStore.getState().accessToken
    const csrfToken = document.cookie
      .split('; ')
      .find(row => row.startsWith('XSRF-TOKEN='))
      ?.split('=')[1]
  
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`
  }

  if (csrfToken && config.headers) {
    config.headers['X-XSRF-TOKEN'] = csrfToken
  }

  return config
}, (error: AxiosError) => Promise.reject(error))

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      try {
        const response = await axios.post<LoginResponse>(
          `${import.meta.env.VITE_API_URL}/api/auth/refresh`,
          {},
          {
            headers: { 'Content-Type': 'application/json' },
            withCredentials: true
          }
        )
        const { user, accessToken } = response.data

        useAuthStore.getState().setAuth(user, accessToken)

        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${accessToken}`
        }

        return api(originalRequest)
      } catch (refreshError) {
        useAuthStore.getState().clearAuth()
        
        return Promise.reject(refreshError)
      }
    }

    return Promise.reject(error)
  }
)
