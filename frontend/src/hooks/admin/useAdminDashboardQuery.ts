import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { adminDashboardService } from '@/services/admin/adminDashboard.service'
import type { AdminDashboardEndpoints } from '@/types/admin/adminDashboard'

export const useAdminDashboardQuery = <E extends keyof AdminDashboardEndpoints>(
  endpoint: E,
  enabled: boolean,
  days?: number
) =>
  useQuery({
    queryKey: queryKeys.admin.dashboard.query(endpoint, days),
    queryFn: () => adminDashboardService.get(endpoint, days),
    enabled,
    placeholderData: (previousData) => previousData
  })
