import { useState, type SubmitEvent } from 'react'
import { useNavigate } from 'react-router-dom'
import { isAxiosError } from 'axios'
import { Check } from 'lucide-react'
import { Field } from '@/components/ui/Field'
import { Row } from '@/components/ui/Row'
import { useCart } from '@/hooks/useCart'
import { useCheckout } from '@/hooks/useCheckout'
import type { CheckoutAddressRequest } from '@/types/checkout'

type Step = 1 | 2 | 3
type AddressForm = Record<keyof CheckoutAddressRequest, string>

const STEPS = ['Address', 'Shipping', 'Review']

const COUNTRIES = [
  { code: 'AO', name: 'Angola' },
  { code: 'AR', name: 'Argentina' },
  { code: 'AU', name: 'Australia' },
  { code: 'AT', name: 'Austria' },
  { code: 'BE', name: 'Belgium' },
  { code: 'BR', name: 'Brazil' },
  { code: 'CA', name: 'Canada' },
  { code: 'CV', name: 'Cape Verde' },
  { code: 'CL', name: 'Chile' },
  { code: 'CN', name: 'China' },
  { code: 'CO', name: 'Colombia' },
  { code: 'CR', name: 'Costa Rica' },
  { code: 'CU', name: 'Cuba' },
  { code: 'CZ', name: 'Czech Republic' },
  { code: 'DK', name: 'Denmark' },
  { code: 'EC', name: 'Ecuador' },
  { code: 'EG', name: 'Egypt' },
  { code: 'FI', name: 'Finland' },
  { code: 'FR', name: 'France' },
  { code: 'DE', name: 'Germany' },
  { code: 'GR', name: 'Greece' },
  { code: 'HU', name: 'Hungary' },
  { code: 'IN', name: 'India' },
  { code: 'ID', name: 'Indonesia' },
  { code: 'IE', name: 'Ireland' },
  { code: 'IL', name: 'Israel' },
  { code: 'IT', name: 'Italy' },
  { code: 'JP', name: 'Japan' },
  { code: 'KE', name: 'Kenya' },
  { code: 'MY', name: 'Malaysia' },
  { code: 'MX', name: 'Mexico' },
  { code: 'MA', name: 'Morocco' },
  { code: 'MZ', name: 'Mozambique' },
  { code: 'NL', name: 'Netherlands' },
  { code: 'NZ', name: 'New Zealand' },
  { code: 'NG', name: 'Nigeria' },
  { code: 'NO', name: 'Norway' },
  { code: 'PA', name: 'Panama' },
  { code: 'PE', name: 'Peru' },
  { code: 'PH', name: 'Philippines' },
  { code: 'PL', name: 'Poland' },
  { code: 'PT', name: 'Portugal' },
  { code: 'QA', name: 'Qatar' },
  { code: 'RO', name: 'Romania' },
  { code: 'RU', name: 'Russia' },
  { code: 'SA', name: 'Saudi Arabia' },
  { code: 'SG', name: 'Singapore' },
  { code: 'ZA', name: 'South Africa' },
  { code: 'KR', name: 'South Korea' },
  { code: 'ES', name: 'Spain' },
  { code: 'SE', name: 'Sweden' },
  { code: 'CH', name: 'Switzerland' },
  { code: 'TH', name: 'Thailand' },
  { code: 'TR', name: 'Turkey' },
  { code: 'UA', name: 'Ukraine' },
  { code: 'AE', name: 'United Arab Emirates' },
  { code: 'GB', name: 'United Kingdom' },
  { code: 'US', name: 'United States' },
  { code: 'UY', name: 'Uruguay' },
  { code: 'VE', name: 'Venezuela' },
  { code: 'VN', name: 'Vietnam' }
]

const EMPTY_ADDRESS: AddressForm = {
  recipientName: '',
  addressLine1: '',
  addressLine2: '',
  city: '',
  state: '',
  postalCode: '',
  country: 'PT',
  phone: ''
}

const priceFormatter = new Intl.NumberFormat('pt-PT', { style: 'currency', currency: 'EUR' })
const formatPrice = (amount: number) => priceFormatter.format(amount)

