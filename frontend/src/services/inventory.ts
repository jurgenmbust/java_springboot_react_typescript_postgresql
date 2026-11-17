import { api } from './api';
import type { Category, CategoryInput, Dashboard, Page, Product, ProductFilters, ProductInput } from '../types';

// Services keep HTTP details outside page components.
export const categoryService = {
  list: async (signal?: AbortSignal) => (await api.get<Category[]>('/categories', { signal })).data,
  create: async (body: CategoryInput) => (await api.post<Category>('/categories', body)).data,
  update: async (id: number, body: CategoryInput) => (await api.put<Category>(`/categories/${id}`, body)).data,
  remove: async (id: number) => { await api.delete(`/categories/${id}`); },
};
export const productService = {
  list: async (filters: ProductFilters, signal?: AbortSignal) => (await api.get<Page<Product>>('/products', { signal, params: { ...filters, categoryId: filters.categoryId || undefined, search: filters.search || undefined } })).data,
  create: async (body: ProductInput) => (await api.post<Product>('/products', body)).data,
  update: async (id: number, body: ProductInput) => (await api.put<Product>(`/products/${id}`, body)).data,
  remove: async (id: number) => { await api.delete(`/products/${id}`); },
};
export const dashboardService = {
  get: async (signal?: AbortSignal) => (await api.get<Dashboard>('/dashboard', { signal })).data,
};
