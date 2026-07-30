export type ProductCategory = 'OUTERWEAR' | 'TOPS' | 'BOTTOMS' | 'FOOTWEAR' | 'UNDERWEAR' | 'ACCESSORIES';

export interface ProductResponse {
  id: number;
  ref: string;
  name: string;
  price: number;
  discountPercentage: number;
  category: ProductCategory;
  cover: string;
}

export interface SpringPage<T> {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
  first: boolean;
}
