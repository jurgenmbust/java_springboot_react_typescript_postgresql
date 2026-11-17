import { useState } from 'react';
import type { FormEvent } from 'react';
import type { Category, Product, ProductInput } from '../types';
import { ApiError, errorMessage } from '../services/api';
import { ErrorPanel } from './Feedback';

export type ProductFields = { name: string; description: string; sku: string; price: string; quantity: string; categoryId: string };

// Validates strings before converting quantity and category to numbers.
export function validateProduct(form: ProductFields): Record<string, string> {
  const errors: Record<string, string> = {};
  if (!form.name.trim() || form.name.trim().length > 120) errors.name = 'Enter a name with 1–120 characters.';
  if (!/^[a-zA-Z0-9_-]{1,64}$/.test(form.sku)) errors.sku = 'Use 1–64 letters, numbers, hyphens or underscores.';
  if (!/^\d{1,12}(\.\d{1,2})?$/.test(form.price)) errors.price = 'Enter a non-negative price with up to 2 decimal places.';
  if (!/^\d+$/.test(form.quantity) || Number(form.quantity) > 2147483647) errors.quantity = 'Enter a whole quantity from 0 to 2147483647.';
  if (!/^[1-9]\d*$/.test(form.categoryId)) errors.categoryId = 'Select a category.';
  if (form.description.length > 2000) errors.description = 'Use 2000 characters or fewer.';
  return errors;
}

// Keeps form input local. The page supplies the create or update operation.
export function ProductForm({ product, categories, onSave, onCancel, onBusy }: { product?: Product; categories: Category[]; onSave: (input: ProductInput) => Promise<void>; onCancel: () => void; onBusy: (busy: boolean) => void }) {
  const [form, setForm] = useState<ProductFields>({ name: product?.name || '', description: product?.description || '', sku: product?.sku || '', price: product?.price || '', quantity: String(product?.quantity ?? 0), categoryId: String(product?.category.id || '') });
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);
  const change = (field: keyof ProductFields, value: string) => setForm((current) => ({ ...current, [field]: value }));
  async function submit(event: FormEvent) {
    event.preventDefault(); const invalid = validateProduct(form); setErrors(invalid); setError('');
    if (Object.keys(invalid).length) return;
    setBusy(true); onBusy(true);
    try { await onSave({ ...form, name: form.name.trim(), quantity: Number(form.quantity), categoryId: Number(form.categoryId) }); }
    catch (reason) { setError(errorMessage(reason)); if (reason instanceof ApiError) setErrors(reason.fieldErrors); }
    finally { setBusy(false); onBusy(false); }
  }
  return <form onSubmit={submit} noValidate>{error && <ErrorPanel message={error}/>}
    <fieldset disabled={busy} className="form-grid">
      <div className="full"><label htmlFor="product-name">Product name *</label><input autoFocus id="product-name" value={form.name} onChange={(e) => change('name', e.target.value)} maxLength={120} aria-invalid={!!errors.name} aria-describedby="name-error" placeholder="e.g. Wireless keyboard"/><small id="name-error" className="field-error">{errors.name}</small></div>
      <div><label htmlFor="product-sku">SKU *</label><input id="product-sku" value={form.sku} onChange={(e) => change('sku', e.target.value)} maxLength={64} aria-invalid={!!errors.sku} aria-describedby="sku-error" placeholder="KEY-001"/><small id="sku-error" className="field-error">{errors.sku}</small></div>
      <div><label htmlFor="product-category">Category *</label><select id="product-category" value={form.categoryId} onChange={(e) => change('categoryId', e.target.value)} aria-invalid={!!errors.categoryId} aria-describedby="category-error"><option value="">Select a category</option>{categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}</select><small id="category-error" className="field-error">{errors.categoryId}</small></div>
      <div><label htmlFor="product-price">Price (USD) *</label><input id="product-price" inputMode="decimal" value={form.price} onChange={(e) => change('price', e.target.value)} aria-invalid={!!errors.price} aria-describedby="price-error" placeholder="0.00"/><small id="price-error" className="field-error">{errors.price}</small></div>
      <div><label htmlFor="product-quantity">Quantity *</label><input id="product-quantity" inputMode="numeric" value={form.quantity} onChange={(e) => change('quantity', e.target.value)} aria-invalid={!!errors.quantity} aria-describedby="quantity-error"/><small id="quantity-error" className="field-error">{errors.quantity}</small></div>
      <div className="full"><label htmlFor="product-description">Description</label><textarea id="product-description" rows={3} value={form.description} onChange={(e) => change('description', e.target.value)} maxLength={2000} aria-describedby="description-error"/><small id="description-error" className="field-error">{errors.description}</small></div>
      <div className="form-actions full"><button type="button" className="button secondary" onClick={onCancel}>Cancel</button><button className="button primary" type="submit">{busy ? 'Saving…' : product ? 'Save changes' : 'Create product'}</button></div>
    </fieldset>
  </form>;
}
