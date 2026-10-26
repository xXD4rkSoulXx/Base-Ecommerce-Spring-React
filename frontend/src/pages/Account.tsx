import { useLogout } from '@/hooks/useLogout'
import { OrderHistorySection } from '@/components/section/OrderHistorySection'

export const Account = () => {
  const { mutate: handleLogout, isPending } = useLogout()

  return (
    <div className="mx-auto max-w-[1440px] px-6 py-16 md:px-10">
      <h1 className="mt-4 font-display text-5xl uppercase tracking-tighter md:text-7xl">
        Your <span className="text-neon-cyan">Console</span>
      </h1>
      <div className="mt-12 grid gap-12 lg:grid-cols-[1fr_2fr]">
        <aside className="self-start border border-border bg-card p-8">
          <button
            onClick={() => handleLogout()}
            disabled={isPending}
            className="w-full border border-border py-3 text-xs font-bold uppercase tracking-widest hover:border-destructive hover:text-destructive"
          >
            Sign Out
          </button>
        </aside>
        <OrderHistorySection />
      </div>
    </div>
  )
}
