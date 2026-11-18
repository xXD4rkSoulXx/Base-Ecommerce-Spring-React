import { useSearchParams } from 'react-router-dom'
import { DASHBOARD_PERIODS, DEFAULT_DASHBOARD_PERIOD } from '@/constants'

export const useAdminDashboardDays = () => {
  const [searchParams] = useSearchParams()
  const days = Number(searchParams.get('days'))

  return DASHBOARD_PERIODS.includes(days) ? days : DEFAULT_DASHBOARD_PERIOD
}
