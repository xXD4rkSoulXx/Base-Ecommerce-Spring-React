import type { AdminProductSkuImageResponse } from '@/types/admin/adminProductSkuImage'
import type { AdminReturnResponse } from '@/types/admin/adminReturn'
import type { OrderDetailsResponse, OrderStatus, ShippingAddress } from '@/types/order'
import type { ProductCategory, ProductColor, ProductSize, FeaturedSlot } from '@/types/product'

export interface DemoSku {
  id: number
  productId: number
  size: ProductSize
  color: ProductColor
  stock: number
  weight: number
  length: number
  width: number
  height: number
  discountPercentage: number
  active: boolean
  images: AdminProductSkuImageResponse[]
}

export interface DemoProduct {
  id: number
  ref: string
  name: string
  description: string
  price: number
  discountPercentage: number
  category: ProductCategory
  featuredSlot?: FeaturedSlot
  active: boolean
  skus: DemoSku[]
}

export interface DemoCustomer {
  id: number
  email: string
  firstName: string
  lastName: string
  createdAt: number
  address: ShippingAddress
}

export interface DemoOrderLine {
  productId: number
  skuId: number
  quantity: number
}

export interface DemoOrder {
  id: number
  customerId: number
  customerEmail: string
  customerName: string
  createdAt: number
  status: OrderStatus
  discountCode?: string
  lines: DemoOrderLine[]
  details: OrderDetailsResponse
}

export interface DemoReturn {
  createdAt: number
  refundedAt?: number
  response: AdminReturnResponse
}

export interface DemoReview {
  id: number
  productId: number
  productName: string
  productRef: string
  customerName: string
  customerEmail: string
  rating: number
  title: string
  description: string
  hidden: boolean
  createdAt: number
}

export interface DemoDiscountCode {
  id: number
  code: string
  discountPercentage: number
  active: boolean
  createdAt: number
}
