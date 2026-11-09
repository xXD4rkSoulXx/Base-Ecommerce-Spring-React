import { useSearchParams } from 'react-router-dom'

export const useAdminDashboardDays = () => {
  const [searchParams] = useSearchParams()
  const days = Number(searchParams.get('days'))

  return [7, 30, 90].includes(days) ? days : 30
}
