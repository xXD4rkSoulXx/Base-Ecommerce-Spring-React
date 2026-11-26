import { useMutation, useQueryClient } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { useAuthStore } from '@/store/useAuthStore'
import { userService } from '@/services/user.service'
import type { ProfileResponse, UpdateProfileRequest } from '@/types/user'

export const useUpdateProfile = () => {
  const queryClient = useQueryClient()
  const setUser = useAuthStore((state) => state.setUser)

  return useMutation({
    mutationFn: (request: UpdateProfileRequest) => userService.updateProfile(request),
    onSuccess: (user) => {
      setUser(user)
      queryClient.setQueryData<ProfileResponse>(queryKeys.profile, (profile) =>
        profile ? { ...profile, user } : profile
      )
    }
  })
}
