import type { AdminProductSkuImageResponse } from '@/types/admin/adminProductSkuImage'

interface AdminSkuImageCardProps {
  image: AdminProductSkuImageResponse
  alt: string
  isBusy: boolean
  onSetSkuCover: (imageId: number) => void
  onSetProductCover: (imageId: number) => void
  onRemove: (imageId: number) => void
}

export const AdminSkuImageCard = ({
  image,
  alt,
  isBusy,
  onSetSkuCover,
  onSetProductCover,
  onRemove
}: AdminSkuImageCardProps) => (
  <div className="border border-border">
    <div className="relative aspect-square bg-background">
      <img src={image.path} alt={alt} loading="lazy" className="h-full w-full object-cover" />
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
            type="button"
            onClick={() => onSetSkuCover(image.id)}
            disabled={isBusy}
            className="hover:text-neon-cyan disabled:opacity-50"
          >
            Set as variant cover
          </button>
        )}
        {!image.productCover && (
          <button
            type="button"
            onClick={() => onSetProductCover(image.id)}
            disabled={isBusy}
            className="hover:text-neon-pink disabled:opacity-50"
          >
            Set as product cover
          </button>
        )}
      </div>
      <button
        type="button"
        onClick={() => onRemove(image.id)}
        disabled={isBusy}
        className="text-neon-pink hover:opacity-80 disabled:opacity-50"
      >
        Remove
      </button>
    </div>
  </div>
)
