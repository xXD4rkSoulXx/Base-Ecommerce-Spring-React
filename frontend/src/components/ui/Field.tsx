interface FieldProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label: string;
  icon: React.ReactNode;
}

function Field({ label, icon, type, placeholder, ...rest }: FieldProps) {
  return (
    <div>
      <label className="font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">
        {label}
      </label>
      <div className="mt-2 flex items-center gap-3 border border-border bg-card px-4 py-3 transition-colors focus-within:border-neon-pink">
        <span className="text-muted-foreground">{icon}</span>
        <input
          type={type}
          placeholder={placeholder}
          className="w-full bg-transparent text-sm text-foreground placeholder:text-muted-foreground focus:outline-none"
          {...rest}
        />
      </div>
    </div>
  )
}

export default Field
