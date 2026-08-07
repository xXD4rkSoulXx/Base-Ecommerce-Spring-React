import { useSearchParams } from 'react-router-dom'
import { useInfiniteQuery } from '@tanstack/react-query'

import type { ProductCategory } from '@/types/Product'

import { productService } from '@/services/productService'


export const useGetInfiniteProducts = () => {
  const [searchParams] = useSearchParams();
  
  const category = (searchParams.get("category") || "ALL") as ProductCategory | "ALL";
  const size = Number(searchParams.get("size")) || 12;
  const sortBy = searchParams.get("sort") || "recent";

  return useInfiniteQuery({
    queryKey: ["products", { category, size, sortBy }],
    queryFn: ({ pageParam: page = 0 }) => productService.getProducts({
      category,
      page,
      size,
      sortBy
    }),
    initialPageParam: 0,
    getNextPageParam: (lastPage) => {
      if (lastPage.last) {
        return undefined;
      }

      return lastPage.page + 1;
    }
  });
};
