import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { isAxiosError } from 'axios'
import { toast } from 'sonner'
import { ArrowLeft } from 'lucide-react'
import { AdminProductForm } from '@/components/section/admin/AdminProductForm'
import { useAdminCreateProduct } from '@/hooks/admin/useAdminCreateProduct'
import type { AdminProductRequest } from '@/types/admin/adminProduct'

const getErrorMessage = (error: unknown, fallback: string) =>
  isAxiosError<{ message?: string }>(error) ? (error.response?.data?.message ?? fallback) : fallback

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
      onError: (mutationError) =>
        setError(getErrorMessage(mutationError, 'Could not create the product'))
    })
  }

  return (
    <div className="p-6 md:p-10">
      <Link
        to="/admin/products"
        className="mb-6 inline-flex items-center gap-2 font-mono text-[10px] uppercase tracking-widest text-muted-foreground hover:text-neon-cyan"
      >
        <ArrowLeft className="h-3.5 w-3.5" /> Products
      </Link>
      <h1 className="mb-8 font-display text-5xl uppercase tracking-tighter md:text-7xl">New product</h1>
      <AdminProductForm
        submitLabel="Create product"
        isPending={isPending}
        error={error}
        onSubmit={submit}
      />
    </div>
  )
}
