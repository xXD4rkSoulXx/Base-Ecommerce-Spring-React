import type { AdminDashboardEndpoints } from '@/types/admin/adminDashboard'

export const queryKeys = {
  bestSellers: ['products-best-sellers'] as const,
  cart: ['cart'] as const,
  dealProducts: ['products-deal'] as const,
  featuredProducts: ['products-featured'] as const,
  newArrivals: ['products-new-arrivals'] as const,
  profile: ['profile'] as const,
  topRated: ['products-top-rated'] as const,
  wishlist: ['wishlist'] as const,
  order: (id: number | null) => ['order', id] as const,
  orders: {
    all: ['orders'] as const,
    list: (params: object) => ['orders', params] as const
  },
  product: (id: string | undefined) => ['product', id] as const,
  productRelated: (id: string | undefined) => ['product-related', id] as const,
  productReviews: (id: string | undefined) => ['product-reviews-infinite', id] as const,
  products: (params: object) => ['products', params] as const,
  productsSearch: (params: object) => ['products-search', params] as const,
  reviewEligibility: (id: string | undefined) => ['review-eligibility', id] as const,
  admin: {
    auditLogs: (params: object) => ['admin', 'audit-logs', params] as const,
    customer: (id: number) => ['admin', 'customer', id] as const,
    customerOrders: {
      all: ['admin', 'customer-orders'] as const,
      list: (id: number, params: object) => ['admin', 'customer-orders', id, params] as const
    },
    customers: {
      all: ['admin', 'customers'] as const,
      list: (params: object) => ['admin', 'customers', params] as const
    },
    dashboard: {
      all: ['admin', 'dashboard'] as const,
      query: (endpoint: keyof AdminDashboardEndpoints, days?: number) => ['admin', 'dashboard', endpoint, days] as const
    },
    discountCodes: {
      all: ['admin', 'discount-codes'] as const,
      list: (params: object) => ['admin', 'discount-codes', params] as const
    },
    order: {
      all: ['admin', 'order'] as const,
      detail: (id: number) => ['admin', 'order', id] as const
    },
    orders: {
      all: ['admin', 'orders'] as const,
      list: (params: object) => ['admin', 'orders', params] as const
    },
    product: (id: number) => ['admin', 'product', id] as const,
    products: {
      all: ['admin', 'products'] as const,
      list: (params: object) => ['admin', 'products', params] as const
    },
    productSkuImages: {
      byProduct: (productId: number) => ['admin', 'product-sku-images', productId] as const,
      bySku: (productId: number, skuId: number) => ['admin', 'product-sku-images', productId, skuId] as const
    },
    productSkus: {
      all: ['admin', 'product-skus'] as const,
      byProduct: (productId: number) => ['admin', 'product-skus', productId] as const
    },
    returns: {
      all: ['admin', 'returns'] as const,
      list: (params: object) => ['admin', 'returns', params] as const
    },
    reviews: {
      all: ['admin', 'reviews'] as const,
      list: (params: object) => ['admin', 'reviews', params] as const
    },
    stock: {
      all: ['admin', 'stock'] as const,
      list: (params: object) => ['admin', 'stock', params] as const
    },
    team: ['admin', 'team'] as const
  }
}
