import { useQuery } from '@tanstack/react-query'
import { queryKeys } from '@/queryKeys'
import { productService } from '@/services/product.service'

export const useProductDetails = (id: string | undefined) =>
  useQuery({
    queryKey: queryKeys.product(id),
    queryFn: () => productService.getProductDetails(id!),
    enabled: !!id
  })
