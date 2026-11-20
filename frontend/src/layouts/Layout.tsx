import { Outlet } from 'react-router-dom'
import { ScrollToTop } from '@/components/layout/ScrollToTop'
import { SkipLink } from '@/components/layout/SkipLink'
import { NavBar } from '@/components/layout/NavBar'
import { Ticker } from '@/components/layout/Ticker'
import { Footer } from '@/components/layout/Footer'
import { MAIN_CONTENT_ID } from '@/constants'
import { useRouteTitle } from '@/hooks/useRouteTitle'

export const Layout = () => {
  useRouteTitle()

  return (
    <div className="min-h-dvh bg-background text-foreground">
      <SkipLink />
      <ScrollToTop />
      <NavBar />
      <Ticker />
      <main id={MAIN_CONTENT_ID} tabIndex={-1} className="focus:outline-none">
        <Outlet />
      </main>
      <Footer />
    </div>
  )
}
