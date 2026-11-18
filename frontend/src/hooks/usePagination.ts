import { useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import { usePageParams } from '@/hooks/usePageParams'

export const usePagination = (totalPages: number) => {
  const [searchParams, setSearchParams] = useSearchParams()
  const { page: currentPage } = usePageParams()

  const goToPage = (page: number, replace: boolean) => {
    const next = new URLSearchParams(searchParams)

    next.set('page', page.toString())
    setSearchParams(next, { replace })
  }

  useEffect(() => {
    if (totalPages > 0 && currentPage > totalPages) {
      const next = new URLSearchParams(searchParams)

      next.set('page', totalPages.toString())
      setSearchParams(next, { replace: true })
    }
  }, [currentPage, totalPages, searchParams, setSearchParams])

  const changePage = (page: number) => {
    if (page >= 1 && page <= totalPages) {
      goToPage(page, false)
    }
  }

  return { currentPage, changePage }
}
