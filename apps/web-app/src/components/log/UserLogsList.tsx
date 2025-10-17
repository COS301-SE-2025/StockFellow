import { useState, useEffect } from "react";
import { useAuth } from "../../contexts/AuthContext";
import { adminService } from "../../services/adminService";
// import LogEntry from "./LogEntry";
import Pagination from "../common/Pagination";

// Updated interface to match backend response
interface Log {
  logId: string;           // Backend sends log_id
  userId: string;
  endpoint: string;
  timestamp: string;
  httpMethod: "POST" | "GET" | "DELETE" | "PUT" | "PATCH"; // Backend sends http_method
  responseStatus: number;   // Backend sends response_status
  ipAddress: string;       // Backend sends ip_address
  userAgent?: string;      // Backend sends user_agent
  responseTime?: number;
  flaggedForReview?: boolean;  // Backend sends flagged_for_review
  riskScore?: number;      // Backend sends risk_score
  riskFactors?: string;    // Backend sends risk_factors
}

// interface PaginatedResponse {
//   content: Log[];
//   totalElements: number;
//   totalPages: number;
//   size: number;
//   number: number;
//   first: boolean;
//   last: boolean;
// }

const UserLogsList = () => {
  const { isAuthenticated, isLoading: authLoading } = useAuth();
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [logs, setLogs] = useState<Log[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // Filter states
  const [userIdFilter, setUserIdFilter] = useState("");
  const [endpointFilter, setEndpointFilter] = useState("");
  const [flaggedOnly, setFlaggedOnly] = useState(false);

  const fetchLogs = async (page: number, size: number) => {
    if (!isAuthenticated) {
      console.log('Not authenticated, skipping fetch');
      setLoading(false);
      return;
    }

    if (authLoading) {
      console.log('Auth still loading, skipping fetch');
      return;
    }

    try {
      setLoading(true);
      setError(null);
      
      console.log('Fetching audit logs with params:', {
        page,
        size,
        userIdFilter,
        endpointFilter,
        flaggedOnly
      });

      const data = await adminService.getAuditLogs({
        userId: userIdFilter || undefined,
        endpoint: endpointFilter || undefined,
        flaggedOnly: flaggedOnly || undefined,
        page,
        size
      });
      
      console.log('Received logs data:', data);
      
      setLogs(data.content || []);
      setTotalPages(data.totalPages || 0);
      setTotalElements(data.totalElements || 0);
      
    } catch (err: any) {
      console.error('Error fetching logs:', err);
      
      if (err.response?.status === 401) {
        setError("Access denied. Your session may have expired. Please log in again.");
      } else if (err.response?.status === 403) {
        setError("You don't have permission to access audit logs. Admin role required.");
      } else if (err.message === 'Authentication required') {
        setError("Authentication required. Please log in again.");
      } else {
        setError(err.message || "An error occurred while fetching logs");
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    console.log('Auth state changed - isAuthenticated:', isAuthenticated, 'authLoading:', authLoading);
    if (!authLoading && isAuthenticated) {
      fetchLogs(currentPage, pageSize);
    }
  }, [currentPage, pageSize, userIdFilter, endpointFilter, flaggedOnly, isAuthenticated, authLoading]);

  const handleSearch = () => {
    setCurrentPage(0);
    fetchLogs(0, pageSize);
  };

  const handleResetFilters = () => {
    setUserIdFilter("");
    setEndpointFilter("");
    setFlaggedOnly(false);
    setCurrentPage(0);
  };

  const handleRetry = () => {
    fetchLogs(currentPage, pageSize);
  };

  const formatDate = (timestamp: string) => {
    return new Date(timestamp).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const getMethodColor = (method: string) => {
    switch (method) {
      case 'POST':
        return 'text-green-700 bg-green-100';
      case 'GET':
        return 'text-blue-700 bg-blue-100';
      case 'DELETE':
        return 'text-red-700 bg-red-100';
      case 'PUT':
      case 'PATCH':
        return 'text-yellow-700 bg-yellow-100';
      default:
        return 'text-gray-700 bg-gray-100';
    }
  };

  const getStatusColor = (status: number) => {
    if (status >= 200 && status < 300) {
      return 'text-green-700 bg-green-100';
    } else if (status >= 400 && status < 500) {
      return 'text-red-700 bg-red-100';
    } else if (status >= 500) {
      return 'text-purple-700 bg-purple-100';
    }
    return 'text-gray-700 bg-gray-100';
  };

  // Show loading while checking authentication
  if (authLoading) {
    return (
      <div className="bg-white rounded-lg shadow-sm">
        <div className="p-6 border-b border-gray-200">
          <h1 className="text-2xl font-semibold text-gray-900">User Logs</h1>
        </div>
        <div className="p-8 text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Checking authentication...</p>
        </div>
      </div>
    );
  }

  // Show authentication required message
  if (!isAuthenticated) {
    return (
      <div className="bg-white rounded-lg shadow-sm">
        <div className="p-6 border-b border-gray-200">
          <h1 className="text-2xl font-semibold text-gray-900">User Logs</h1>
        </div>
        <div className="p-8 text-center">
          <div className="bg-yellow-50 border-l-4 border-yellow-400 p-4 mb-4">
            <div className="flex">
              <div className="flex-shrink-0">
                <svg className="h-5 w-5 text-yellow-400" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                </svg>
              </div>
              <div className="ml-3">
                <p className="text-sm text-yellow-700">
                  Authentication required to view audit logs. Please log in with admin credentials.
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  // Show loading while fetching logs
  if (loading && logs.length === 0) {
    return (
      <div className="bg-white rounded-lg shadow-sm">
        <div className="p-6 border-b border-gray-200">
          <h1 className="text-2xl font-semibold text-gray-900">User Logs</h1>
        </div>
        <div className="p-8 text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading logs...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-sm">
      <div className="p-6 border-b border-gray-200">
        <h1 className="text-2xl font-semibold text-gray-900">User Logs</h1>
        <p className="text-gray-600 mt-1">Total {totalElements} logs found</p>
        
        {/* Filters */}
        <div className="mt-4 grid grid-cols-1 md:grid-cols-4 gap-4">
          <div>
            <label htmlFor="userId" className="block text-sm font-medium text-gray-700 mb-1">
              User ID
            </label>
            <input
              type="text"
              id="userId"
              value={userIdFilter}
              onChange={(e) => setUserIdFilter(e.target.value)}
              placeholder="Filter by user ID"
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          
          <div>
            <label htmlFor="endpoint" className="block text-sm font-medium text-gray-700 mb-1">
              Endpoint
            </label>
            <input
              type="text"
              id="endpoint"
              value={endpointFilter}
              onChange={(e) => setEndpointFilter(e.target.value)}
              placeholder="Filter by endpoint"
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
          </div>
          
          <div className="flex items-end">
            <label className="flex items-center">
              <input
                type="checkbox"
                checked={flaggedOnly}
                onChange={(e) => setFlaggedOnly(e.target.checked)}
                className="rounded border-gray-300 text-blue-600 focus:ring-blue-500"
              />
              <span className="ml-2 text-sm text-gray-700">Flagged only</span>
            </label>
          </div>
          
          <div className="flex items-end space-x-2">
            <button
              onClick={handleSearch}
              className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              Search
            </button>
            <button
              onClick={handleResetFilters}
              className="px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-gray-500"
            >
              Reset
            </button>
          </div>
        </div>
      </div>

      {error && (
        <div className="bg-red-50 border-l-4 border-red-400 p-4 m-4">
          <div className="flex">
            <div className="flex-shrink-0">
              <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
              </svg>
            </div>
            <div className="ml-3">
              <p className="text-sm text-red-700">{error}</p>
              <div className="mt-2">
                <button
                  onClick={handleRetry}
                  className="text-sm bg-red-100 text-red-700 px-3 py-1 rounded hover:bg-red-200"
                >
                  Retry
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Table Header */}
      <div className="bg-gray-50 px-6 py-3 border-b border-gray-200">
        <div className="grid grid-cols-8 gap-4 text-xs font-medium text-gray-500 uppercase tracking-wider">
          <div>ID</div>
          <div>USER</div>
          <div>ENDPOINT</div>
          <div>DATE</div>
          <div>METHOD</div>
          <div>STATUS</div>
          <div>RISK SCORE</div>
          <div>FLAGGED</div>
        </div>
      </div>

      {/* Table Body */}
      <div className="divide-y divide-gray-200">
        {logs.length === 0 && !loading ? (
          <div className="p-8 text-center text-gray-500">
            {totalElements === 0 ? 'No logs available' : 'No logs found matching your criteria'}
          </div>
        ) : (
          logs.map((log) => (
            <div key={log.logId} className="px-6 py-4 hover:bg-gray-50 transition-colors duration-200">
              <div className="grid grid-cols-8 gap-4 items-center text-sm">
                <div className="text-gray-900 font-mono text-xs">
                  {log.logId?.slice(0, 8) || 'N/A'}...
                </div>
                <div className="text-gray-900 font-mono text-xs">
                  {log.userId || 'Anonymous'}
                </div>
                <div className="text-gray-600 font-mono text-xs truncate" title={log.endpoint}>
                  {log.endpoint || 'N/A'}
                </div>
                <div className="text-gray-600 text-xs">
                  {log.timestamp ? formatDate(log.timestamp) : 'N/A'}
                </div>
                <div>
                  <span className={`px-2 py-1 rounded-full text-xs font-medium ${getMethodColor(log.httpMethod)}`}>
                    {log.httpMethod || 'N/A'}
                  </span>
                </div>
                <div>
                  <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(log.responseStatus)}`}>
                    {log.responseStatus || 'N/A'}
                  </span>
                </div>
                <div>
                  <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                    (log.riskScore || 0) >= 70 ? 'text-red-700 bg-red-100' :
                    (log.riskScore || 0) >= 40 ? 'text-yellow-700 bg-yellow-100' :
                    (log.riskScore || 0) > 0 ? 'text-blue-700 bg-blue-100' :
                    'text-gray-700 bg-gray-100'
                  }`}>
                    {log.riskScore || 0}
                  </span>
                </div>
                <div>
                  {log.flaggedForReview ? (
                    <span className="px-2 py-1 rounded-full text-xs font-medium text-red-700 bg-red-100">
                      Flagged
                    </span>
                  ) : (
                    <span className="px-2 py-1 rounded-full text-xs font-medium text-gray-700 bg-gray-100">
                      Normal
                    </span>
                  )}
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      {logs.length > 0 && (
        <div className="p-6 border-t border-gray-200">
          <Pagination
            currentPage={currentPage + 1}
            totalPages={totalPages}
            onPageChange={(page) => setCurrentPage(page - 1)}
          />
          
          <div className="mt-4 flex items-center justify-between">
            <div className="text-sm text-gray-700">
              Showing {logs.length} of {totalElements} logs
            </div>
            <select
              value={pageSize}
              onChange={(e) => setPageSize(Number(e.target.value))}
              className="px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value={5}>5 per page</option>
              <option value={10}>10 per page</option>
              <option value={20}>20 per page</option>
              <option value={50}>50 per page</option>
            </select>
          </div>
        </div>
      )}
    </div>
  );
};

export default UserLogsList;