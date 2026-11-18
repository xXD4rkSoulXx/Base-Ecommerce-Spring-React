import { useState } from 'react'
import { UserPlus } from 'lucide-react'
import { AdminAssignRoleDialog } from '@/components/dialog/admin/AdminAssignRoleDialog'
import { AdminTeamMemberRow } from '@/components/row/admin/AdminTeamMemberRow'
import { AdminPageTitle } from '@/components/ui/admin/AdminPageTitle'
import { AdminTable, type AdminTableColumn } from '@/components/ui/admin/AdminTable'
import { FormError } from '@/components/ui/FormError'
import { useAdminTeam } from '@/hooks/admin/useAdminTeam'
import { usePermission } from '@/hooks/usePermission'
import { useAuthStore } from '@/store/useAuthStore'
import type { AdminTeamMemberResponse } from '@/types/admin/adminTeam'

const COLUMNS: AdminTableColumn[] = [
  { label: 'Name' },
  { label: 'Email' },
  { label: 'Role' },
  { label: 'Joined' },
  { label: 'Action', alignRight: true }
]

export const AdminTeam = () => {
  const { data: team, isLoading, isError } = useAdminTeam()
  const canEdit = usePermission('TEAM_WRITE')
  const currentEmail = useAuthStore((state) => state.user?.email)
  const [dialog, setDialog] = useState<{ member: AdminTeamMemberResponse | null } | null>(null)

  return (
    <div className="p-6 md:p-10">
      <AdminPageTitle
        count={team?.length}
        action={
          canEdit && (
            <button
              type="button"
              onClick={() => setDialog({ member: null })}
              className="flex items-center gap-2 bg-neon-cyan px-5 py-3 text-xs font-black uppercase tracking-widest text-primary-foreground hover:opacity-80"
            >
              <UserPlus className="h-4 w-4" /> Add Team Member
            </button>
          )
        }
      >
        Team
      </AdminPageTitle>
      {isLoading && <p className="font-mono text-xs uppercase tracking-widest text-muted-foreground">Loading...</p>}
      {isError && <FormError message="Could not load the team" />}
      {team && (
        <AdminTable
          columns={COLUMNS}
          minWidthClassName="min-w-[700px]"
          empty={team.length === 0}
          emptyMessage="No team members"
        >
          {team.map((member) => (
            <AdminTeamMemberRow
              key={member.id}
              member={member}
              isSelf={member.email === currentEmail}
              canEdit={canEdit}
              onChangeRole={(selected) => setDialog({ member: selected })}
            />
          ))}
        </AdminTable>
      )}
      {dialog && <AdminAssignRoleDialog member={dialog.member} onClose={() => setDialog(null)} />}
    </div>
  )
}
