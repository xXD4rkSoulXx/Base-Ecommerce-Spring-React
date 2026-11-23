import type { FieldErrors, UseFormRegister } from 'react-hook-form'
import { Field } from '@/components/ui/Field'
import { FormError } from '@/components/ui/FormError'
import { SHIPPING_COUNTRY_CODES } from '@/constants'
import type { CheckoutAddressForm } from '@/schemas/checkout'
import { formatCountry } from '@/utils/format'
import { getDialCode, onlyDigits } from '@/utils/phone'

interface CheckoutAddressSectionProps {
  register: UseFormRegister<CheckoutAddressForm>
  errors: FieldErrors<CheckoutAddressForm>
  error: string | null
  onCountryChange: (country: string) => void
}

const COUNTRY_OPTIONS = SHIPPING_COUNTRY_CODES.map((code) => ({
  code,
  name: formatCountry(code),
  dialCode: getDialCode(code)
})).sort((a, b) => a.name.localeCompare(b.name))

export const CheckoutAddressSection = ({ register, errors, error, onCountryChange }: CheckoutAddressSectionProps) => {
  return (
    <>
      <Field
        label="Full Name"
        autoComplete="name"
        error={errors.recipientName?.message}
        {...register('recipientName')}
      />
      <div className="grid gap-6 md:grid-cols-[1fr_2fr]">
        <label className="block">
          <span className="block font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">
            Dial Code
          </span>
          <select
            className="mt-2 w-full border border-border bg-card px-4 py-3 text-sm transition-colors focus:border-neon-pink focus:outline-none"
            {...register('phoneCountry')}
          >
            {COUNTRY_OPTIONS.map((country) => (
              <option key={country.code} value={country.code}>
                {country.name} (+{country.dialCode})
              </option>
            ))}
          </select>
        </label>
        <Field
          label="Phone"
          type="tel"
          inputMode="numeric"
          maxLength={15}
          autoComplete="tel-national"
          error={errors.phone?.message}
          {...register('phone')}
          onChange={(event) => {
            event.target.value = onlyDigits(event.target.value)

            return register('phone').onChange(event)
          }}
        />
      </div>
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
            {...register('country', { onChange: (event) => onCountryChange(event.target.value) })}
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
}
