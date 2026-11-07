import { useState } from 'react'
import { isAxiosError } from 'axios'
import { toast } from 'sonner'
import { FormError } from '@/components/ui/FormError'
import { useAdminDeleteProductSku } from '@/hooks/admin/useAdminDeleteProductSku'
import type { AdminProductSkuResponse } from '@/types/admin/adminProductSku'

interface AdminDeleteVariantDialogProps {
  productId: number
  variant: AdminProductSkuResponse
  onClose: () => void
}

const formatSize = (size: string) => (size.startsWith('_') ? size.substring(1) : size)

const getErrorMessage = (error: unknown, fallback: string) =>
  isAxiosError<{ message?: string }>(error) ? (error.response?.data?.message ?? fallback) : fallback

export const AdminDeleteVariantDialog = ({ productId, variant, onClose }: AdminDeleteVariantDialogProps) => {
  const [error, setError] = useState<string | null>(null)
  const { mutate: deleteSku, isPending } = useAdminDeleteProductSku(productId)

  const confirm = () => {
    setError(null)

    deleteSku(variant.id, {
      onSuccess: () => {
        toast.success('Variant deleted')
        onClose()
      },
      onError: (mutationError) => setError(getErrorMessage(mutationError, 'Could not delete the variant'))
    })
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 p-4 backdrop-blur-sm">
      <div className="w-full max-w-md border border-neon-pink/40 bg-card p-6 shadow-2xl shadow-neon-pink/10">
        <p className="font-mono text-[10px] uppercase tracking-[0.4em] text-neon-pink">Delete</p>
        <h2 className="mt-2 font-display text-2xl uppercase tracking-tighter">
          {formatSize(variant.size)} · {variant.color}
        </h2>
        <p className="mt-3 text-sm text-muted-foreground">
          This removes the variant, its stock and its images. It cannot be deleted if it has orders.
        </p>
        {error && (
          <div className="mt-4">
            <FormError message={error} />
          </div>
        )}
        <div className="mt-6 flex justify-end gap-2">
          <button
            onClick={onClose}
            className="border border-border px-5 py-3 text-xs font-black uppercase tracking-widest hover:border-foreground/40"
          >
            Cancel
          </button>
          <button
            onClick={confirm}
            disabled={isPending}
            className="bg-neon-pink px-5 py-3 text-xs font-black uppercase tracking-widest text-primary-foreground hover:opacity-80 disabled:opacity-50"
          >
            Delete variant
          </button>
        </div>
      </div>
    </div>
  )
}
