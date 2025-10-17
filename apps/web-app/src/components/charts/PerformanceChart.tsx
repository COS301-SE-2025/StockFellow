// src/components/charts/PerformanceChart.tsx
import React from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const PerformanceChart: React.FC = () => {
  // Mock data for stokvel performance comparison
  const data = [
    { name: 'Family Stokvel', value: 240000, growth: 12 },
    { name: 'Workplace Stokvel', value: 180000, growth: 8 },
    { name: 'Church Group', value: 150000, growth: 15 },
    { name: 'Community Stokvel', value: 120000, growth: 5 },
    { name: 'Youth Investment', value: 90000, growth: 20 },
  ];

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <h2 className="text-lg font-semibold text-gray-900 mb-4">Stokvel Performance Comparison</h2>
      <div className="h-80">
        <ResponsiveContainer width="100%" height="100%">
          <BarChart data={data} layout="vertical" margin={{ top: 5, right: 30, left: 100, bottom: 5 }}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis type="number" />
            <YAxis type="category" dataKey="name" width={80} />
            <Tooltip 
              formatter={(value, name) => {
                if (name === 'value') return [`R${value}`, 'Total Value'];
                return [`${value}%`, 'Growth'];
              }}
            />
            <Legend />
            <Bar dataKey="value" name="Total Value" fill="#3b82f6" />
            <Bar dataKey="growth" name="Growth %" fill="#10b981" />
          </BarChart>
        </ResponsiveContainer>
      </div>
      <div className="mt-4 text-sm text-gray-500">
        <p>Comparison of different stokvel groups by total value and growth percentage</p>
      </div>
    </div>
  );
};

export default PerformanceChart;