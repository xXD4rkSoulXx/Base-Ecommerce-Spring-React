interface FieldErrorProps {
  message?: string
  id?: string
}

export const FieldError = ({ message, id }: FieldErrorProps) =>
  message ? (
    <p id={id} className="mt-2 font-mono text-[10px] text-destructive">
      {message}
    </p>
  ) : null
