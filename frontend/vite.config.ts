import { defineConfig } from 'vitest/config';
import { loadEnv } from 'vite';
import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';

// Vite reads only public VITE_ settings into browser code. Other values stay here.
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, '..', '');
  return {
    plugins: [react(), tailwindcss()],
    envDir: '..',
    server: { port: 5173, strictPort: true, proxy: { '/api': `http://127.0.0.1:${env.BACKEND_PORT || 8081}` } },
    test: { environment: 'jsdom', setupFiles: ['./src/test/setup.ts'], globals: true, css: false },
  };
});
