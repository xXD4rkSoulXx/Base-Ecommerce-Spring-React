import { useRef, useState, type ChangeEvent, type DragEvent } from 'react'
import { isAxiosError } from 'axios'
import { toast } from 'sonner'
import { ImagePlus, X } from 'lucide-react'
import { FormError } from '@/components/ui/FormError'
import { useAdminProductSkuImages } from '@/hooks/admin/useAdminProductSkuImages'
import { useAdminDeleteProductSkuImage } from '@/hooks/admin/useAdminDeleteProductSkuImage'
import { useAdminSetProductCover } from '@/hooks/admin/useAdminSetProductCover'
import { useAdminSetSkuCover } from '@/hooks/admin/useAdminSetSkuCover'
import { useAdminUploadProductSkuImage } from '@/hooks/admin/useAdminUploadProductSkuImage'
import type { AdminProductSkuResponse } from '@/types/admin/adminProductSku'

interface AdminVariantImagesDialogProps {
  productId: number
  variant: AdminProductSkuResponse
  onClose: () => void
}

const formatSize = (size: string) => (size.startsWith('_') ? size.substring(1) : size)

const getErrorMessage = (error: unknown, fallback: string) =>
  isAxiosError<{ message?: string }>(error) ? (error.response?.data?.message ?? fallback) : fallback

