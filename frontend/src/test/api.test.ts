import { describe, expect, it } from 'vitest';
import { AxiosError } from 'axios';
import type { AxiosResponse } from 'axios';
import { api, ApiError } from '../services/api';

// A custom adapter tests real interceptor behavior without network access.
describe('API error handling', () => {
  it('preserves backend validation details', async () => {
    const response = { data: { message: 'Invalid input.', fieldErrors: { name: 'Required' } }, status: 400 } as AxiosResponse;
    await expect(api.get('/products', { adapter: async () => { throw new AxiosError('Bad request', 'ERR_BAD_REQUEST', undefined, undefined, response); } }))
      .rejects.toMatchObject({ message: 'Invalid input.', fieldErrors: { name: 'Required' } });
  });
  it('turns connection failures into readable errors', async () => {
    await expect(api.get('/products', { adapter: async () => { throw new AxiosError('Network Error', 'ERR_NETWORK'); } }))
      .rejects.toBeInstanceOf(ApiError);
  });
});
