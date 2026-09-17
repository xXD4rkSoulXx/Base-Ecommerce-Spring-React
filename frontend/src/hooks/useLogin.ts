import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useLocation, useNavigate } from 'react-router-dom'
import { authService } from '@/services/auth.service'
import { wishlistService } from '@/services/wishlist.service'
import { useAuthStore } from '@/store/useAuthStore'
import { useWishlistStore } from '@/store/useWishlistStore'
import { useCartStore } from '@/store/useCartStore'
import { cartService } from '@/services/cart.service'

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
    mutationFn: authService.login,
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
          void 0
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
          void 0
        }
      }

      queryClient.invalidateQueries({ queryKey: ['wishlist'] })
      queryClient.invalidateQueries({ queryKey: ['cart'] })
      navigate(from, { replace: true })
    }
  })
}
