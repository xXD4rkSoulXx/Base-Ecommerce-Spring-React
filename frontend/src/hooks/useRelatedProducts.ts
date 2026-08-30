import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { productService } from '@/services/product.service'

export const useRelatedProducts = () => {
  const { id } = useParams<{ id: string }>()

  return useQuery({
    queryKey: ['product-related', id],
    queryFn: () => productService.getRelatedProducts(id!),
    enabled: !!id
  })
}
