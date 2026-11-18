import { AdminBrand } from '@/components/layout/admin/AdminBrand'
import { AdminLeaveButton } from '@/components/layout/admin/AdminLeaveButton'
import { AdminNavLinks } from '@/components/layout/admin/AdminNavLinks'

export const AdminSidebar = () => (
  <aside className="sticky top-0 hidden h-dvh w-64 shrink-0 border-r border-border bg-card lg:block">
    <div className="border-b border-border p-6">
      <AdminBrand />
    </div>
    <AdminNavLinks className="p-3" />
    <AdminLeaveButton />
  </aside>
)
