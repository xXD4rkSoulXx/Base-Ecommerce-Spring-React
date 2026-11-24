import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { userService } from '@/services/user.service'

export const useProfile = () =>
  useQuery({
    queryKey: queryKeys.profile,
    queryFn: () => userService.getProfile()
  })
