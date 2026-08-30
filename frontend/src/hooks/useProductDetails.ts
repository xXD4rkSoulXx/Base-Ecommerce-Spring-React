import { useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { productService } from '@/services/product.service'

export const useProductDetails = () => {
  const { id } = useParams<{ id: string }>()

  return useQuery({
    queryKey: ['product', id],
    queryFn: () => productService.getProductDetails(id!),
    enabled: !!id
  })
}
