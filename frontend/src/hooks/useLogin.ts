import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useLocation, useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { queryKeys } from '@/queryKeys'
import { useAuthStore } from '@/store/useAuthStore'
import { useWishlistStore } from '@/store/useWishlistStore'
import { useCartStore } from '@/store/useCartStore'
import { authService } from '@/services/auth.service'
import { wishlistService } from '@/services/wishlist.service'
import { cartService } from '@/services/cart.service'
import type { LoginRequest } from '@/types/auth'

interface LocationState {
  from?: {
    pathname: string
  }
}

export const useLogin = () => {
  const setAuth = useAuthStore((state) => state.setAuth)
  const navigate = useNavigate()
  const location = useLocation()
  const queryClient = useQueryClient()

  return useMutation({
    mutationFn: (request: LoginRequest) => authService.login(request),
    onSuccess: async (data) => {
      const state = location.state as LocationState
      const from = state?.from?.pathname || '/'

      setAuth(data.user, data.accessToken)

      const guestWishlistItems = useWishlistStore.getState().items

      if (guestWishlistItems.length > 0) {
        const productIds = guestWishlistItems.map((item) => item.id)

        try {
          await wishlistService.mergeWishlist(productIds)
          useWishlistStore.getState().clearWishlist()
        } catch {
          toast.error('Could not sync your wishlist. It will be retried on your next login.')
        }
      }

      const guestCartItems = useCartStore.getState().items

      if (guestCartItems.length > 0) {
        const items = guestCartItems.map((item) => ({
          productSkuId: item.productSkuId,
          quantity: item.quantity
        }))

        try {
          await cartService.mergeCart({ items })
          useCartStore.getState().clearCart()
        } catch {
          toast.error('Could not sync your cart. It will be retried on your next login.')
        }
      }

      queryClient.invalidateQueries({ queryKey: queryKeys.wishlist })
      queryClient.invalidateQueries({ queryKey: queryKeys.cart })
      navigate(from, { replace: true })
    }
  })
}
