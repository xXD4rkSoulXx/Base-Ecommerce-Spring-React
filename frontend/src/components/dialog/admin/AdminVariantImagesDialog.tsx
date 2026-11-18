import { useRef, useState, type ChangeEvent, type DragEvent } from 'react'
import { toast } from 'sonner'
import { ImagePlus } from 'lucide-react'
import { AdminSkuImageCard } from '@/components/card/admin/AdminSkuImageCard'
import { FormError } from '@/components/ui/FormError'
import { Modal } from '@/components/ui/Modal'
import { useAdminProductSkuImages } from '@/hooks/admin/useAdminProductSkuImages'
import { useAdminDeleteProductSkuImage } from '@/hooks/admin/useAdminDeleteProductSkuImage'
import { useAdminSetProductCover } from '@/hooks/admin/useAdminSetProductCover'
import { useAdminSetSkuCover } from '@/hooks/admin/useAdminSetSkuCover'
import { useAdminUploadProductSkuImage } from '@/hooks/admin/useAdminUploadProductSkuImage'
import { getErrorMessage } from '@/utils/error'
import { formatSize } from '@/utils/format'
import type { AdminProductSkuResponse } from '@/types/admin/adminProductSku'

interface AdminVariantImagesDialogProps {
  productId: number
  variant: AdminProductSkuResponse
  onClose: () => void
}

const ACCEPTED_IMAGE_TYPES = ['image/jpeg', 'image/png', 'image/webp']
const MAX_IMAGE_SIZE_MB = 5

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
    if (!ACCEPTED_IMAGE_TYPES.includes(file.type)) {
      setError('Only JPEG, PNG or WebP images are accepted')
      return
    }

    if (file.size > MAX_IMAGE_SIZE_MB * 1024 * 1024) {
      setError(`The image must be at most ${MAX_IMAGE_SIZE_MB} MB`)
      return
    }

    setError(null)

    uploadImage(file, {
      onSuccess: () => toast.success('Image uploaded'),
      onError: (mutationError) => setError(getErrorMessage(mutationError, 'Could not upload the image'))
    })
  }

  const selectFile = (event: ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]
    event.target.value = ''

    if (file) {
      sendFile(file)
    }
  }

  const dropFile = (event: DragEvent<HTMLButtonElement>) => {
    event.preventDefault()
    setIsDragging(false)

    const file = event.dataTransfer.files[0]

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
    <Modal
      eyebrow="Images"
      title={`${formatSize(variant.size)} · ${variant.color}`}
      description={`JPEG, PNG or WebP, up to ${MAX_IMAGE_SIZE_MB} MB. The variant cover is shown in the cart and in orders; the product cover is shown in the shop lists. The first image of a variant (and of the product) becomes the cover automatically.`}
      size="2xl"
      onClose={onClose}
    >
      <input
        ref={inputRef}
        type="file"
        accept={ACCEPTED_IMAGE_TYPES.join(',')}
        onChange={selectFile}
        className="hidden"
      />
      <button
        type="button"
        disabled={isBusy}
        onClick={() => inputRef.current?.click()}
        onDragOver={(event) => {
          event.preventDefault()
          setIsDragging(true)
        }}
        onDragLeave={() => setIsDragging(false)}
        onDrop={dropFile}
        className={`flex w-full flex-col items-center justify-center gap-2 border-2 border-dashed px-6 py-8 transition-colors disabled:opacity-50 ${
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
      {isLoading && <p className="mt-5 font-mono text-xs uppercase tracking-widest text-muted-foreground">Loading...</p>}
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
          {images.map((image, index) => (
            <AdminSkuImageCard
              key={image.id}
              image={image}
              alt={`Image ${index + 1}`}
              isBusy={isBusy}
              onSetSkuCover={makeSkuCover}
              onSetProductCover={makeProductCover}
              onRemove={remove}
            />
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
          type="button"
          onClick={onClose}
          className="border border-border px-5 py-3 text-xs font-black uppercase tracking-widest hover:border-foreground/40"
        >
          Close
        </button>
      </div>
    </Modal>
  )
}
