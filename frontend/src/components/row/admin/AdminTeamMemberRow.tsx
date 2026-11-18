import { Shield } from 'lucide-react'
import { RoleBadge } from '@/components/badge/RoleBadge'
import { formatDate } from '@/utils/format'
import type { AdminTeamMemberResponse } from '@/types/admin/adminTeam'

interface AdminTeamMemberRowProps {
  member: AdminTeamMemberResponse
  isSelf: boolean
  canEdit: boolean
  onChangeRole: (member: AdminTeamMemberResponse) => void
}

export const AdminTeamMemberRow = ({ member, isSelf, canEdit, onChangeRole }: AdminTeamMemberRowProps) => (
  <tr className="border-b border-border hover:bg-secondary/30">
    <td className="p-4 font-bold">
      {member.firstName} {member.lastName}
    </td>
    <td className="p-4 text-muted-foreground">{member.email}</td>
    <td className="p-4">
      <RoleBadge role={member.role} />
    </td>
    <td className="p-4 font-mono text-muted-foreground">{formatDate(member.createdAt)}</td>
    <td className="p-4 text-right">
      {isSelf && <span className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">You</span>}
      {!isSelf && canEdit && (
        <button
          type="button"
          onClick={() => onChangeRole(member)}
          className="inline-flex items-center gap-2 border border-neon-cyan/40 px-3 py-2 text-[10px] font-bold uppercase tracking-widest text-neon-cyan hover:bg-neon-cyan hover:text-primary-foreground"
        >
          <Shield className="h-3 w-3" /> Role
        </button>
      )}
    </td>
  </tr>
)
