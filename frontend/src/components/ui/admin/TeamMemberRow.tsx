import { Shield } from 'lucide-react'
import { RoleBadge } from '@/components/ui/RoleBadge'
import type { TeamMemberResponse } from '@/types/admin/team'

interface TeamMemberRowProps {
  member: TeamMemberResponse
  isSelf: boolean
  canEdit: boolean
  onChangeRole: (member: TeamMemberResponse) => void
}

const dateFormatter = new Intl.DateTimeFormat('pt-PT', { dateStyle: 'medium' })

export const TeamMemberRow = ({ member, isSelf, canEdit, onChangeRole }: TeamMemberRowProps) => (
  <tr className="border-b border-border hover:bg-secondary/30">
    <td className="p-4 font-bold">
      {member.firstName} {member.lastName}
    </td>
    <td className="p-4 text-muted-foreground">{member.email}</td>
    <td className="p-4">
      <RoleBadge role={member.role} />
    </td>
    <td className="p-4 font-mono text-muted-foreground">{dateFormatter.format(new Date(member.createdAt))}</td>
    <td className="p-4 text-right">
      {isSelf && (
        <span className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">You</span>
      )}
      {!isSelf && canEdit && (
        <button
          onClick={() => onChangeRole(member)}
          className="inline-flex items-center gap-2 border border-neon-cyan/40 px-3 py-2 text-[10px] font-bold uppercase tracking-widest text-neon-cyan hover:bg-neon-cyan hover:text-primary-foreground"
        >
          <Shield className="h-3 w-3" /> Role
        </button>
      )}
    </td>
  </tr>
)