export const AdminVariantImagesDialog = ({ productId, variant, onClose }: AdminVariantImagesDialogProps) => {
  const { data: images, isLoading, isError } = useAdminProductSkuImages(productId, variant.id)
  const { mutate: uploadImage, isPending: isUploading } = useAdminUploadProductSkuImage(productId, variant.id)
  const { mutate: setSkuCover, isPending: isSettingSkuCover } = useAdminSetSkuCover(productId, variant.id)
  const { mutate: setProductCover, isPending: isSettingProductCover } = useAdminSetProductCover(productId, variant.id)
  const { mutate: deleteImage, isPending: isDeleting } = useAdminDeleteProductSkuImage(productId, variant.id)
  const inputRef = useRef<HTMLInputElement>(null)
  const [error, setError] = useState<string | null>(null)
  const [isDragging, setIsDragging] = useState(false)
  const isBusy = isUploading || isSettingSkuCover || isSettingProductCover || isDeleting

  const sendFile = (file: File) => {
    if (!file.type.startsWith('image/')) {
      setError('Only image files are accepted')
      return
    }

    if (file.size > 5 * 1024 * 1024) {
      setError('The image must be at most 5 MB')
      return
    }

    setError(null)

    uploadImage(file, {
      onSuccess: () => toast.success('Image uploaded'),
      onError: (mutationError) => setError(getErrorMessage(mutationError, 'Could not upload the image'))
    })
  }

  const selectFile = (e: ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    e.target.value = ''

    if (file) {
      sendFile(file)
    }
  }

  const dropFile = (e: DragEvent<HTMLButtonElement>) => {
    e.preventDefault()
    setIsDragging(false)

    const file = e.dataTransfer.files[0]

    if (file && !isBusy) {
      sendFile(file)
    }
  }

  const makeSkuCover = (imageId: number) => {
    setError(null)

    setSkuCover(imageId, {
      onSuccess: () => toast.success('Variant cover updated'),
      onError: (mutationError) => setError(getErrorMessage(mutationError, 'Could not update the variant cover'))
    })
  }

  const makeProductCover = (imageId: number) => {
    setError(null)

    setProductCover(imageId, {
      onSuccess: () => toast.success('Product cover updated'),
      onError: (mutationError) => setError(getErrorMessage(mutationError, 'Could not update the product cover'))
    })
  }

  const remove = (imageId: number) => {
    setError(null)

    deleteImage(imageId, {
      onSuccess: () => toast.success('Image removed'),
      onError: (mutationError) => setError(getErrorMessage(mutationError, 'Could not remove the image'))
    })
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-background/80 p-4 backdrop-blur-sm">
      <div className="max-h-[90vh] w-full max-w-2xl overflow-y-auto border border-neon-cyan/40 bg-card p-6 shadow-2xl shadow-neon-cyan/10">
        <div className="flex items-start justify-between">
          <div>
            <p className="font-mono text-[10px] uppercase tracking-[0.4em] text-neon-cyan">Images</p>
            <h2 className="mt-2 font-display text-2xl uppercase tracking-tighter">
              {formatSize(variant.size)} · {variant.color}
            </h2>
          </div>
          <button onClick={onClose} className="text-muted-foreground hover:text-foreground">
            <X className="h-5 w-5" />
          </button>
        </div>
        <p className="mt-3 text-xs text-muted-foreground">
          JPEG, PNG or WebP, up to 5 MB. The variant cover is shown in the cart and in orders; the product
          cover is shown in the shop lists. The first image of a variant (and of the product) becomes the
          cover automatically.
        </p>
        <input ref={inputRef} type="file" accept="image/jpeg,image/png,image/webp" onChange={selectFile} className="hidden" />
        <button
          type="button"
          disabled={isBusy}
          onClick={() => inputRef.current?.click()}
          onDragOver={(e) => {
            e.preventDefault()
            setIsDragging(true)
          }}
          onDragLeave={() => setIsDragging(false)}
          onDrop={dropFile}
          className={`mt-5 flex w-full flex-col items-center justify-center gap-2 border-2 border-dashed px-6 py-8 transition-colors disabled:opacity-50 ${
            isDragging
              ? 'border-neon-cyan bg-neon-cyan/10 text-neon-cyan'
              : 'border-border text-muted-foreground hover:border-neon-cyan/60 hover:text-foreground'
          }`}
        >
          <ImagePlus className="h-6 w-6" />
          <span className="font-mono text-[10px] uppercase tracking-[0.3em]">
            {isUploading ? 'Uploading...' : 'Drag an image here or click to choose'}
          </span>
        </button>
        {isLoading && (
          <p className="mt-5 font-mono text-xs uppercase tracking-widest text-muted-foreground">Loading...</p>
        )}
        {isError && (
          <div className="mt-5">
            <FormError message="Could not load the images" />
          </div>
        )}
        {images && images.length === 0 && (
          <p className="mt-5 text-center font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">
            No images yet
          </p>
        )}
        {images && images.length > 0 && (
          <div className="mt-5 grid grid-cols-2 gap-3 sm:grid-cols-3">
            {images.map((image) => (
              <div key={image.id} className="border border-border">
                <div className="relative aspect-square bg-background">
                  <img
                    src={image.path}
                    alt={`${formatSize(variant.size)} ${variant.color}`}
                    loading="lazy"
                    className="h-full w-full object-cover"
                  />
                  <div className="absolute left-2 top-2 flex flex-col items-start gap-1">
                    {image.skuCover && (
                      <span className="bg-neon-cyan px-2 py-1 font-mono text-[10px] font-bold uppercase tracking-widest text-primary-foreground">
                        Variant cover
                      </span>
                    )}
                    {image.productCover && (
                      <span className="bg-neon-pink px-2 py-1 font-mono text-[10px] font-bold uppercase tracking-widest text-primary-foreground">
                        Product cover
                      </span>
                    )}
                  </div>
                </div>
                <div className="flex items-start justify-between gap-2 p-2 text-[10px] font-bold uppercase tracking-widest">
                  <div className="flex flex-col items-start gap-1">
                    {!image.skuCover && (
                      <button
                        onClick={() => makeSkuCover(image.id)}
                        disabled={isBusy}
                        className="hover:text-neon-cyan disabled:opacity-50"
                      >
                        Set as variant cover
                      </button>
                    )}
                    {!image.productCover && (
                      <button
                        onClick={() => makeProductCover(image.id)}
                        disabled={isBusy}
                        className="hover:text-neon-pink disabled:opacity-50"
                      >
                        Set as product cover
                      </button>
                    )}
                  </div>
                  <button
                    onClick={() => remove(image.id)}
                    disabled={isBusy}
                    className="text-neon-pink hover:opacity-80 disabled:opacity-50"
                  >
                    Remove
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
        {error && (
          <div className="mt-4">
            <FormError message={error} />
          </div>
        )}
        <div className="mt-6 flex justify-end">
          <button
            onClick={onClose}
            className="border border-border px-5 py-3 text-xs font-black uppercase tracking-widest hover:border-foreground/40"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  )
}
