import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { toast } from 'sonner'
import { AdminProductForm } from '@/components/form/admin/AdminProductForm'
import { AdminBackLink } from '@/components/ui/admin/AdminBackLink'
import { AdminPageTitle } from '@/components/ui/admin/AdminPageTitle'
import { useAdminCreateProduct } from '@/hooks/admin/useAdminCreateProduct'
import type { AdminProductRequest } from '@/types/admin/adminProduct'
import { getErrorMessage } from '@/utils/error'

export const AdminProductCreate = () => {
  const navigate = useNavigate()
  const [error, setError] = useState<string | null>(null)
  const { mutate: createProduct, isPending } = useAdminCreateProduct()

  const submit = (request: AdminProductRequest) => {
    setError(null)

    createProduct(request, {
      onSuccess: (product) => {
        toast.success('Product created')
        navigate(`/admin/products/${product.id}`, { replace: true })
      },
      onError: (mutationError) => setError(getErrorMessage(mutationError, 'Could not create the product'))
    })
  }

  return (
    <div className="p-6 md:p-10">
      <AdminBackLink to="/admin/products">Products</AdminBackLink>
      <AdminPageTitle>New product</AdminPageTitle>
      <AdminProductForm submitLabel="Create product" isPending={isPending} error={error} onSubmit={submit} />
    </div>
  )
}
