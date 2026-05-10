import type { Product } from "@/types/Product";
import api from "./api";

export const getProducts = async (): Promise<Product[]> => {
  const res = await api.get<Product[]>("/products");
  return res.data;
};
