import { useSearchParams } from 'react-router-dom'

export const useUrlFilters = () => {
  const [searchParams, setSearchParams] = useSearchParams()

  const changeFilters = (changes: Record<string, string>) => {
    const next = new URLSearchParams(searchParams)

    Object.entries(changes).forEach(([key, value]) => {
      if (value) {
        next.set(key, value)
      } else {
        next.delete(key)
      }
    })

    next.delete('page')
    setSearchParams(next)
  }

  return { searchParams, setSearchParams, changeFilters }
}
