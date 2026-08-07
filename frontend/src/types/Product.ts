export type ProductCategory = "OUTERWEAR" | "TOPS" | "BOTTOMS" | "FOOTWEAR" | "UNDERWEAR" | "ACCESSORIES";

export interface ProductShopRequest {
  category?: ProductCategory | "ALL";
  page?: number;
  size?: number;
  sortBy?: "recent" | "price_asc" | "price_desc" | string;
}

export interface ProductSearchRequest {
  q?: string;
  page?: number;
  size?: number;
}

export interface ProductResponse {
  id: number;
  ref: string;
  name: string;
  price: number;
  discountPercentage: number;
  category: ProductCategory;
  cover: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}
