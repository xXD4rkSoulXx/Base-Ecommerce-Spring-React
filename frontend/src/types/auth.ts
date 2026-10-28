import type { UserResponse } from '@/types/user'

export interface RegisterRequest {
  email: string
  password: string
  firstName: string
  lastName: string
}

export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
}

export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  user: UserResponse
  accessToken: string
}
