import { useProducts } from "@/hooks/useProducts";
import { useMemo, useState } from "react";
import Loading from "./Loading";
import Error from "./Error";
import { SearchIcon } from "lucide-react";
import { ProductCard } from "@/components/product/ProductCard";

function Search() {
  const [q, setQ] = useState("");
  const {data: products, isLoading, error} = useProducts();

  const results = useMemo(() => {
    if (!products || !q.trim()) return [];

    const term = q.toLowerCase();

    return products.filter(
      (p: { name: string; category: string; ref: string; }) =>
        p.name.toLowerCase().includes(term) ||
        p.category.toLowerCase().includes(term) ||
        p.ref.toLowerCase().includes(term)
    );
  }, [q, products]);

  if (isLoading) {
    return <Loading />;
  }

  if (error || !products) {
    return <Error />;
  }

  return (
    <div className="mx-auto max-w-[1440px] px-6 py-16 md:px-10">
      <p className="font-mono text-[10px] uppercase tracking-[0.4em] text-muted-foreground">
        Archive Search
      </p>
      <h1 className="mt-4 font-display text-5xl uppercase tracking-tighter md:text-7xl">
        Locate
      </h1>

      <div className="mt-10 flex items-center gap-3 border-b-2 border-foreground pb-3">
        <SearchIcon className="h-5 w-5 text-neon-cyan" />
        <input
          autoFocus
          value={q}
          onChange={(e) => setQ(e.target.value)}
          placeholder="Search by name, REF, or category…"
          className="w-full bg-transparent text-2xl placeholder:text-muted-foreground focus:outline-none md:text-3xl"
        />
        {q && (
          <button onClick={() => setQ("")} className="text-xs uppercase text-muted-foreground hover:text-foreground">
            Clear
          </button>
        )}
      </div>

      {q && (
        <div className="mt-12">
          <p className="mb-8 font-mono text-xs text-muted-foreground">
            {results.length} result{results.length !== 1 && "s"} for "{q}"
          </p>
          <div className="grid grid-cols-2 gap-x-4 gap-y-12 md:grid-cols-4 lg:gap-x-6">
            {results.map((p: { id: number; }, i: number) => (
              <ProductCard key={p.id} product={p} index={i} />
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default Search
