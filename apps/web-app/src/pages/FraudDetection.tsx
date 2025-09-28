import React, { useState, useEffect } from 'react';
import { useAuth } from "../contexts/AuthContext";
import { adminService } from "../services/adminService";
import Sidebar from "../components/layout/Sidebar";
import Header from "../components/layout/Header";
import { ChevronDown, ChevronUp, User, AlertTriangle, Shield, Clock, RefreshCw } from 'lucide-react';

interface SuspiciousActivity {
  logId: string;
  userId: string;
  endpoint: string;
  timestamp: string;
  httpMethod: string;
  responseStatus: number;
  riskScore: number;
  riskFactors: string;
  flaggedForReview: boolean;
  ipAddress?: string;
  userAgent?: string;
}

interface SuspiciousActivitiesResponse {
  suspiciousActivities: SuspiciousActivity[];
  count: number;
  generatedAt: string;
}

interface FraudEntry {
  id: string;
  user: {
    name: string;
    userId: string;
    role: string;
  };
  type: string;
  threatLevel: 'SEVERE' | 'MEDIUM' | 'LIGHT';
  date: string;
  riskScore: number;
  riskFactors: string[];
  details: SuspiciousActivity[];
}

const FraudDetection: React.FC = () => {
  const { isAuthenticated, isLoading: authLoading } = useAuth();
  const [expandedRows, setExpandedRows] = useState<Set<string>>(new Set());
  const [currentPage, setCurrentPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [fraudEntries, setFraudEntries] = useState<FraudEntry[]>([]);
  const [totalCount, setTotalCount] = useState(0);
  const [generatedAt, setGeneratedAt] = useState<string>('');
  const [investigationLoading, setInvestigationLoading] = useState<string | null>(null);

  const fetchSuspiciousActivity = async () => {
    if (!isAuthenticated) {
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError(null);

      console.log('Fetching suspicious activities...');
      const response = await adminService.getSuspiciousActivity();
      console.log('Suspicious activities response:', response);

      setTotalCount(response.count || 0);
      setGeneratedAt(response.generatedAt || new Date().toISOString());

      if (response.suspiciousActivities && response.suspiciousActivities.length > 0) {
        // Group activities by user and create fraud entries
        const groupedByUser = groupActivitiesByUser(response.suspiciousActivities);
        setFraudEntries(groupedByUser);
      } else {
        // No suspicious activities found
        setFraudEntries([]);
      }

    } catch (err: any) {
      console.error('Error fetching suspicious activities:', err);
      
      if (err.response?.status === 401) {
        setError("Access denied. Please log in again.");
      } else if (err.response?.status === 403) {
        setError("You don't have permission to access fraud detection data.");
      } else {
        setError(err.message || "Failed to fetch suspicious activities");
      }
    } finally {
      setLoading(false);
    }
  };

  const groupActivitiesByUser = (activities: SuspiciousActivity[]): FraudEntry[] => {
    const userGroups: { [key: string]: SuspiciousActivity[] } = {};
    
    // Group by userId
    activities.forEach(activity => {
      const userId = activity.userId || 'Anonymous';
      if (!userGroups[userId]) {
        userGroups[userId] = [];
      }
      userGroups[userId].push(activity);
    });

    // Convert to FraudEntry format
    return Object.entries(userGroups).map(([userId, userActivities], index) => {
      const maxRiskScore = Math.max(...userActivities.map(a => a.riskScore || 0));
      const allRiskFactors = userActivities
        .map(a => a.riskFactors)
        .filter(rf => rf)
        .flatMap(rf => rf.split(','))
        .map(f => f.trim())
        .filter((f, i, arr) => arr.indexOf(f) === i); // unique factors

      const threatLevel: 'SEVERE' | 'MEDIUM' | 'LIGHT' = 
        maxRiskScore >= 70 ? 'SEVERE' :
        maxRiskScore >= 40 ? 'MEDIUM' : 'LIGHT';

      const fraudType = determineFraudType(allRiskFactors);
      const latestActivity = userActivities.sort((a, b) => 
        new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
      )[0];

      return {
        id: `fraud-${index}`,
        user: {
          name: userId === 'Anonymous' ? 'Unknown User' : userId,
          userId: userId,
          role: 'User'
        },
        type: fraudType,
        threatLevel,
        date: formatDate(latestActivity.timestamp),
        riskScore: maxRiskScore,
        riskFactors: allRiskFactors,
        details: userActivities.sort((a, b) => 
          new Date(b.timestamp).getTime() - new Date(a.timestamp).getTime()
        )
      };
    });
  };

  const determineFraudType = (riskFactors: string[]): string => {
    if (riskFactors.some(f => f.includes('UNUSUAL_USER_AGENT'))) {
      return 'Bot/Automation Detection';
    }
    if (riskFactors.some(f => f.includes('HIGH_FREQUENCY_ACCESS'))) {
      return 'Suspicious Activity Pattern';
    }
    if (riskFactors.some(f => f.includes('OFF_HOURS_ACTIVITY'))) {
      return 'Unusual Access Time';
    }
    if (riskFactors.some(f => f.includes('SENSITIVE_ENDPOINT'))) {
      return 'Unauthorized Access Attempt';
    }
    return 'Identity Fraud';
  };

  const handleInvestigate = async (logId: string, reason?: string) => {
    try {
      setInvestigationLoading(logId);
      
      const investigationReason = reason || `Flagged for investigation due to suspicious activity patterns - Risk Score Analysis`;
      
      console.log('Marking log for investigation:', { logId, reason: investigationReason });
      
      await adminService.markLogForInvestigation(logId, investigationReason);
      
      // Refresh the data after successful investigation
      await fetchSuspiciousActivity();
      
      // Show success (you might want to add a toast notification here)
      console.log('Successfully marked log for investigation');
      
    } catch (err: any) {
      console.error('Error marking log for investigation:', err);
      setError(`Failed to mark log for investigation: ${err.message || 'Unknown error'}`);
    } finally {
      setInvestigationLoading(null);
    }
  };

  const formatDate = (timestamp: string) => {
    return new Date(timestamp).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  const formatDateTime = (timestamp: string) => {
    return new Date(timestamp).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  useEffect(() => {
    if (!authLoading && isAuthenticated) {
      fetchSuspiciousActivity();
    }
  }, [isAuthenticated, authLoading]);

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
        return 'text-blue-600';
      default:
        return 'text-gray-600';
    }
  };

  const getStatusColor = (status: number) => {
    if (status >= 200 && status < 300) return 'bg-green-100 text-green-800';
    if (status >= 400 && status < 500) return 'bg-red-100 text-red-800';
    if (status >= 500) return 'bg-purple-100 text-purple-800';
    return 'bg-gray-100 text-gray-800';
  };

  const getMethodColor = (method: string) => {
    switch (method) {
      case 'POST': return 'bg-green-100 text-green-800';
      case 'GET': return 'bg-blue-100 text-blue-800';
      case 'DELETE': return 'bg-red-100 text-red-800';
      case 'PUT': case 'PATCH': return 'bg-yellow-100 text-yellow-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  // Show loading while checking authentication
  if (authLoading) {
    return (
      <div className="flex h-screen bg-gray-50">
        <Sidebar />
        <div className="flex-1 flex flex-col overflow-hidden">
          <Header />
          <main className="flex-1 overflow-x-hidden overflow-y-auto bg-gray-50">
            <div className="container mx-auto px-6 py-8">
              <div className="bg-white rounded-lg shadow-sm">
                <div className="p-6 border-b border-gray-200">
                  <h1 className="text-2xl font-semibold text-gray-900">Fraud Detection</h1>
                </div>
                <div className="p-8 text-center">
                  <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
                  <p className="mt-4 text-gray-600">Checking authentication...</p>
                </div>
              </div>
            </div>
          </main>
        </div>
      </div>
    );
  }

  // Show authentication required message
  if (!isAuthenticated) {
    return (
      <div className="flex h-screen bg-gray-50">
        <Sidebar />
        <div className="flex-1 flex flex-col overflow-hidden">
          <Header />
          <main className="flex-1 overflow-x-hidden overflow-y-auto bg-gray-50">
            <div className="container mx-auto px-6 py-8">
              <div className="bg-white rounded-lg shadow-sm">
                <div className="p-6 border-b border-gray-200">
                  <h1 className="text-2xl font-semibold text-gray-900">Fraud Detection</h1>
                </div>
                <div className="p-8 text-center">
                  <div className="bg-yellow-50 border-l-4 border-yellow-400 p-4">
                    <p className="text-sm text-yellow-700">
                      Authentication required to access fraud detection data. Please log in with admin credentials.
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </main>
        </div>
      </div>
    );
  }

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar />
      <div className="flex-1 flex flex-col overflow-hidden">
        <Header />
        <main className="flex-1 overflow-x-hidden overflow-y-auto bg-gray-50">
          <div className="container mx-auto px-6 py-8">
            <div className="bg-white rounded-lg shadow-sm">
              <div className="p-6 border-b border-gray-200">
                <div className="flex items-center justify-between">
                  <div>
                    <h1 className="text-2xl font-semibold text-gray-900 flex items-center">
                      <Shield className="h-6 w-6 mr-2 text-red-600" />
                      Fraud Detection
                    </h1>
                    <p className="text-gray-600 mt-1">
                      {totalCount} suspicious activities detected
                      {generatedAt && (
                        <span className="text-sm text-gray-500 ml-2">
                          (Last updated: {formatDateTime(generatedAt)})
                        </span>
                      )}
                    </p>
                  </div>
                  <button
                    onClick={fetchSuspiciousActivity}
                    disabled={loading}
                    className="flex items-center px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50"
                  >
                    <RefreshCw className={`h-4 w-4 mr-2 ${loading ? 'animate-spin' : ''}`} />
                    Refresh
                  </button>
                </div>
              </div>

              {error && (
                <div className="bg-red-50 border-l-4 border-red-400 p-4 m-6">
                  <div className="flex">
                    <div className="flex-shrink-0">
                      <AlertTriangle className="h-5 w-5 text-red-400" />
                    </div>
                    <div className="ml-3">
                      <p className="text-sm text-red-700">{error}</p>
                      <div className="mt-2">
                        <button
                          onClick={fetchSuspiciousActivity}
                          className="text-sm bg-red-100 text-red-700 px-3 py-1 rounded hover:bg-red-200"
                        >
                          Retry
                        </button>
                      </div>
                    </div>
                  </div>
                </div>
              )}

              {loading && fraudEntries.length === 0 ? (
                <div className="p-8 text-center">
                  <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
                  <p className="mt-4 text-gray-600">Loading fraud detection data...</p>
                </div>
              ) : (
                <>
                  {fraudEntries.length === 0 ? (
                    <div className="p-8 text-center">
                      <Shield className="h-12 w-12 text-green-500 mx-auto mb-4" />
                      <h3 className="text-lg font-medium text-gray-900 mb-2">No Suspicious Activity Detected</h3>
                      <p className="text-gray-600">
                        All user activities appear normal. The system will continue monitoring for suspicious patterns.
                      </p>
                    </div>
                  ) : (
                    <>
                      {/* Table Header */}
                      <div className="grid grid-cols-12 gap-4 px-6 py-4 border-b border-gray-200 text-sm font-medium text-gray-700 uppercase tracking-wider">
                        <div className="col-span-3">USER</div>
                        <div className="col-span-2">TYPE</div>
                        <div className="col-span-2">THREAT LEVEL</div>
                        <div className="col-span-2">RISK SCORE</div>
                        <div className="col-span-2">DATE</div>
                        <div className="col-span-1"></div>
                      </div>

                      {/* Table Rows */}
                      <div className="divide-y divide-gray-200">
                        {fraudEntries.map((entry) => (
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
                                <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                                  entry.riskScore >= 70 ? 'text-red-700 bg-red-100' :
                                  entry.riskScore >= 40 ? 'text-yellow-700 bg-yellow-100' :
                                  'text-blue-700 bg-blue-100'
                                }`}>
                                  {entry.riskScore}
                                </span>
                              </div>
                              <div className="col-span-2 flex items-center">
                                <span className="text-sm text-gray-900">{entry.date}</span>
                              </div>
                              <div className="col-span-1 flex items-center justify-end">
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
                                <div className="mb-4">
                                  <h4 className="text-sm font-medium text-gray-900 mb-2">Risk Factors:</h4>
                                  <div className="flex flex-wrap gap-2">
                                    {entry.riskFactors.map((factor, idx) => (
                                      <span key={idx} className="px-2 py-1 bg-yellow-100 text-yellow-800 text-xs rounded-full">
                                        {factor}
                                      </span>
                                    ))}
                                  </div>
                                </div>
                                
                                <div className="overflow-x-auto">
                                  <h4 className="text-sm font-medium text-gray-900 mb-2">Recent Activities:</h4>
                                  <table className="min-w-full">
                                    <thead>
                                      <tr className="text-xs text-gray-500">
                                        <th className="text-left py-1 px-2">ID</th>
                                        <th className="text-left py-1 px-2">ENDPOINT</th>
                                        <th className="text-left py-1 px-2">DATE</th>
                                        <th className="text-left py-1 px-2">METHOD</th>
                                        <th className="text-left py-1 px-2">STATUS</th>
                                        <th className="text-left py-1 px-2">RISK</th>
                                        <th className="text-left py-1 px-2">ACTION</th>
                                      </tr>
                                    </thead>
                                    <tbody className="divide-y divide-gray-200">
                                      {entry.details.map((detail) => (
                                        <tr key={detail.logId} className="hover:bg-white">
                                          <td className="py-2 px-2 text-sm text-gray-900 font-mono">
                                            {detail.logId.slice(0, 8)}...
                                          </td>
                                          <td className="py-2 px-2 text-sm text-gray-900 font-mono">
                                            {detail.endpoint}
                                          </td>
                                          <td className="py-2 px-2 text-sm text-gray-900">
                                            {formatDateTime(detail.timestamp)}
                                          </td>
                                          <td className="py-2 px-2">
                                            <span className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${getMethodColor(detail.httpMethod)}`}>
                                              {detail.httpMethod}
                                            </span>
                                          </td>
                                          <td className="py-2 px-2">
                                            <span className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${getStatusColor(detail.responseStatus)}`}>
                                              {detail.responseStatus}
                                            </span>
                                          </td>
                                          <td className="py-2 px-2">
                                            <span className={`inline-flex px-2 py-1 text-xs font-medium rounded-full ${
                                              detail.riskScore >= 70 ? 'text-red-700 bg-red-100' :
                                              detail.riskScore >= 40 ? 'text-yellow-700 bg-yellow-100' :
                                              'text-blue-700 bg-blue-100'
                                            }`}>
                                              {detail.riskScore}
                                            </span>
                                          </td>
                                          <td className="py-2 px-2">
                                            <button
                                              onClick={() => handleInvestigate(detail.logId)}
                                              disabled={investigationLoading === detail.logId || detail.flaggedForReview}
                                              className={`px-3 py-1 text-xs font-medium rounded-md transition-colors ${
                                                detail.flaggedForReview 
                                                  ? 'bg-gray-100 text-gray-500 cursor-not-allowed'
                                                  : 'bg-red-600 text-white hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500'
                                              }`}
                                            >
                                              {investigationLoading === detail.logId ? (
                                                <RefreshCw className="h-3 w-3 animate-spin" />
                                              ) : detail.flaggedForReview ? (
                                                'Flagged'
                                              ) : (
                                                'Investigate'
                                              )}
                                            </button>
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
                    </>
                  )}
                </>
              )}
            </div>
          </div>
        </main>
      </div>
    </div>
  );
};

export default FraudDetection;