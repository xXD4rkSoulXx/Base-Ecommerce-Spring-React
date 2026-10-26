import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useAuthStore } from '@/store/useAuthStore'
import { useCartStore, type GuestCartItem } from '@/store/useCartStore'
import { cartService } from '@/services/cart.service'

type Priced = {
  price: number
  discountPercentage: number
}

const getUnitPrice = (item: Priced) => {
  const cents = Math.round(item.price * 100)

  return Math.round((cents * (100 - item.discountPercentage)) / 100) / 100
}

export const useCart = () => {
  const isAuthenticated = useAuthStore((state) => !!state.user)
  const queryClient = useQueryClient()

  const guestItems = useCartStore((state) => state.items)
  const guestAdd = useCartStore((state) => state.addToCart)
  const guestUpdateQuantity = useCartStore((state) => state.updateQuantity)
  const guestRemove = useCartStore((state) => state.removeFromCart)
  const guestClear = useCartStore((state) => state.clearCart)

  const { data: serverCart } = useQuery({
    queryKey: ['cart'],
    queryFn: cartService.getCart,
    enabled: isAuthenticated
  })

  const invalidateCart = () => queryClient.invalidateQueries({ queryKey: ['cart'] })

  const addMutation = useMutation({
    mutationFn: (item: { productSkuId: number; quantity: number }) => cartService.addToCart(item),
    onSuccess: invalidateCart
  })

  const applyDiscountMutation = useMutation({
    mutationFn: (code: string) => cartService.applyDiscountCode({ code }),
    onSuccess: invalidateCart
  })

  const updateQuantityMutation = useMutation({
    mutationFn: ({ productSkuId, quantity }: { productSkuId: number; quantity: number }) =>
      cartService.updateQuantity(productSkuId, { quantity }),
    onSuccess: invalidateCart
  })

  const removeMutation = useMutation({
    mutationFn: (productSkuId: number) => cartService.removeFromCart(productSkuId),
    onSuccess: invalidateCart
  })

  const removeDiscountMutation = useMutation({
    mutationFn: cartService.removeDiscountCode,
    onSuccess: invalidateCart
  })

  const clearMutation = useMutation({
    mutationFn: cartService.clearCart,
    onSuccess: invalidateCart
  })

  const items = isAuthenticated ? (serverCart?.items ?? []) : guestItems

  const totalItems = items.reduce((total, item) => total + item.quantity, 0)

  const discountCode = isAuthenticated ? (serverCart?.discountCode ?? null) : null
  const discountPercentage = isAuthenticated ? (serverCart?.discountPercentage ?? 0) : 0
  const getLineTotal = (item: Priced & { quantity: number }) => getUnitPrice(item) * item.quantity
  const subtotal = items.reduce((total, item) => total + getLineTotal(item), 0)
  const discountAmount = (subtotal * discountPercentage) / 100
  const { isError: isDiscountCodeError, error: discountCodeError } = applyDiscountMutation

  const isInCart = (productSkuId: number) =>
    items.some((item) => item.productSkuId === productSkuId)

  const addToCart = (item: GuestCartItem, onSuccess?: () => void) => {
    if (isAuthenticated) {
      addMutation.mutate(
        { productSkuId: item.productSkuId, quantity: item.quantity },
        { onSuccess }
      )

      return
    }

    guestAdd(item)
    onSuccess?.()
  }

  const applyDiscountCode = (code: string) => {
    if (!isAuthenticated) {
      return
    }

    applyDiscountMutation.mutate(code)
  }

  const getQuantity = (productSkuId: number) =>
    items.find((item) => item.productSkuId === productSkuId)?.quantity ?? 0

  const updateQuantity = (productSkuId: number, quantity: number) => {
    if (isAuthenticated) {
      updateQuantityMutation.mutate({ productSkuId, quantity })

      return
    }

    guestUpdateQuantity(productSkuId, quantity)
  }

  const removeFromCart = (productSkuId: number) => {
    if (isAuthenticated) {
      removeMutation.mutate(productSkuId)

      return
    }

    guestRemove(productSkuId)
  }

  const removeDiscountCode = () => {
    if (!isAuthenticated) {
      return
    }

    removeDiscountMutation.mutate()
  }

  const clearCart = () => {
    if (isAuthenticated) {
      clearMutation.mutate()

      return
    }

    guestClear()
  }

  return {
    isAuthenticated,
    items,
    totalItems,
    discountCode,
    discountPercentage,
    subtotal,
    discountAmount,
    isDiscountCodeError,
    discountCodeError,
    isInCart,
    addToCart,
    applyDiscountCode,
    getQuantity,
    getLineTotal,
    updateQuantity,
    removeFromCart,
    removeDiscountCode,
    clearCart
  }
}
