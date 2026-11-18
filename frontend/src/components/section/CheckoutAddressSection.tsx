import type { FieldErrors, UseFormRegister } from 'react-hook-form'
import { Field } from '@/components/ui/Field'
import { FormError } from '@/components/ui/FormError'
import { SHIPPING_COUNTRY_CODES } from '@/constants'
import type { CheckoutAddressForm } from '@/schemas/checkout'
import { formatCountry } from '@/utils/format'

interface CheckoutAddressSectionProps {
  register: UseFormRegister<CheckoutAddressForm>
  errors: FieldErrors<CheckoutAddressForm>
  error: string | null
}

const COUNTRY_OPTIONS = SHIPPING_COUNTRY_CODES.map((code) => ({ code, name: formatCountry(code) })).sort((a, b) =>
  a.name.localeCompare(b.name)
)

export const CheckoutAddressSection = ({ register, errors, error }: CheckoutAddressSectionProps) => (
  <>
    <Field label="Full Name" autoComplete="name" error={errors.recipientName?.message} {...register('recipientName')} />
    <Field label="Phone" type="tel" autoComplete="tel" error={errors.phone?.message} {...register('phone')} />
    <Field
      label="Address Line 1"
      autoComplete="address-line1"
      error={errors.addressLine1?.message}
      {...register('addressLine1')}
    />
    <Field
      label="Address Line 2 (optional)"
      autoComplete="address-line2"
      error={errors.addressLine2?.message}
      {...register('addressLine2')}
    />
    <div className="grid gap-6 md:grid-cols-2">
      <Field label="City" autoComplete="address-level2" error={errors.city?.message} {...register('city')} />
      <Field
        label="Postal Code"
        autoComplete="postal-code"
        error={errors.postalCode?.message}
        {...register('postalCode')}
      />
    </div>
    <div className="grid gap-6 md:grid-cols-2">
      <Field
        label="State / Region (optional)"
        autoComplete="address-level1"
        error={errors.state?.message}
        {...register('state')}
      />
      <label className="block">
        <span className="block font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">Country</span>
        <select
          autoComplete="country"
          className="mt-2 w-full border border-border bg-card px-4 py-3 text-sm transition-colors focus:border-neon-pink focus:outline-none"
          {...register('country')}
        >
          {COUNTRY_OPTIONS.map((country) => (
            <option key={country.code} value={country.code}>
              {country.name}
            </option>
          ))}
        </select>
      </label>
    </div>
    {error && <FormError message={error} />}
  </>
)
