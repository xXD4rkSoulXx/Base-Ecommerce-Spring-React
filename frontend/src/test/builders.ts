import type { GuestCartItem } from '@/store/useCartStore'
import type { ProductDetailsResponse, ProductResponse, ProductSkuDetailsResponse } from '@/types/product'
import type { UserResponse } from '@/types/user'

export const buildUser = (overrides: Partial<UserResponse> = {}): UserResponse => ({
  email: 'ana@mail.com',
  firstName: 'Ana',
  lastName: 'Costa',
  role: 'CUSTOMER',
  permissions: [],
  ...overrides
})

export const buildProduct = (overrides: Partial<ProductResponse> = {}): ProductResponse => ({
  id: 1,
  ref: 'REF-1',
  name: 'Rain Jacket',
  price: 100,
  discountPercentage: 0,
  category: 'OUTERWEAR',
  cover: '/covers/rain-jacket.webp',
  ...overrides
})

export const buildProductSku = (overrides: Partial<ProductSkuDetailsResponse> = {}): ProductSkuDetailsResponse => ({
  id: 1,
  size: 'S',
  color: 'BLACK',
  stock: 3,
  discountPercentage: 0,
  paths: ['/img/black-1.webp', '/img/black-2.webp'],
  ...overrides
})

export const buildProductDetails = (overrides: Partial<ProductDetailsResponse> = {}): ProductDetailsResponse => ({
  id: 1,
  ref: 'REF-1',
  name: 'Rain Jacket',
  description: 'Waterproof jacket.',
  price: 100,
  discountPercentage: 0,
  category: 'OUTERWEAR',
  averageRating: 4.5,
  totalReviews: 3,
  productSkus: [buildProductSku()],
  ...overrides
})

export const buildGuestCartItem = (overrides: Partial<GuestCartItem> = {}): GuestCartItem => ({
  productSkuId: 1,
  productId: 1,
  productName: 'Rain Jacket',
  productRef: 'REF-1',
  size: 'M',
  color: 'BLACK',
  price: 100,
  discountPercentage: 0,
  stock: 5,
  quantity: 1,
  available: true,
  ...overrides
})
