import { useSearchParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'

import { productService } from '@/services/productService'

export const useSearchProducts = () => {
  const [searchParams] = useSearchParams();
  
  const q = searchParams.get("q") || "";
  const urlPage = Number(searchParams.get("page")) || 1;
  const size = Number(searchParams.get("size")) || 12;

  const displayPage = urlPage < 1 ? 1 : urlPage;
  const apiPage = displayPage - 1;

  return useQuery({
    queryKey: ["products-search", { q, page: displayPage, size }],
    queryFn: () => productService.searchProducts({
      q,
      page: apiPage,
      size
    }),
    placeholderData: (previousData) => previousData
  });
};
