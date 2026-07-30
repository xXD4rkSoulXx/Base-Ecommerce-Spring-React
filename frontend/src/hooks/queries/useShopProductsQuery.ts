import { useSearchParams } from 'react-router-dom'
import { useInfiniteQuery } from '@tanstack/react-query'

import type { ProductCategory } from '@/types/Product'

import { productService } from '@/services/productService'


export const useShopProductsQuery = () => {
  const [searchParams] = useSearchParams();
  
  const category = (searchParams.get("category") || "ALL") as ProductCategory | "ALL";
  const sortBy = searchParams.get("sort") || "recent";

  return useInfiniteQuery({
    queryKey: ["products", { category, sortBy }],
    queryFn: ({ pageParam = 0 }) => productService.fetchShopProducts(category, sortBy, pageParam),
    initialPageParam: 0,
    getNextPageParam: (lastPage) => {
      if (lastPage.last) {
        return undefined;
      }

      return lastPage.page + 1;
    }
  });
};
