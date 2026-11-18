import { useState, type SubmitEvent } from 'react'
import { FormError } from '@/components/ui/FormError'

interface PromoCodeFormProps {
  onApply: (code: string) => void
  error: string | null
}

export const PromoCodeForm = ({ onApply, error }: PromoCodeFormProps) => {
  const [code, setCode] = useState('')

  const submit = (event: SubmitEvent<HTMLFormElement>) => {
    event.preventDefault()

    const trimmedCode = code.trim()

    if (!trimmedCode) {
      return
    }

    onApply(trimmedCode)
    setCode('')
  }

  return (
    <form onSubmit={submit}>
      <p className="mb-2 font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">Promo Code</p>
      <div className="flex gap-2">
        <input
          type="text"
          value={code}
          onChange={(event) => setCode(event.target.value)}
          aria-label="Promo code"
          placeholder="Code"
          className="flex-1 border border-border bg-background px-3 py-2 font-mono text-xs uppercase tracking-widest focus:border-neon-cyan focus:outline-none"
        />
        <button
          type="submit"
          className="bg-neon-pink px-4 py-2 text-xs font-black uppercase tracking-widest text-primary-foreground hover:opacity-80"
        >
          Apply
        </button>
      </div>
      {error && (
        <div className="mt-2">
          <FormError message={error} />
        </div>
      )}
    </form>
  )
}
