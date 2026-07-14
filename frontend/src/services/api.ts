import axios from "axios"

import { useAuthStore } from "@/store/useAuthStore"
import type { LoginResponse } from "@/types/Auth"

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json'
  }
});

api.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;

  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
}, (error) => Promise.reject(error));

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const response = await axios.post<LoginResponse>(
          `${import.meta.env.VITE_API_URL}/api/auth/refresh`,
          {},
          { withCredentials: true }
        );
        const { accessToken, user } = response.data;

        useAuthStore.getState().setAuth(accessToken, user);
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;

        return api(originalRequest);
      } catch (refreshError) {
        useAuthStore.getState().clearAuth();
        
        return Promise.reject(refreshError);
      }
    }
    return Promise.reject(error);
  }
);
