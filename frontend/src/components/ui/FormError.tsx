import { AlertTriangle } from 'lucide-react'

interface FormErrorProps {
  message: string
}

export const FormError = ({ message }: FormErrorProps) => (
  <div
    role="alert"
    className="flex items-start gap-3 border border-destructive/60 bg-destructive/10 px-4 py-3 text-xs text-destructive"
  >
    <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0" />
    <p className="font-mono">{message}</p>
  </div>
)
