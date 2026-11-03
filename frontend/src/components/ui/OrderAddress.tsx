import type { ShippingAddress } from '@/types/order'

interface OrderAddressProps {
  address: ShippingAddress
}

const countryNames = new Intl.DisplayNames('en', { type: 'region' })
const formatCountry = (code: string) => countryNames.of(code) ?? code

export const OrderAddress = ({ address }: OrderAddressProps) => (
  <div className="space-y-0.5 text-xs">
    <p>{address.recipientName}</p>
    <p>
      {address.addressLine1}
      {address.addressLine2 && `, ${address.addressLine2}`}
    </p>
    <p>
      {address.postalCode} {address.city}
      {address.state && `, ${address.state}`}
    </p>
    <p>{formatCountry(address.country)}</p>
  </div>
)
