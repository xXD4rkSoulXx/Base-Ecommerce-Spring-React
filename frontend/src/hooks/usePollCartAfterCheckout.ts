import { useEffect } from 'react'
import { useQueryClient } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'

const POLL_INTERVAL_MS = 2000
const MAX_POLLS = 5

export const usePollCartAfterCheckout = () => {
  const queryClient = useQueryClient()

  useEffect(() => {
    let polls = 0
    const refreshCart = () => queryClient.invalidateQueries({ queryKey: queryKeys.cart })

    refreshCart()

    const interval = setInterval(() => {
      polls += 1
      refreshCart()

      if (polls === MAX_POLLS) {
        clearInterval(interval)
      }
    }, POLL_INTERVAL_MS)

    return () => clearInterval(interval)
  }, [queryClient])
}
