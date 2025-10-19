// src/components/dashboard/UpcomingEvents.tsx
import React from 'react';
import { Calendar, MapPin, Users } from 'lucide-react';

const UpcomingEvents: React.FC = () => {
  const events = [
    {
      id: 1,
      title: 'Monthly Contribution Deadline',
      date: 'Oct 25, 2023',
      time: '5:00 PM',
      location: 'Online',
      attendees: 42,
    },
    {
      id: 2,
      title: 'Stokvel Committee Meeting',
      date: 'Oct 28, 2023',
      time: '2:00 PM',
      location: 'Community Hall',
      attendees: 12,
    },
    {
      id: 3,
      title: 'Investment Strategy Session',
      date: 'Nov 5, 2023',
      time: '10:00 AM',
      location: 'Online',
      attendees: 8,
    },
  ];

  return (
    <div className="bg-white rounded-lg shadow p-6">
      <h2 className="text-lg font-semibold text-gray-900 mb-4">Upcoming Events</h2>
      <div className="space-y-4">
        {events.map((event) => (
          <div key={event.id} className="border-l-4 border-blue-500 pl-4">
            <h3 className="text-sm font-medium text-gray-900">{event.title}</h3>
            <div className="mt-1 flex items-center text-sm text-gray-500">
              <Calendar className="h-4 w-4 mr-1" />
              <span>{event.date} at {event.time}</span>
            </div>
            <div className="mt-1 flex items-center text-sm text-gray-500">
              <MapPin className="h-4 w-4 mr-1" />
              <span>{event.location}</span>
            </div>
            <div className="mt-1 flex items-center text-sm text-gray-500">
              <Users className="h-4 w-4 mr-1" />
              <span>{event.attendees} attendees</span>
            </div>
          </div>
        ))}
      </div>
      <div className="mt-4">
        <button className="text-sm text-blue-600 hover:text-blue-500">
          View all events
        </button>
      </div>
    </div>
  );
};

export default UpcomingEvents;