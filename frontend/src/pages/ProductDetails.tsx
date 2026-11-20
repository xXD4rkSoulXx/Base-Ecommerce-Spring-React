import { useParams } from 'react-router-dom'
import { ProductOverviewSection } from '@/components/section/ProductOverviewSection'
import { RelatedProductsSection } from '@/components/section/RelatedProductsSection'
import { ReviewsSection } from '@/components/section/ReviewsSection'
import { useDocumentTitle } from '@/hooks/useDocumentTitle'
import { useProductDetails } from '@/hooks/useProductDetails'
import { useRelatedProducts } from '@/hooks/useRelatedProducts'
import { ErrorPage } from '@/pages/ErrorPage'
import { Loading } from '@/pages/Loading'

export const ProductDetails = () => {
  const { id } = useParams<{ id: string }>()
  const { data: product, isLoading, isError } = useProductDetails(id)
  const { data: relatedProducts } = useRelatedProducts(id)

  useDocumentTitle(product?.name)

  if (isLoading) {
    return <Loading />
  }

  if (isError || !product) {
    return <ErrorPage />
  }

  return (
    <>
      <ProductOverviewSection key={product.id} product={product} />
      <RelatedProductsSection products={relatedProducts} />
      <ReviewsSection key={product.id} product={product} />
    </>
  )
}
