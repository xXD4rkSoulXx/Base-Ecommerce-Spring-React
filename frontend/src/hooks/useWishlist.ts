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

  const { data: serverItems } = useQuery({
    queryKey: ['wishlist'],
    queryFn: wishlistService.getWishlist,
    enabled: isAuthenticated
  })

  const addMutation = useMutation({
    mutationFn: (productId: number) => wishlistService.addToWishlist(productId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['wishlist'] })
  })

  const removeMutation = useMutation({
    mutationFn: (productId: number) => wishlistService.removeFromWishlist(productId),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['wishlist'] })
  })

  const clearMutation = useMutation({
    mutationFn: wishlistService.clearWishlist,
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['wishlist'] })
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
    } else {
      guestClear()
    }
  }

  return {
    items,
    totalItems: items.length,
    isInWishlist,
    toggleWishlist,
    clearWishlist
  }
}
