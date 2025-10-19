import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
    // Ensure single React instance
    dedupe: ['react', 'react-dom']
  },
  server: {
    hmr: {
      overlay: false // This disables the error overlay if needed
    }
  },
  optimizeDeps: {
    // Force pre-bundling of React to avoid version conflicts
    include: ['react', 'react-dom']
  }
})