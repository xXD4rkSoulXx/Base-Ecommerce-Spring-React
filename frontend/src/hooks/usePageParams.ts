import { useSearchParams } from 'react-router-dom'
import { LIST_PAGE_SIZE } from '@/constants'

export const usePageParams = (defaultSize = LIST_PAGE_SIZE) => {
  const [searchParams] = useSearchParams()
  const page = Math.max(Number(searchParams.get('page')) || 1, 1)
  const size = Number(searchParams.get('size')) || defaultSize

  return { page, size }
}
