import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import RequestCard from "./RequestCard";
import Pagination from "../common/Pagination";
import { useAuth } from "../../contexts/AuthContext";
import { adminService } from "../../services/adminService";
import { AlertTriangle, RefreshCw } from 'lucide-react';

interface AdminRequest {
  requestId: string;
  userId: string;
  requestType: string;
  status: "PENDING" | "APPROVED" | "REJECTED" | "COMPLETED";
  reason: string;
  groupId?: string;
  cardId?: string;
  createdAt: string;
  processedAt?: string;
  adminUserId?: string;
  adminNotes?: string;
}

// interface PaginatedResponse {
//   content: AdminRequest[];
//   totalElements: number;
//   totalPages: number;
//   size: number;
//   number: number;
//   first: boolean;
//   last: boolean;
//   numberOfElements: number;
//   empty: boolean;
// }

// Transform backend request to frontend format
const transformRequestForCard = (request: AdminRequest) => ({
  id: request.requestId,
  name: `User ${request.userId.substring(0, 8)}...`, // Show truncated user ID since we don't have user details here
  role: getRequestTypeDisplay(request.requestType),
  status: request.status === "COMPLETED" ? "Approved" as const : 
          request.status === "APPROVED" ? "Approved" as const :
          request.status === "REJECTED" ? "Declined" as const :
          "Pending" as const,
  date: formatDate(request.createdAt)
});

const getRequestTypeDisplay = (type: string): string => {
  switch (type) {
    case "LEAVE_GROUP":
      return "Leave Group";
    case "JOIN_GROUP":
      return "Join Group";
    case "TRANSACTION_DISPUTE":
      return "Transaction Dispute";
    case "DELETE_CARD":
      return "Delete Card";
    case "CLOSE_ACCOUNT":
      return "Close Account";
    default:
      return type.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase());
  }
};

const formatDate = (dateString: string): string => {
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
};

