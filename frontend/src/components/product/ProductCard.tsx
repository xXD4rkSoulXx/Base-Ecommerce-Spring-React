import { Link } from "react-router-dom";
import type { Product } from "@/types/Product";

export const ProductCard = ({ product, index = 0 }: { product: Product; index?: number }) => {
  const offset = index % 2 === 1 ? "lg:translate-y-12" : "";
  const final = product.discount ?
    Math.round(product.price * (1 - product.discount / 100)) :
    product.price;
  const hasDiscount = !!product.discount;

  return (
    <Link
      to={`/product/${product.id}`}
      className={`group block ${offset}`}
    >
      <div className="relative aspect-[3/4] overflow-hidden border border-border bg-card">
        <img
          src={product.image}
          alt={product.name}
          loading="lazy"
          className="h-full w-full object-cover grayscale-[40%] contrast-110 transition-all duration-700 group-hover:scale-105 group-hover:grayscale-0"
        />
        <div className="absolute left-3 top-3 flex flex-col gap-1">
          <span className="bg-background/70 px-2 py-1 font-mono text-[9px] font-bold uppercase tracking-widest backdrop-blur">
            REF: {product.ref}
          </span>
          {(new Date(product.createdAt) >= new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)) && (
            <span className="bg-neon-pink px-2 py-1 font-mono text-[9px] font-extrabold uppercase tracking-widest text-primary-foreground">
              New
            </span>
          )}
          {product.isLimited && (
            <span className="bg-neon-cyan px-2 py-1 font-mono text-[9px] font-extrabold uppercase tracking-widest text-primary-foreground">
              Limited
            </span>
          )}
        </div>
        {hasDiscount && (
          <div className="absolute right-3 top-3 flex h-12 w-12 items-center justify-center bg-neon-pink font-mono text-xs font-black tracking-tighter text-primary-foreground shadow-lg shadow-neon-pink/30">
            -{product.discount}%
          </div>
        )}
        <div className="absolute inset-x-0 bottom-0 translate-y-full bg-neon-cyan p-3 text-center transition-transform duration-300 group-hover:translate-y-0">
          <span className="text-xs font-black uppercase tracking-widest text-primary-foreground">
            View Object
          </span>
        </div>
      </div>
      <div className="mt-4 flex items-start justify-between gap-3">
        <div>
          <h3 className="font-bold uppercase tracking-tight">{product.name}</h3>
          <p className="mt-1 font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
            {product.category}
          </p>
        </div>
        <div className="flex shrink-0 flex-col items-end leading-tight">
          {hasDiscount ? (
            <>
              <span className="font-mono text-sm tracking-tighter text-neon-pink">
                {final.toFixed(2)}€
              </span>
              <span className="font-mono text-[10px] text-muted-foreground line-through">
                {product.price.toFixed(2)}€
              </span>
            </>
          ) : (
            <span className="font-mono text-sm tracking-tighter text-neon-cyan">
              {product.price.toFixed(2)}€
            </span>
          )}
        </div>
      </div>
    </Link>
  );
};
