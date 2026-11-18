import { useEffect, useState, type SubmitEvent } from 'react'
import { useDebounce } from '@/hooks/useDebounce'
import { useUrlFilters } from '@/hooks/useUrlFilters'

export const useSearchFilter = () => {
  const { searchParams, changeFilters } = useUrlFilters()
  const q = searchParams.get('q') || ''
  const [inputValue, setInputValue] = useState(q)
  const debouncedQuery = useDebounce(inputValue.trim(), 400)

  useEffect(() => {
    if (debouncedQuery !== q) {
      changeFilters({ q: debouncedQuery })
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [debouncedQuery])

  const submitSearch = (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault()
    changeFilters({ q: inputValue.trim() })
  }

  return { inputValue, setInputValue, submitSearch }
}
