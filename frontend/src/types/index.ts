// Request types describe editable fields. Responses also contain server-owned fields.
export interface CategoryInput { name: string; description: string }
export interface Category extends CategoryInput { id: number; createdAt: string; updatedAt: string }
export interface ProductInput { name: string; description: string; sku: string; price: string; quantity: number; categoryId: number }
export interface Product extends Omit<ProductInput, 'categoryId'> { id: number; category: Category; createdAt: string; updatedAt: string }
export interface Page<T> { content: T[]; page: number; size: number; totalElements: number; totalPages: number }
export interface ProductFilters { search: string; categoryId: string; lowStock: boolean; page: number; size: number; sort: string; direction: string }
export interface Dashboard { totalProducts: number; totalCategories: number; totalInventoryValue: string; lowStockCount: number; lowStockThreshold: number; recentProducts: Product[]; lowStockProducts: Product[] }
export interface ApiErrorBody { timestamp: string; status: number; message: string; path: string; fieldErrors: Record<string, string> }
