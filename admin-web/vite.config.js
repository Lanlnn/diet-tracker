import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import process from 'node:process'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 4173,
    proxy: { '/api': process.env.ADMIN_API_TARGET || 'http://127.0.0.1:8080' },
  },
})
