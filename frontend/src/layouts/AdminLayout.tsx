import { useState } from 'react'
import { Outlet } from 'react-router-dom'
import { ScrollToTop } from '@/components/layout/ScrollToTop'
import { SkipLink } from '@/components/layout/SkipLink'
import { AdminSidebar } from '@/components/layout/admin/AdminSidebar'
import { AdminHeaderMobile } from '@/components/layout/admin/AdminHeaderMobile'
import { AdminMenuMobile } from '@/components/layout/admin/AdminMenuMobile'
import { MAIN_CONTENT_ID } from '@/constants'
import { useRouteTitle } from '@/hooks/useRouteTitle'

export const AdminLayout = () => {
  const [open, setOpen] = useState(false)

  useRouteTitle()

  return (
    <div className="flex min-h-dvh w-full bg-background text-foreground">
      <SkipLink />
      <ScrollToTop />
      <AdminSidebar />
      <main id={MAIN_CONTENT_ID} tabIndex={-1} className="min-w-0 flex-1 overflow-x-auto focus:outline-none">
        <AdminHeaderMobile onOpenMenu={() => setOpen(true)} />
        <Outlet />
      </main>
      <AdminMenuMobile open={open} onClose={() => setOpen(false)} />
    </div>
  )
}
