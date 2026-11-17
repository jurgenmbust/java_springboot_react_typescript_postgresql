import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it, vi } from 'vitest';
import DashboardPage from '../pages/DashboardPage';
import { dashboardService } from '../services/inventory';

describe('dashboard', () => {
  it('shows server totals and an empty catalog', async () => {
    vi.spyOn(dashboardService, 'get').mockResolvedValueOnce({ totalProducts: 0, totalCategories: 0, totalInventoryValue: '0.00', lowStockCount: 0, lowStockThreshold: 5, recentProducts: [], lowStockProducts: [] });
    render(<MemoryRouter><DashboardPage/></MemoryRouter>);
    expect(await screen.findByText('$0.00')).toBeInTheDocument();
    expect(screen.getByText('Create a category, then add your first product.')).toBeInTheDocument();
    vi.restoreAllMocks();
  });
  it('shows a retry action when the API fails', async () => {
    vi.spyOn(dashboardService, 'get').mockRejectedValueOnce(new Error('Server unavailable'));
    render(<MemoryRouter><DashboardPage/></MemoryRouter>);
    expect(await screen.findByRole('alert')).toHaveTextContent('Server unavailable');
    expect(screen.getByRole('button', { name: 'Try again' })).toBeInTheDocument();
    vi.restoreAllMocks();
  });
});
