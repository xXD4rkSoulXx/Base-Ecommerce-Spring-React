import type { AdminTeamMemberResponse } from '@/types/admin/adminTeam'
import type { ShippingAddress } from '@/types/order'
import type { UserResponse } from '@/types/user'
import type { DemoCustomer } from '@/demo/types'
import { DAY, type Random } from '@/demo/data/random'

export const DEMO_USER: UserResponse = {
  email: 'demo@velociti.demo',
  firstName: 'Demo',
  lastName: 'Visitor',
  role: 'DEMO',
  permissions: [
    'DASHBOARD_READ',
    'PRODUCTS_READ',
    'STOCK_READ',
    'ORDERS_READ',
    'RETURNS_READ',
    'DISCOUNTS_READ',
    'CUSTOMERS_READ',
    'REVIEWS_READ',
    'TEAM_READ',
    'AUDIT_READ'
  ]
}

export const DEMO_ACCESS_TOKEN = 'demo-access-token'

const FIRST_NAMES = [
  'Ava', 'Liam', 'Noah', 'Maya', 'Lucas', 'Iris', 'Theo', 'Nina', 'Oscar', 'Lena', 'Hugo', 'Zoe',
  'Felix', 'Clara', 'Jonas', 'Mila', 'Aron', 'Sara', 'Leo', 'Eva', 'Tomas', 'Alba', 'Rui', 'Ines'
]

const LAST_NAMES = [
  'Moreau', 'Silva', 'Kowalski', 'Jensen', 'Costa', 'Novak', 'Bianchi', 'Weber', 'Ferreira', 'Dubois',
  'Santos', 'Meyer', 'Rossi', 'Almeida', 'Larsen', 'Petit', 'Carvalho', 'Fischer', 'Ribeiro', 'Lopez',
  'Marques', 'Hansen', 'Pereira', 'Vidal'
]

interface Place {
  city: string
  postalCode: string
  country: string
  street: string
}

const PLACES: Place[] = [
  { city: 'Lisboa', postalCode: '1000-001', country: 'PT', street: 'Rua da Prata' },
  { city: 'Porto', postalCode: '4000-100', country: 'PT', street: 'Rua de Santa Catarina' },
  { city: 'Braga', postalCode: '4700-200', country: 'PT', street: 'Avenida Central' },
  { city: 'Madrid', postalCode: '28001', country: 'ES', street: 'Calle Mayor' },
  { city: 'Paris', postalCode: '75001', country: 'FR', street: 'Rue de Rivoli' },
  { city: 'Berlin', postalCode: '10115', country: 'DE', street: 'Torstrasse' },
  { city: 'Amsterdam', postalCode: '1012 AB', country: 'NL', street: 'Damrak' },
  { city: 'Milano', postalCode: '20121', country: 'IT', street: 'Via Montenapoleone' }
]

export const CUSTOMER_COUNT = 24

export const buildCustomers = (now: number, rng: Random): DemoCustomer[] =>
  Array.from({ length: CUSTOMER_COUNT }, (_, index) => {
    const firstName = FIRST_NAMES[index]
    const lastName = LAST_NAMES[index]
    const place = rng.pick(PLACES)
    const ageInDays = index < 6 ? rng.int(1, 28) : index < 12 ? rng.int(30, 58) : rng.int(60, 220)

    const address: ShippingAddress = {
      recipientName: `${firstName} ${lastName}`,
      addressLine1: `${place.street} ${rng.int(1, 180)}`,
      addressLine2: rng.chance(0.3) ? `Apt ${rng.int(1, 40)}` : undefined,
      city: place.city,
      postalCode: place.postalCode,
      country: place.country,
      phone: `+351 9${rng.int(10000000, 99999999)}`
    }

    return {
      id: index + 1,
      email: `${firstName}.${lastName}@example.com`.toLowerCase(),
      firstName,
      lastName,
      createdAt: now - ageInDays * DAY,
      address
    }
  })

export const buildTeam = (now: number): AdminTeamMemberResponse[] => [
  {
    id: 9001,
    email: 'admin@velociti.demo',
    firstName: 'Alex',
    lastName: 'Admin',
    role: 'ADMIN',
    createdAt: new Date(now - 400 * DAY).toISOString()
  },
  {
    id: 9002,
    email: 'maria.manager@velociti.demo',
    firstName: 'Maria',
    lastName: 'Manager',
    role: 'MANAGER',
    createdAt: new Date(now - 320 * DAY).toISOString()
  },
  {
    id: 9003,
    email: 'miguel.merch@velociti.demo',
    firstName: 'Miguel',
    lastName: 'Merchandiser',
    role: 'MERCHANDISER',
    createdAt: new Date(now - 250 * DAY).toISOString()
  },
  {
    id: 9004,
    email: 'filipa.fulfilment@velociti.demo',
    firstName: 'Filipa',
    lastName: 'Fulfilment',
    role: 'FULFILLMENT',
    createdAt: new Date(now - 180 * DAY).toISOString()
  },
  {
    id: 9005,
    email: 'sofia.support@velociti.demo',
    firstName: 'Sofia',
    lastName: 'Support',
    role: 'SUPPORT',
    createdAt: new Date(now - 90 * DAY).toISOString()
  },
  {
    id: 9006,
    email: DEMO_USER.email,
    firstName: DEMO_USER.firstName,
    lastName: DEMO_USER.lastName,
    role: 'DEMO',
    createdAt: new Date(now - 7 * DAY).toISOString()
  }
]
