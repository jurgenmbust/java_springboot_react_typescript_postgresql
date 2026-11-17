import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, expect, it, vi } from 'vitest';
import { ProductForm, validateProduct } from '../components/ProductForm';
import { CategoryForm } from '../components/CategoryForm';
import { ApiError } from '../services/api';

const categories = [{ id: 1, name: 'Office', description: '', createdAt: '', updatedAt: '' }];
const valid = { name: 'Keyboard', sku: 'KEY-1', price: '19.99', quantity: '0', categoryId: '1', description: '' };

describe('product validation', () => {
  it('accepts zero stock and decimal money strings', () => { expect(validateProduct(valid)).toEqual({}); });
  it('rejects missing fields, negative numbers and too many decimals', () => {
    const errors = validateProduct({ ...valid, name: ' ', sku: 'bad sku', price: '-1.001', quantity: '-1', categoryId: '' });
    expect(Object.keys(errors)).toEqual(expect.arrayContaining(['name', 'sku', 'price', 'quantity', 'categoryId']));
  });
});

describe('forms', () => {
  it('shows validation messages without sending invalid product input', async () => {
    const save = vi.fn(); render(<ProductForm categories={categories} onSave={save} onCancel={vi.fn()} onBusy={vi.fn()}/>);
    await userEvent.click(screen.getByRole('button', { name: 'Create product' }));
    expect(await screen.findByText('Select a category.')).toBeInTheDocument(); expect(save).not.toHaveBeenCalled();
  });
  it('submits a valid product using a string price', async () => {
    const save = vi.fn().mockResolvedValue(undefined); const user = userEvent.setup();
    render(<ProductForm categories={categories} onSave={save} onCancel={vi.fn()} onBusy={vi.fn()}/>);
    await user.type(screen.getByLabelText('Product name *'), 'Keyboard');
    await user.type(screen.getByLabelText('SKU *'), 'KEY-1');
    await user.type(screen.getByLabelText('Price (USD) *'), '19.99');
    await user.selectOptions(screen.getByLabelText('Category *'), '1');
    await user.click(screen.getByRole('button', { name: 'Create product' }));
    expect(save).toHaveBeenCalledWith({ ...valid, quantity: 0, categoryId: 1 });
  });
  it('keeps the category draft and displays API errors', async () => {
    const user = userEvent.setup(); const save = vi.fn().mockRejectedValue(new ApiError('Category already exists.', { name: 'Choose another name.' }));
    render(<CategoryForm onSave={save} onCancel={vi.fn()} onBusy={vi.fn()}/>);
    await user.type(screen.getByLabelText('Category name *'), 'Office');
    await user.click(screen.getByRole('button', { name: 'Create category' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('Category already exists.');
    expect(screen.getByLabelText('Category name *')).toHaveValue('Office');
    expect(screen.getByText('Choose another name.')).toBeInTheDocument();
  });
  it('rejects a blank category name', async () => {
    const save = vi.fn(); render(<CategoryForm onSave={save} onCancel={vi.fn()} onBusy={vi.fn()}/>);
    await userEvent.click(screen.getByRole('button', { name: 'Create category' }));
    expect(screen.getByText('Enter a name with 1–80 characters.')).toBeInTheDocument(); expect(save).not.toHaveBeenCalled();
  });
});
