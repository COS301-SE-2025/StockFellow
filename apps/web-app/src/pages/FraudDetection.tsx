import React, { useState } from 'react';
import Sidebar from "../components/layout/Sidebar";
import { ChevronDown, ChevronUp, User } from 'lucide-react';

interface FraudEntry {
  id: string;
  user: {
    name: string;
    avatar: string;
    role: string;
  };
  type: string;
  threatLevel: 'SEVERE' | 'MEDIUM' | 'LIGHT';
  date: string;
  details: {
    id: string;
    user: string;
    endpoint: string;
    date: string;
    method: string;
    status: number;
  }[];
}

const FraudDetection: React.FC = () => {
  const [expandedRows, setExpandedRows] = useState<Set<string>>(new Set(['1']));
  const [currentPage, setCurrentPage] = useState(1);

  const fraudData: FraudEntry[] = [
    {
      id: '1',
      user: {
        name: 'Obito Uchiha',
        avatar: 'https://i.pravatar.cc/40?img=1',
        role: 'User'
      },
      type: 'Identity Fraud',
      threatLevel: 'SEVERE',
      date: '17 August 2025',
      details: [
        {
          id: '00042',
          user: 'Obito Uchiha',
          endpoint: '/auth/login',
          date: '14 June',
          method: 'POST',
          status: 403
        },
        {
          id: '00033',
          user: 'Obito Uchiha',
          endpoint: '/auth/login',
          date: '14 June 2025',
          method: 'POST',
          status: 403
        },
        {
          id: '00024',
          user: 'Obito Uchiha',
          endpoint: '/auth/login',
          date: '13 June 2025',
          method: 'POST',
          status: 403
        }
      ]
    },
    {
      id: '2',
      user: {
        name: 'Obito Uchiha',
        avatar: 'https://i.pravatar.cc/40?img=1',
        role: 'User'
      },
      type: 'Identity Fraud',
      threatLevel: 'MEDIUM',
      date: '17 August 2025',
      details: []
    },
    {
      id: '3',
      user: {
        name: 'Obito Uchiha',
        avatar: 'https://i.pravatar.cc/40?img=1',
        role: 'User'
      },
      type: 'Identity Fraud',
      threatLevel: 'LIGHT',
      date: '17 August 2025',
      details: []
    },
    {
      id: '4',
      user: {
        name: 'Obito Uchiha',
        avatar: 'https://i.pravatar.cc/40?img=1',
        role: 'User'
      },
      type: 'Identity Fraud',
      threatLevel: 'SEVERE',
      date: '17 August 2025',
      details: []
    }
  ];

  const toggleRow = (id: string) => {
    const newExpandedRows = new Set(expandedRows);
    if (expandedRows.has(id)) {
      newExpandedRows.delete(id);
    } else {
      newExpandedRows.add(id);
    }
    setExpandedRows(newExpandedRows);
  };

  const getThreatLevelColor = (level: string) => {
    switch (level) {
      case 'SEVERE':
        return 'text-red-600';
      case 'MEDIUM':
        return 'text-yellow-600';
      case 'LIGHT':
        return 'text-gray-400';
      default:
        return 'text-gray-600';
    }
  };

  const getStatusColor = (status: number) => {
    return status >= 400 ? 'bg-red-100 text-red-800' : 'bg-green-100 text-green-800';
  };

  return (
    <div className="flex h-screen bg-gray-50">
      {/* Sidebar */}
      <Sidebar />

      {/* Main Content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Header */}
        <div className="flex items-center justify-between bg-white p-4 shadow">
          <div className="relative">
            <input
              type="text"
              placeholder="Search"
              className="rounded-md border px-3 py-2 pl-8 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 w-80"
            />
            <svg className="absolute left-2 top-2.5 h-4 w-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </div>
          <div className="flex items-center space-x-2">
            <img
              src="https://i.pravatar.cc/40?img=5"
              alt="user"
              className="h-8 w-8 rounded-full"
            />
            <div className="text-right">
              <span className="block text-sm font-medium">Naruto Uzumaki</span>
              <span className="block text-xs text-gray-500">Admin</span>
            </div>
          </div>
        </div>

        {/* Main Content Area */}
        <main className="flex-1 overflow-x-hidden overflow-y-auto bg-gray-50">
          <div className="container mx-auto px-6 py-8">
            <div className="bg-white rounded-lg shadow">
              {/* Table Header */}
              <div className="grid grid-cols-12 gap-4 px-6 py-4 border-b border-gray-200 text-sm font-medium text-gray-700 uppercase tracking-wider">
                <div className="col-span-3">USER</div>
                <div className="col-span-2">TYPE</div>
                <div className="col-span-2">THREAT LEVEL</div>
                <div className="col-span-2">DATE</div>
                <div className="col-span-3"></div>
              </div>

              {/* Table Rows */}
              <div className="divide-y divide-gray-200">
                {fraudData.map((entry) => (
                  <div key={entry.id}>
                    {/* Main Row */}
                    <div className="grid grid-cols-12 gap-4 px-6 py-4 hover:bg-gray-50">
                      <div className="col-span-3 flex items-center space-x-3">
                        <div className="flex-shrink-0">
                          <div className="h-10 w-10 rounded-full bg-blue-100 flex items-center justify-center">
                            <User className="h-5 w-5 text-blue-600" />
                          </div>
                        </div>
                        <div>
                          <div className="text-sm font-medium text-gray-900">{entry.user.name}</div>
                          <div className="text-sm text-gray-500">{entry.user.role}</div>
                        </div>
                      </div>
                      <div className="col-span-2 flex items-center">
                        <span className="text-sm text-gray-900">{entry.type}</span>
                      </div>
                      <div className="col-span-2 flex items-center">
                        <span className={`text-sm font-medium ${getThreatLevelColor(entry.threatLevel)}`}>
                          {entry.threatLevel}
                        </span>
                      </div>
                      <div className="col-span-2 flex items-center">
                        <span className="text-sm text-gray-900">{entry.date}</span>
                      </div>
                      <div className="col-span-3 flex items-center justify-end space-x-2">
                        <button className="px-4 py-2 bg-gray-900 text-white text-sm rounded-md hover:bg-gray-800 transition-colors">
                          Manage
                        </button>
                        <button
                          onClick={() => toggleRow(entry.id)}
                          className="p-1 hover:bg-gray-100 rounded"
                        >
                          {expandedRows.has(entry.id) ? (
                            <ChevronUp className="h-5 w-5 text-gray-500" />
                          ) : (
                            <ChevronDown className="h-5 w-5 text-gray-500" />
                          )}
                        </button>
                      </div>
                    </div>

                    {/* Expanded Details */}
                    {expandedRows.has(entry.id) && entry.details.length > 0 && (
                      <div className="bg-gray-50 px-6 py-4">
                        <div className="overflow-x-auto">
                          <table className="min-w-full">
                            <tbody className="divide-y divide-gray-200">
                              {entry.details.map((detail) => (
                                <tr key={detail.id} className="hover:bg-white">
                                  <td className="py-2 px-4 text-sm text-gray-900 w-20">{detail.id}</td>
                                  <td className="py-2 px-4 text-sm text-gray-900">{detail.user}</td>
                                  <td className="py-2 px-4 text-sm text-gray-900">{detail.endpoint}</td>
                                  <td className="py-2 px-4 text-sm text-gray-900">{detail.date}</td>
                                  <td className="py-2 px-4 text-sm text-gray-900">{detail.method}</td>
                                  <td className="py-2 px-4">
                                    <span className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${getStatusColor(detail.status)}`}>
                                      {detail.status}
                                    </span>
                                  </td>
                                </tr>
                              ))}
                            </tbody>
                          </table>
                        </div>
                      </div>
                    )}
                  </div>
                ))}
              </div>

              {/* Pagination */}
              <div className="px-6 py-4 border-t border-gray-200">
                <div className="flex items-center justify-center space-x-2">
                  <button
                    onClick={() => setCurrentPage(Math.max(1, currentPage - 1))}
                    disabled={currentPage === 1}
                    className={`flex items-center px-3 py-2 text-sm font-medium rounded-md transition-colors duration-200 ${
                      currentPage === 1
                        ? "text-gray-400 cursor-not-allowed"
                        : "text-blue-600 hover:bg-blue-50"
                    }`}
                  >
                    <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                    </svg>
                    Previous
                  </button>

                  <div className="flex space-x-1">
                    {[1, 2, 3, 4].map((page) => (
                      <button
                        key={page}
                        onClick={() => setCurrentPage(page)}
                        className={`w-8 h-8 text-sm font-medium rounded-md transition-colors duration-200 ${
                          currentPage === page
                            ? "bg-blue-600 text-white"
                            : "text-gray-600 hover:bg-gray-100"
                        }`}
                      >
                        {page}
                      </button>
                    ))}
                  </div>

                  <button
                    onClick={() => setCurrentPage(Math.min(4, currentPage + 1))}
                    disabled={currentPage === 4}
                    className={`flex items-center px-3 py-2 text-sm font-medium rounded-md transition-colors duration-200 ${
                      currentPage === 4
                        ? "text-gray-400 cursor-not-allowed"
                        : "text-blue-600 hover:bg-blue-50"
                    }`}
                  >
                    Next
                    <svg className="w-4 h-4 ml-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </main>
      </div>
    </div>
  );
};

export default FraudDetection;