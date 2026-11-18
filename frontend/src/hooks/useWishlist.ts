import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useAuthStore } from '@/store/useAuthStore'
import { useWishlistStore } from '@/store/useWishlistStore'
import { wishlistService } from '@/services/wishlist.service'
import type { ProductResponse } from '@/types/product'

export const useWishlist = () => {
  const isAuthenticated = useAuthStore((state) => !!state.user)
  const queryClient = useQueryClient()

  const guestItems = useWishlistStore((state) => state.items)
  const guestAdd = useWishlistStore((state) => state.addToWishlist)
  const guestRemove = useWishlistStore((state) => state.removeFromWishlist)
  const guestClear = useWishlistStore((state) => state.clearWishlist)

  const {
    data: serverItems,
    isLoading,
    isError
  } = useQuery({
    queryKey: ['wishlist'],
    queryFn: () => wishlistService.getWishlist(),
    enabled: isAuthenticated
  })

  const invalidateWishlist = () => queryClient.invalidateQueries({ queryKey: ['wishlist'] })

  const addMutation = useMutation({
    mutationFn: (productId: number) => wishlistService.addToWishlist(productId),
    onSuccess: invalidateWishlist
  })

  const removeMutation = useMutation({
    mutationFn: (productId: number) => wishlistService.removeFromWishlist(productId),
    onSuccess: invalidateWishlist
  })

  const clearMutation = useMutation({
    mutationFn: () => wishlistService.clearWishlist(),
    onSuccess: invalidateWishlist
  })

  const items = isAuthenticated ? (serverItems ?? []) : guestItems

  const isInWishlist = (productId: number) => items.some((item) => item.id === productId)

  const toggleWishlist = (product: ProductResponse) => {
    const alreadyIn = isInWishlist(product.id)

    if (isAuthenticated) {
      if (alreadyIn) {
        removeMutation.mutate(product.id)
      } else {
        addMutation.mutate(product.id)
      }

      return
    }

    if (alreadyIn) {
      guestRemove(product.id)
    } else {
      guestAdd(product)
    }
  }

  const clearWishlist = () => {
    if (isAuthenticated) {
      clearMutation.mutate()

      return
    }

    guestClear()
  }

  return {
    items,
    totalItems: items.length,
    isLoading,
    isError,
    isInWishlist,
    toggleWishlist,
    clearWishlist
  }
}
