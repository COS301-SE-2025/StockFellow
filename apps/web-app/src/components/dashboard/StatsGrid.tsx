// src/components/dashboard/StatsGrid.tsx
import React from 'react';
import { TrendingUp } from 'lucide-react';

const StatsGrid: React.FC = () => {
  const stats = [
    {
      title: 'Total payouts',
      value: 'R150,000',
      change: '+3.2% from last month',
      icon: TrendingUp,
      color: 'text-green-600',
      bgColor: 'bg-green-100',
    },
    {
      title: 'Number of Stokvels',
      value: '1,250',
      change: '+12 from last month',
      icon: TrendingUp,
      color: 'text-blue-600',
      bgColor: 'bg-blue-100',
    },
    {
      title: 'Total Users Growth Rate',
      value: '+5.80%',
      change: '+1.2% from last month',
      icon: TrendingUp,
      color: 'text-purple-600',
      bgColor: 'bg-purple-100',
    },
  ];

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mt-6">
      {stats.map((stat, index) => (
        <div key={index} className="bg-white rounded-lg shadow p-6">
          <div className="flex items-center">
            <div className={`p-3 rounded-full ${stat.bgColor}`}>
              <stat.icon className={`h-6 w-6 ${stat.color}`} />
            </div>
            <div className="ml-4">
              <p className="text-sm font-medium text-gray-600 font-inter">{stat.title}</p>
              <p className="text-2xl font-semibold text-gray-900 font-jakarta">{stat.value}</p>
              <p className="text-xs text-gray-500 font-inter">{stat.change}</p>
            </div>
          </div>
        </div>
      ))}
    </div>
  );
};

export default StatsGrid;