import { useState } from 'react'
import { Outlet } from 'react-router-dom'
import { ScrollToTop } from '@/components/layout/ScrollToTop'
import { AdminSidebar } from '@/components/layout/admin/AdminSideBar'
import { AdminHeaderMobile } from '@/components/layout/admin/AdminHeaderMobile'
import { AdminMenuMobile } from '@/components/layout/admin/AdminMenuMobile'

export const AdminLayout = () => {
  const [open, setOpen] = useState(false)

  return (
    <div className="flex min-h-dvh w-full bg-background text-foreground">
      <ScrollToTop />
      <AdminSidebar />
      <main className="min-w-0 flex-1 overflow-x-auto">
        <AdminHeaderMobile onOpenMenu={() => setOpen(true)} />
        <Outlet />
      </main>
      <AdminMenuMobile open={open} onClose={() => setOpen(false)} />
    </div>
  )
}
