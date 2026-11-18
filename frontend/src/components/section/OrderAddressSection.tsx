import { formatCountry } from '@/utils/format'
import type { ShippingAddress } from '@/types/order'

interface OrderAddressSectionProps {
  address: ShippingAddress
}

export const OrderAddressSection = ({ address }: OrderAddressSectionProps) => (
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
