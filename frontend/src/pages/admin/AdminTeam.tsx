import { useState } from 'react'
import { UserPlus } from 'lucide-react'
import { AdminAssignRoleDialog } from '@/components/dialog/admin/AdminAssignRoleDialog'
import { FormError } from '@/components/ui/FormError'
import { TeamMemberRow } from '@/components/ui/admin/AdminTeamMemberRow'
import { usePermission } from '@/hooks/usePermission'
import { useAdminTeam } from '@/hooks/admin/useAdminTeam'
import { useAuthStore } from '@/store/useAuthStore'
import type { AdminTeamMemberResponse } from '@/types/admin/adminTeam'

export const AdminTeam = () => {
  const { data: team, isLoading, isError } = useAdminTeam()
  const canEdit = usePermission('TEAM_WRITE')
  const currentEmail = useAuthStore((state) => state.user?.email)
  const [dialogOpen, setDialogOpen] = useState(false)
  const [selected, setSelected] = useState<AdminTeamMemberResponse | null>(null)

  const openDialog = (member: AdminTeamMemberResponse | null) => {
    setSelected(member)
    setDialogOpen(true)
  }

  return (
    <div className="p-6 md:p-10">
      <div className="mb-8 flex flex-wrap items-end justify-between gap-4">
        <h1 className="font-display text-5xl uppercase tracking-tighter md:text-7xl">
          {team ? `Team (${team.length})` : 'Team'}
        </h1>
        {canEdit && (
          <button
            onClick={() => openDialog(null)}
            className="flex items-center gap-2 bg-neon-cyan px-5 py-3 text-xs font-black uppercase tracking-widest text-primary-foreground hover:opacity-80"
          >
            <UserPlus className="h-4 w-4" /> Add Team Member
          </button>
        )}
      </div>
      {isLoading && (
        <p className="font-mono text-xs uppercase tracking-widest text-muted-foreground">Loading...</p>
      )}
      {isError && <FormError message="Could not load the team" />}
      {team && (
        <div className="overflow-x-auto border border-border bg-card">
          <table className="w-full min-w-[700px] text-sm">
            <thead>
              <tr className="border-b border-border text-left font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
                <th className="p-4">Name</th>
                <th className="p-4">Email</th>
                <th className="p-4">Role</th>
                <th className="p-4">Joined</th>
                <th className="p-4 text-right">Action</th>
              </tr>
            </thead>
            <tbody>
              {team.map((member) => (
                <TeamMemberRow
                  key={member.id}
                  member={member}
                  isSelf={member.email === currentEmail}
                  canEdit={canEdit}
                  onChangeRole={openDialog}
                />
              ))}
            </tbody>
          </table>
        </div>
      )}
      <AdminAssignRoleDialog
        key={`${selected?.id ?? 'new'}-${selected?.role}`}
        open={dialogOpen}
        onOpenChange={setDialogOpen}
        member={selected}
      />
    </div>
  )
}
