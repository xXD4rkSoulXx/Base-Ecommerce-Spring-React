import { AUDIT_ACTION_LABELS, AUDIT_ENTITY_TYPE_LABELS } from '@/types/admin/adminAuditLog'
import type { AdminAuditLogResponse } from '@/types/admin/adminAuditLog'

interface AdminDashboardActivityProps {
  activity: AdminAuditLogResponse[]
}

const dateFormatter = new Intl.DateTimeFormat('pt-PT', { dateStyle: 'short', timeStyle: 'short' })

export const AdminDashboardActivity = ({ activity }: AdminDashboardActivityProps) => (
  <ul>
    {activity.map((entry) => (
      <li key={entry.id} className="border-b border-border p-4 last:border-b-0">
        <p className="text-sm">
          <span className="font-bold">{AUDIT_ACTION_LABELS[entry.action]}</span>{' '}
          <span className="text-muted-foreground">
            {AUDIT_ENTITY_TYPE_LABELS[entry.entityType].toLowerCase()}
          </span>{' '}
          {entry.entityLabel}
        </p>
        <p className="font-mono text-[10px] text-muted-foreground">
          {entry.actorEmail} · {dateFormatter.format(new Date(entry.createdAt))}
        </p>
      </li>
    ))}
  </ul>
)
