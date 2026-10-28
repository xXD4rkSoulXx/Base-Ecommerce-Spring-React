import { useMutation, useQueryClient } from '@tanstack/react-query'
import { userService } from '@/services/user.service'
import { useAuthStore } from '@/store/useAuthStore'
import type { ProfileResponse } from '@/types/user'

export const useUpdateProfile = () => {
  const queryClient = useQueryClient()
  const setUser = useAuthStore((state) => state.setUser)

  return useMutation({
    mutationFn: userService.updateProfile,
    onSuccess: (user) => {
      setUser(user)
      queryClient.setQueryData<ProfileResponse>(['profile'], (profile) =>
        profile ? { ...profile, user } : profile
      )
    }
  })
}
