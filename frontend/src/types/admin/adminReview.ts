export interface AdminReviewSearchRequest {
  q?: string
  hidden?: boolean
  page?: number
  size?: number
}

export interface AdminReviewResponse {
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
  createdAt: string
}
