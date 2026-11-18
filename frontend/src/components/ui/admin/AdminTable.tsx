import type { ReactNode } from 'react'

export interface AdminTableColumn {
  label: string
  alignRight?: boolean
}

interface AdminTableProps {
  columns: AdminTableColumn[]
  minWidthClassName: string
  empty: boolean
  emptyMessage: string
  children: ReactNode
}

export const AdminTable = ({ columns, minWidthClassName, empty, emptyMessage, children }: AdminTableProps) => (
  <div className="overflow-x-auto border border-border bg-card">
    <table className={`w-full text-sm ${minWidthClassName}`}>
      <thead>
        <tr className="border-b border-border text-left font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
          {columns.map((column) => (
            <th key={column.label} className={column.alignRight ? 'p-4 text-right' : 'p-4'}>
              {column.label}
            </th>
          ))}
        </tr>
      </thead>
      <tbody>
        {empty ? (
          <tr>
            <td
              colSpan={columns.length}
              className="p-10 text-center font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground"
            >
              {emptyMessage}
            </td>
          </tr>
        ) : (
          children
        )}
      </tbody>
    </table>
  </div>
)
