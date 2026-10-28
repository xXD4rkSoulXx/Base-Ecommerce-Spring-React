import { api } from '@/lib/api'
import type { ProfileResponse, UpdateProfileRequest, UserResponse } from '@/types/user'

export const userService = {
  getProfile: async (): Promise<ProfileResponse> => {
    const response = await api.get<ProfileResponse>('/api/users/me')

    return response.data
  },

  updateProfile: async (request: UpdateProfileRequest): Promise<UserResponse> => {
    const response = await api.patch<UserResponse>('/api/users/me', request)

    return response.data
  }
}
