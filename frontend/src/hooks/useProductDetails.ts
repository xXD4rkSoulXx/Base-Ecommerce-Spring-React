import { useQuery } from '@tanstack/react-query'
import { productService } from '@/services/product.service'

export const useProductDetails = (id: string | undefined) =>
  useQuery({
    queryKey: ['product', id],
    queryFn: () => productService.getProductDetails(id!),
    enabled: !!id
  })
