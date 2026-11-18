import { Outlet } from 'react-router-dom'
import { ScrollToTop } from '@/components/layout/ScrollToTop'
import { NavBar } from '@/components/layout/NavBar'
import { Ticker } from '@/components/layout/Ticker'
import { Footer } from '@/components/layout/Footer'

export const Layout = () => (
  <div className="min-h-dvh bg-background text-foreground">
    <ScrollToTop />
    <NavBar />
    <Ticker />
    <main>
      <Outlet />
    </main>
    <Footer />
  </div>
)
