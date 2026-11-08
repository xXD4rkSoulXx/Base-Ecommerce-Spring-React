import { AUDIT_ACTION_LABELS, AUDIT_ENTITY_TYPE_LABELS, type AdminAuditLogResponse, type AuditAction } from '@/types/admin/adminAuditLog'

interface AdminAuditLogRowProps {
  auditLog: AdminAuditLogResponse
}

const dateTimeFormatter = new Intl.DateTimeFormat('pt-PT', { dateStyle: 'medium', timeStyle: 'short' })

const DESTRUCTIVE_ACTIONS: AuditAction[] = [
  'ARCHIVED',
  'DEACTIVATED',
  'HIDDEN',
  'IMAGE_DELETED',
  'REJECTED',
  'REFUNDED'
]

export const AdminAuditLogRow = ({ auditLog }: AdminAuditLogRowProps) => (
  <tr className="border-b border-border align-top last:border-b-0">
    <td className="whitespace-nowrap p-4 font-mono text-xs text-muted-foreground">
      {dateTimeFormatter.format(new Date(auditLog.createdAt))}
    </td>
    <td className="p-4 font-mono text-xs">{auditLog.actorEmail}</td>
    <td className="p-4">
      <span
        className={`inline-block whitespace-nowrap border px-2 py-0.5 font-mono text-[10px] uppercase tracking-widest ${
          DESTRUCTIVE_ACTIONS.includes(auditLog.action)
            ? 'border-neon-pink text-neon-pink'
            : 'border-neon-cyan text-neon-cyan'
        }`}
      >
        {AUDIT_ACTION_LABELS[auditLog.action]}
      </span>
    </td>
    <td className="p-4">
      <p className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
        {AUDIT_ENTITY_TYPE_LABELS[auditLog.entityType]}
      </p>
      <p className="text-sm font-bold">{auditLog.entityLabel}</p>
    </td>
    <td className="p-4 text-xs text-muted-foreground">{auditLog.details ?? '—'}</td>
  </tr>
)
