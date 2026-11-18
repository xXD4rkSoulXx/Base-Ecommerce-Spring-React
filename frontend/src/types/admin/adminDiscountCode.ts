export interface AdminCreateDiscountCodeRequest extends AdminUpdateDiscountCodeRequest {
  code: string
}

export interface AdminUpdateDiscountCodeRequest {
  discountPercentage: number
}

export interface AdminDiscountCodeSearchRequest {
  q?: string
  active?: boolean
  page: number
  size: number
}

export interface AdminDiscountCodeResponse {
  id: number
  code: string
  discountPercentage: number
  active: boolean
  orders: number
  createdAt: string
}