const RequestsList = () => {
  const navigate = useNavigate();
  const { isAuthenticated, isLoading: authLoading } = useAuth();
  
  // State management
  const [filter, setFilter] = useState<"All" | "PENDING" | "APPROVED" | "REJECTED" | "COMPLETED">("All");
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize] = useState(10); // Fixed page size
  const [requests, setRequests] = useState<AdminRequest[]>([]);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isRefreshing, setIsRefreshing] = useState(false);

  // Fetch requests whenever dependencies change
  useEffect(() => {
    if (!authLoading && isAuthenticated) {
      fetchRequests();
    }
  }, [currentPage, filter, isAuthenticated, authLoading]);

  const fetchRequests = async (showRefreshIndicator = false) => {
    try {
      if (showRefreshIndicator) {
        setIsRefreshing(true);
      } else {
        setIsLoading(true);
      }
      setError(null);

      console.log('Fetching requests with params:', {
        page: currentPage - 1, // Backend uses 0-based indexing
        size: pageSize,
        requestType: filter === "All" ? undefined : filter
      });

      const response = await adminService.getPendingRequests({
        page: currentPage - 1, // Convert to 0-based indexing for backend
        size: pageSize,
        requestType: filter === "All" ? undefined : filter
      });

      console.log('Requests response:', response);

      setRequests(response.content || []);
      setTotalElements(response.totalElements || 0);
      setTotalPages(response.totalPages || 0);
      
    } catch (err: any) {
      console.error('Error fetching requests:', err);
      
      if (err.message === 'Authentication required') {
        setError("Authentication required. Please log in again.");
      } else if (err.response?.status === 401) {
        setError("Your session has expired. Please log in again.");
      } else if (err.response?.status === 403) {
        setError("You don't have permission to view requests.");
      } else if (err.response?.status >= 500) {
        setError("Server error. Please try again later.");
      } else {
        setError(err.message || "Failed to fetch requests");
      }
      
      // Reset data on error
      setRequests([]);
      setTotalElements(0);
      setTotalPages(0);
    } finally {
      setIsLoading(false);
      setIsRefreshing(false);
    }
  };

  const handleFilterChange = (newFilter: typeof filter) => {
    setFilter(newFilter);
    setCurrentPage(1); // Reset to first page when filter changes
  };

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const handleView = (requestId: string) => {
    console.log("Navigating to request details:", requestId);
    navigate(`/requests/${requestId}`);
  };

  const handleRefresh = () => {
    fetchRequests(true);
  };

  // Show loading while checking authentication
  if (authLoading) {
    return (
      <div className="bg-white rounded-lg shadow-sm">
        <div className="p-6 border-b border-gray-200">
          <h1 className="text-2xl font-semibold text-gray-900">Edit Requests</h1>
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
          <h1 className="text-2xl font-semibold text-gray-900">Edit Requests</h1>
        </div>
        <div className="p-8 text-center">
          <div className="bg-yellow-50 border-l-4 border-yellow-400 p-4 max-w-md mx-auto">
            <p className="text-sm text-yellow-700">
              Authentication required to view requests. Please log in with admin credentials.
            </p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-sm">
      <div className="p-6 border-b border-gray-200">
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-2xl font-semibold text-gray-900">Edit Requests</h1>
            {totalElements > 0 && (
              <p className="text-sm text-gray-600 mt-1">
                Showing {requests.length} of {totalElements} requests
              </p>
            )}
          </div>
          <div className="flex items-center space-x-4">
            {/* Refresh Button */}
            <button
              onClick={handleRefresh}
              disabled={isLoading || isRefreshing}
              className="flex items-center space-x-2 px-3 py-2 text-sm bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200 focus:outline-none focus:ring-2 focus:ring-gray-500 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <RefreshCw className={`h-4 w-4 ${isRefreshing ? 'animate-spin' : ''}`} />
              <span>Refresh</span>
            </button>

            {/* Filter Dropdown */}
            <div className="relative">
              <select 
                value={filter}
                onChange={(e) => handleFilterChange(e.target.value as typeof filter)}
                disabled={isLoading}
                className="appearance-none bg-white border border-gray-300 rounded-md py-2 pl-3 pr-10 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <option value="All">All Requests</option>
                <option value="PENDING">Pending</option>
                <option value="APPROVED">Approved</option>
                <option value="COMPLETED">Completed</option>
                <option value="REJECTED">Rejected</option>
              </select>
              <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-gray-700">
                <svg className="fill-current h-4 w-4" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20">
                  <path d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" />
                </svg>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Error Display */}
      {error && (
        <div className="p-6 border-b border-gray-200">
          <div className="bg-red-50 border-l-4 border-red-400 p-4">
            <div className="flex">
              <div className="flex-shrink-0">
                <AlertTriangle className="h-5 w-5 text-red-400" />
              </div>
              <div className="ml-3">
                <p className="text-sm text-red-700">{error}</p>
                <div className="mt-2">
                  <button
                    onClick={() => fetchRequests()}
                    className="text-sm bg-red-100 text-red-700 px-3 py-1 rounded hover:bg-red-200"
                  >
                    Retry
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Loading State */}
      {isLoading && requests.length === 0 && !error && (
        <div className="p-8 text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading requests...</p>
        </div>
      )}

      {/* Empty State */}
      {!isLoading && !error && requests.length === 0 && (
        <div className="p-8 text-center">
          <div className="text-gray-400 mb-4">
            <svg className="mx-auto h-12 w-12" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
          </div>
          <h3 className="text-lg font-medium text-gray-900 mb-2">No Requests Found</h3>
          <p className="text-gray-600 mb-4">
            {filter === "All" 
              ? "There are currently no requests in the system."
              : `There are no ${filter.toLowerCase()} requests at this time.`
            }
          </p>
          <button
            onClick={handleRefresh}
            className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            Refresh
          </button>
        </div>
      )}

      {/* Requests List */}
      {!isLoading && !error && requests.length > 0 && (
        <>
          <div className="divide-y divide-gray-200">
            {requests.map((request) => (
              <RequestCard
                key={request.requestId}
                request={transformRequestForCard(request)}
                onView={() => handleView(request.requestId)}
              />
            ))}
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div className="p-6 border-t border-gray-200">
              <Pagination
                currentPage={currentPage}
                totalPages={totalPages}
                onPageChange={handlePageChange}
              />
            </div>
          )}
        </>
      )}

      {/* Loading overlay for refresh */}
      {isRefreshing && requests.length > 0 && (
        <div className="absolute inset-0 bg-white bg-opacity-50 flex items-center justify-center">
          <div className="bg-white rounded-lg shadow-lg p-4 flex items-center space-x-3">
            <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-blue-600"></div>
            <span className="text-gray-700">Refreshing...</span>
          </div>
        </div>
      )}
    </div>
  );
};

export default RequestsList;