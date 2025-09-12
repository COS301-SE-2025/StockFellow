import React from 'react'

function App() {
  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center">
      <div className="bg-white p-8 rounded-lg shadow-md">
        <h1 className="text-3xl font-bold text-blue-600 mb-4">
          Welcome to StockFellow!
        </h1>
        <p className="text-gray-700">
          React + TypeScript + Tailwind CSS is successfully set up! 🎉
        </p>
        <button className="mt-4 px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 transition-colors">
          Get Started
        </button>
      </div>
    </div>
  )
}

export default App