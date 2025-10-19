// src/components/dashboard/RecentActivities.tsx
import React from 'react';

const RecentActivities: React.FC = () => {
  const activities = [
    { id: 1, action: 'Stokvel Created', name: 'The Akatsuki', time: '2 hours ago' },
    { id: 2, action: 'Payment Processed', name: 'Retailer Purchase', time: '5 hours ago' },
    { id: 3, action: 'User Registered', name: 'John Doe', time: '1 day ago' },
    { id: 4, action: 'Withdrawal Request', name: 'Sarah Johnson', time: '2 days ago' },
    { id: 5, action: 'Stokvel Created', name: 'Son Goku', time: '2 days ago' },
  ];

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <h2 className="text-lg font-semibold text-gray-900 mb-4 font-jakarta">Recent Activities</h2>
      <div className="space-y-4">
        {activities.map((activity) => (
          <div key={activity.id} className="flex items-center justify-between py-2 border-b border-gray-100">
            <div>
              <p className="text-sm font-medium text-gray-900 font-inter">{activity.action}</p>
              <p className="text-sm text-gray-500 font-inter">{activity.name}</p>
            </div>
            <span className="text-sm text-gray-500 font-inter">{activity.time}</span>
          </div>
        ))}
      </div>
    </div>
  );
};

export default RecentActivities;