const getErrorMessage = (error: unknown, fallback: string) =>
  (isAxiosError<{ message?: string }>(error) && error.response?.data?.message) || fallback

export const Checkout = () => {
  const navigate = useNavigate()
  const {
    items,
    subtotal,
    discountAmount,
    discountCode,
    isDiscountCodeError,
    discountCodeError,
    applyDiscountCode,
    getLineTotal,
    removeDiscountCode
  } = useCart()
  const {
    shippingRates,
    isLoadingShippingRates,
    shippingRatesError,
    isCreatingSession,
    createSessionError,
    createSession,
    getShippingRates
  } = useCheckout()

  const [step, setStep] = useState<Step>(1)
  const [address, setAddress] = useState<AddressForm>(EMPTY_ADDRESS)
  const [selectedRateId, setSelectedRateId] = useState('')
  const [codeInput, setCodeInput] = useState('')

  const selectedRate = shippingRates.find((rate) => rate.rateId === selectedRateId)
  const total = subtotal - discountAmount + (selectedRate?.amount ?? 0)
  const hasUnavailableItems = items.some((item) => item.available === false)

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setAddress((current) => ({ ...current, [e.target.name]: e.target.value }))
  }

  const buildAddress = (): CheckoutAddressRequest => ({
    recipientName: address.recipientName.trim(),
    addressLine1: address.addressLine1.trim(),
    addressLine2: address.addressLine2.trim() || undefined,
    city: address.city.trim(),
    state: address.state.trim() || undefined,
    postalCode: address.postalCode.trim(),
    country: address.country,
    phone: address.phone.trim()
  })

  const handleSubmit = (e: SubmitEvent<HTMLFormElement>) => {
    e.preventDefault()

    if (step === 1) {
      getShippingRates(buildAddress(), {
        onSuccess: (rates) => {
          setSelectedRateId(rates[0]?.rateId ?? '')
          setStep(2)
        }
      })

      return
    }

    if (step === 2) {
      setStep(3)

      return
    }

    createSession({ address: buildAddress(), rateId: selectedRateId })
  }

  const handleApplyCode = (e: SubmitEvent<HTMLFormElement>) => {
    e.preventDefault()

    if (!codeInput.trim()) {
      return
    }

    applyDiscountCode(codeInput.trim())
    setCodeInput('')
  }

  if (items.length === 0) {
    return (
      <div className="mx-auto max-w-2xl px-6 py-32 text-center">
        <h1 className="font-display text-3xl uppercase">Cart empty</h1>
        <button
          onClick={() => navigate('/shop')}
          className="mt-8 bg-neon-cyan px-8 py-4 text-xs font-black uppercase tracking-widest text-primary-foreground"
        >
          Enter Archive
        </button>
      </div>
    )
  }

  if (hasUnavailableItems) {
    return (
      <div className="mx-auto max-w-2xl px-6 py-32 text-center">
        <h1 className="font-display text-3xl uppercase">Items unavailable</h1>
        <p className="mt-4 text-muted-foreground">
          Some items in your cart are no longer available. Remove them to continue.
        </p>
        <button
          onClick={() => navigate('/cart')}
          className="mt-8 bg-neon-cyan px-8 py-4 text-xs font-black uppercase tracking-widest text-primary-foreground"
        >
          Back to cart
        </button>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-[1440px] px-6 py-16 md:px-10">
      <h1 className="mt-4 font-display text-5xl uppercase tracking-tighter md:text-6xl">
        Initiate <span className="text-neon-cyan">Transit</span>
      </h1>
      <div className="mt-10 flex items-center gap-4 border-b border-border pb-6">
        {STEPS.map((label, i) => {
          const n = (i + 1) as Step
          const active = step === n
          const done = step > n

          return (
            <div key={label} className="flex items-center gap-3">
              <div
                className={`flex h-8 w-8 items-center justify-center border font-mono text-xs font-bold ${
                  active
                    ? 'border-neon-cyan bg-neon-cyan text-primary-foreground'
                    : done
                      ? 'border-neon-cyan text-neon-cyan'
                      : 'border-border text-muted-foreground'
                }`}
              >
                {done ? <Check className="h-4 w-4" /> : n}
              </div>
              <span
                className={`text-xs font-bold uppercase tracking-widest ${
                  active ? 'text-foreground' : 'text-muted-foreground'
                }`}
              >
                {label}
              </span>
              {i < STEPS.length - 1 && <span className="ml-2 h-px w-8 bg-border" />}
            </div>
          )
        })}
      </div>
      <div className="mt-12 grid gap-12 lg:grid-cols-[1fr_400px]">
        <form onSubmit={handleSubmit} className="space-y-6">
          {step === 1 && (
            <>
              <Field label="Full Name" name="recipientName" value={address.recipientName} onChange={handleChange} required />
              <Field label="Phone" name="phone" type="tel" value={address.phone} onChange={handleChange} required />
              <Field label="Address Line 1" name="addressLine1" value={address.addressLine1} onChange={handleChange} required />
              <Field label="Address Line 2 (optional)" name="addressLine2" value={address.addressLine2} onChange={handleChange} />
              <div className="grid gap-6 md:grid-cols-2">
                <Field label="City" name="city" value={address.city} onChange={handleChange} required />
                <Field label="Postal Code" name="postalCode" value={address.postalCode} onChange={handleChange} required />
              </div>
              <div className="grid gap-6 md:grid-cols-2">
                <Field label="State / Region (optional)" name="state" value={address.state} onChange={handleChange} />
                <div>
                  <label className="font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">
                    Country
                  </label>
                  <select
                    name="country"
                    value={address.country}
                    onChange={handleChange}
                    className="mt-2 w-full border border-border bg-card px-4 py-3 text-sm transition-colors focus:border-neon-pink focus:outline-none"
                  >
                    {COUNTRIES.map((country) => (
                      <option key={country.code} value={country.code}>
                        {country.name}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
              {shippingRatesError && (
                <p className="font-mono text-xs text-destructive">
                  {getErrorMessage(shippingRatesError, 'Could not load shipping rates')}
                </p>
              )}
            </>
          )}
          {step === 2 && (
            <div className="space-y-3">
              <p className="font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">
                Shipping method
              </p>
              {shippingRates.map((rate) => {
                const isSelected = rate.rateId === selectedRateId

                return (
                  <label
                    key={rate.rateId}
                    className={`flex cursor-pointer items-center justify-between gap-4 border p-4 transition-colors ${
                      isSelected ? 'border-neon-cyan bg-card' : 'border-border hover:border-foreground'
                    }`}
                  >
                    <div className="flex items-center gap-4">
                      <input
                        type="radio"
                        name="rate"
                        value={rate.rateId}
                        checked={isSelected}
                        onChange={() => setSelectedRateId(rate.rateId)}
                        className="accent-neon-cyan"
                      />
                      <div>
                        <p className="text-sm font-bold uppercase">
                          {rate.carrier} · {rate.service}
                        </p>
                        <p className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
                          Delivery: {rate.estimatedDays}
                        </p>
                      </div>
                    </div>
                    <span className="font-mono text-sm text-neon-cyan">{formatPrice(rate.amount)}</span>
                  </label>
                )
              })}
            </div>
          )}
          {step === 3 && selectedRate && (
            <div className="space-y-4">
              <div className="border border-border bg-card p-4">
                <p className="font-mono text-[10px] uppercase tracking-widest text-neon-cyan">Ship to</p>
                <p className="mt-2 text-sm font-bold">{address.recipientName}</p>
                <p className="text-sm text-muted-foreground">
                  {address.addressLine1}
                  {address.addressLine2 && `, ${address.addressLine2}`}
                </p>
                <p className="text-sm text-muted-foreground">
                  {address.postalCode} {address.city}
                  {address.state && `, ${address.state}`}
                  {' · '}
                  {COUNTRIES.find((country) => country.code === address.country)?.name}
                </p>
                <p className="text-sm text-muted-foreground">{address.phone}</p>
              </div>
              <div className="border border-border bg-card p-4">
                <p className="font-mono text-[10px] uppercase tracking-widest text-neon-cyan">Shipping method</p>
                <p className="mt-2 text-sm">
                  {selectedRate.carrier} · {selectedRate.service} · {formatPrice(selectedRate.amount)}
                </p>
              </div>
              <p className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground">
                You will complete the payment securely on Stripe.
              </p>
              {createSessionError && (
                <p className="font-mono text-xs text-destructive">
                  {getErrorMessage(createSessionError, 'Could not start the payment')}
                </p>
              )}
            </div>
          )}
          <div className="flex gap-3 pt-4">
            {step > 1 && (
              <button
                type="button"
                onClick={() => setStep((step - 1) as Step)}
                disabled={isCreatingSession}
                className="border border-border px-8 py-4 text-xs font-bold uppercase tracking-widest hover:border-foreground disabled:opacity-40"
              >
                ← Back
              </button>
            )}
            <button
              type="submit"
              disabled={isLoadingShippingRates || isCreatingSession || (step === 2 && !selectedRateId)}
              className="flex-1 bg-neon-cyan px-8 py-4 text-xs font-black uppercase tracking-widest text-primary-foreground transition-opacity hover:opacity-80 disabled:cursor-not-allowed disabled:opacity-40"
            >
              {step === 1 && (isLoadingShippingRates ? 'Finding rates...' : 'Continue →')}
              {step === 2 && 'Continue →'}
              {step === 3 && (isCreatingSession ? 'Redirecting...' : 'Pay with Stripe →')}
            </button>
          </div>
        </form>
        <aside className="h-fit border border-border bg-card p-6">
          <h2 className="mb-4 font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">
            Order Manifest
          </h2>
          <div className="space-y-3 border-b border-border pb-4">
            {items.map((item) => (
              <div key={item.productSkuId} className="flex gap-3 text-sm">
                <div className="h-14 w-14 shrink-0 overflow-hidden border border-border">
                  <img src={item.cover} alt={item.productName} className="h-full w-full object-cover" />
                </div>
                <div className="flex-1">
                  <p className="text-xs font-bold uppercase">{item.productName}</p>
                  <p className="font-mono text-[10px] text-muted-foreground">
                    {item.size.startsWith('_') ? item.size.substring(1) : item.size} · Qty {item.quantity}
                  </p>
                </div>
                <span className="font-mono text-xs text-neon-cyan">{formatPrice(getLineTotal(item))}</span>
              </div>
            ))}
          </div>
          <div className="mt-4 space-y-2 text-sm">
            <Row label="Subtotal" value={formatPrice(subtotal)} />
            {discountCode && <Row label={`Discount (${discountCode})`} value={`-${formatPrice(discountAmount)}`} />}
            <Row label="Shipping" value={selectedRate ? formatPrice(selectedRate.amount) : 'Next step'} />
            <Row label="Taxes" value="At payment" />
            {discountCode ? (
              <div className="mt-4 flex items-center justify-between border border-border px-3 py-2">
                <span className="font-mono text-xs uppercase tracking-widest text-neon-pink">{discountCode}</span>
                <button
                  type="button"
                  onClick={removeDiscountCode}
                  className="font-mono text-[10px] uppercase tracking-widest text-muted-foreground hover:text-foreground"
                >
                  Remove
                </button>
              </div>
            ) : (
              <form onSubmit={handleApplyCode} className="mt-4">
                <p className="mb-2 font-mono text-[10px] uppercase tracking-[0.3em] text-muted-foreground">
                  Promo Code
                </p>
                <div className="flex gap-2">
                  <input
                    type="text"
                    value={codeInput}
                    onChange={(e) => setCodeInput(e.target.value)}
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
                {isDiscountCodeError && (
                  <p className="mt-2 font-mono text-xs text-destructive">
                    {getErrorMessage(discountCodeError, 'Invalid discount code')}
                  </p>
                )}
              </form>
            )}
            <div className="mt-3 flex justify-between border-t border-border pt-3">
              <span className="font-bold uppercase">Total before tax</span>
              <span className="font-mono text-xl text-neon-cyan">{formatPrice(total)}</span>
            </div>
          </div>
        </aside>
      </div>
    </div>
  )
}
