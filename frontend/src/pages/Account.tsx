import { OrderHistorySection } from '@/components/section/OrderHistorySection'
import { ProfileSection } from '@/components/section/ProfileSection'

export const Account = () => (
  <div className="mx-auto max-w-[1440px] px-6 py-16 md:px-10">
    <h1 className="mt-4 font-display text-5xl uppercase tracking-tighter md:text-7xl">
      Your <span className="text-neon-cyan">Console</span>
    </h1>
    <div className="mt-12 grid gap-12 lg:grid-cols-[1fr_2fr]">
      <ProfileSection />
      <OrderHistorySection />
    </div>
  </div>
)
