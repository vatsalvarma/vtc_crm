import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
        configure: (proxy) => {
          proxy.on('error', (_err, _req, res) => {
            if (res && !('headersSent' in res && res.headersSent)) {
              (res as any).writeHead(502, { 'Content-Type': 'application/json' });
              res.end(JSON.stringify({ error: 'BACKEND_OFFLINE' }));
            }
          });
        },
      },
      '/ws': {
        target: 'http://127.0.0.1:8080',
        ws: true,
        changeOrigin: true,
      },
    },
  },
});

