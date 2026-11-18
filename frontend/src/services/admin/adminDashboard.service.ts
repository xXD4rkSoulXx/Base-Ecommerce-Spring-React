import { api } from '@/lib/api'
import type { AdminDashboardEndpoints } from '@/types/admin/adminDashboard'

export const adminDashboardService = {
  get: async <E extends keyof AdminDashboardEndpoints>(endpoint: E, days?: number) => {
    const response = await api.get<AdminDashboardEndpoints[E]>(`/api/admin/dashboard/${endpoint}`, {
      params: { days }
    })

    return response.data
  }
}
