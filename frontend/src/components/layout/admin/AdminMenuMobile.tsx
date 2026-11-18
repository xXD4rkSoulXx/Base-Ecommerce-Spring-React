import { AdminBrand } from '@/components/layout/admin/AdminBrand'
import { AdminLeaveButton } from '@/components/layout/admin/AdminLeaveButton'
import { AdminNavLinks } from '@/components/layout/admin/AdminNavLinks'
import { Drawer } from '@/components/ui/Drawer'

interface AdminMenuMobileProps {
  open: boolean
  onClose: () => void
}

export const AdminMenuMobile = ({ open, onClose }: AdminMenuMobileProps) => (
  <Drawer open={open} onClose={onClose} label="Admin menu" hideFrom="lg">
    <AdminBrand onNavigate={onClose} />
    <AdminNavLinks onNavigate={onClose} className="mt-8" />
    <AdminLeaveButton onNavigate={onClose} />
  </Drawer>
)
