// src/components/dashboard/StokvelTierStats.tsx
import React from 'react';

const StokvelTierStats: React.FC = () => {
  const tiers = [
    { name: 'Tier 3', percentage: 90, color: 'bg-blue-500' },
    { name: 'Tier 2', percentage: 75, color: 'bg-green-500' },
    { name: 'Tier 4', percentage: 60, color: 'bg-purple-500' },
    { name: 'Tier 5', percentage: 45, color: 'bg-yellow-500' },
    { name: 'Tier 6', percentage: 30, color: 'bg-red-500' },
  ];

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <h2 className="text-lg font-semibold text-gray-900 mb-4 font-jakarta">Stokvel Tier Statistics</h2>
      <div className="space-y-4">
        {tiers.map((tier, index) => (
          <div key={index}>
            <div className="flex justify-between mb-1">
              <span className="text-sm font-medium text-gray-700 font-inter">{tier.name}</span>
              <span className="text-sm font-medium text-gray-700 font-inter">{tier.percentage}%</span>
            </div>
            <div className="w-full bg-gray-200 rounded-full h-2.5">
              <div
                className={`h-2.5 rounded-full ${tier.color}`}
                style={{ width: `${tier.percentage}%` }}
              ></div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
};

export default StokvelTierStats;