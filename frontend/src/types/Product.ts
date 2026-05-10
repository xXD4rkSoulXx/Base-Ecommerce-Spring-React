export type Product = {
  id: number;
  ref: string;
  name: string;
  category: string;
  price: number;
  image: string;
  description: string;
  sizes: string[];
  colors: string[];
  stock: number;
  featured: boolean;
  isLimited: boolean;
  discount?: number;
  createdAt: string;
};
