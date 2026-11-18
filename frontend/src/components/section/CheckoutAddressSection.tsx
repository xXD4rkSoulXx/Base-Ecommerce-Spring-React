import type { ChangeEvent } from 'react'
import { Field } from '@/components/ui/Field'
import { FormError } from '@/components/ui/FormError'
import { SHIPPING_COUNTRY_CODES } from '@/constants'
import type { CheckoutAddressForm } from '@/types/checkout'
import { formatCountry } from '@/utils/format'

interface CheckoutAddressSectionProps {
  address: CheckoutAddressForm
  error: string | null
  onChange: (event: ChangeEvent<HTMLInputElement | HTMLSelectElement>) => void
}

const COUNTRY_OPTIONS = SHIPPING_COUNTRY_CODES.map((code) => ({ code, name: formatCountry(code) })).sort((a, b) =>
  a.name.localeCompare(b.name)
)

export const CheckoutAddressSection = ({ address, error, onChange }: CheckoutAddressSectionProps) => (
  <>
    <Field
      label="Full Name"
      name="recipientName"
      value={address.recipientName}
      onChange={onChange}
      autoComplete="name"
      maxLength={100}
      required
    />
    <Field
      label="Phone"
      name="phone"
      type="tel"
      value={address.phone}
      onChange={onChange}
      autoComplete="tel"
      maxLength={30}
      required
    />
    <Field
      label="Address Line 1"
      name="addressLine1"
      value={address.addressLine1}
      onChange={onChange}
      autoComplete="address-line1"
      maxLength={100}
      required
    />
    <Field
      label="Address Line 2 (optional)"
      name="addressLine2"
      value={address.addressLine2}
      onChange={onChange}
      autoComplete="address-line2"
      maxLength={100}
    />
    <div className="grid gap-6 md:grid-cols-2">
      <Field
        label="City"
        name="city"
        value={address.city}
        onChange={onChange}
        autoComplete="address-level2"
        maxLength={100}
        required
      />
      <Field
        label="Postal Code"
        name="postalCode"
        value={address.postalCode}
        onChange={onChange}
        autoComplete="postal-code"
        maxLength={20}
        required
      />
    </div>
    <div className="grid gap-6 md:grid-cols-2">
      <Field
        label="State / Region (optional)"
        name="state"
        value={address.state}
        onChange={onChange}
        autoComplete="address-level1"
        maxLength={100}
      />
      <label className="block">
        <span className="block font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">Country</span>
        <select
          name="country"
          value={address.country}
          onChange={onChange}
          autoComplete="country"
          className="mt-2 w-full border border-border bg-card px-4 py-3 text-sm transition-colors focus:border-neon-pink focus:outline-none"
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
