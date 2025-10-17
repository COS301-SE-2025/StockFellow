import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../contexts/AuthContext";
import { adminService } from "../../services/adminService";
import { AlertTriangle, CheckCircle, XCircle, Clock, User, FileText } from 'lucide-react';

interface RequestDetails {
  request: {
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
  };
  user?: {
    userId: string;
    email: string;
    firstName?: string;
    lastName?: string;
    phoneNumber?: string;
    // Add other user fields as needed
  };
  group?: {
    groupId: string;
    name: string;
    // Add other group fields as needed
  };
  userError?: string;
  groupError?: string;
}

interface UserRequestFormProps {
  requestId?: string;
}

const UserRequestForm = ({ requestId }: UserRequestFormProps) => {
  const navigate = useNavigate();
  const { isAuthenticated, isLoading: authLoading } = useAuth();
  const [isLoading, setIsLoading] = useState(false);
  const [actionLoading, setActionLoading] = useState<'approve' | 'reject' | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [requestDetails, setRequestDetails] = useState<RequestDetails | null>(null);
  const [adminNotes, setAdminNotes] = useState("");

  useEffect(() => {
    if (!authLoading && isAuthenticated && requestId) {
      fetchRequestDetails();
    }
  }, [requestId, isAuthenticated, authLoading]);

  const fetchRequestDetails = async () => {
    if (!requestId) return;

    try {
      setIsLoading(true);
      setError(null);

      console.log('Fetching request details for:', requestId);
      const details = await adminService.getRequestDetails(requestId);
      console.log('Request details response:', details);
      
      setRequestDetails(details);
    } catch (err: any) {
      console.error('Error fetching request details:', err);
      
      if (err.response?.status === 404) {
        setError("Request not found. It may have been deleted or the ID is invalid.");
      } else if (err.response?.status === 401) {
        setError("Access denied. Your session may have expired. Please log in again.");
      } else if (err.response?.status === 403) {
        setError("You don't have permission to view this request.");
      } else if (err.message === 'Authentication required') {
        setError("Authentication required. Please log in again.");
      } else {
        setError(err.message || "Failed to fetch request details");
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleApprove = async () => {
    if (!requestId || !requestDetails) return;

    try {
      setActionLoading('approve');
      setError(null);

      console.log('Approving request:', requestId, 'with notes:', adminNotes);
      
      const response = await adminService.approveRequest(requestId, adminNotes || "Request approved by admin");
      console.log('Approval response:', response);

      // Refresh the request details to show updated status
      await fetchRequestDetails();
      
      // Show success message (you might want to use a toast here)
      console.log('Request approved successfully');
      
      // Navigate back after a short delay
      setTimeout(() => {
        navigate("/requests");
      }, 2000);
      
    } catch (err: any) {
      console.error('Error approving request:', err);
      setError(err.message || "Failed to approve request");
    } finally {
      setActionLoading(null);
    }
  };

  const handleReject = async () => {
    if (!requestId || !requestDetails) return;

    // Require notes for rejection
    if (!adminNotes.trim()) {
      setError("Please provide a reason for rejecting this request");
      return;
    }

    try {
      setActionLoading('reject');
      setError(null);

      console.log('Rejecting request:', requestId, 'with notes:', adminNotes);
      
      const response = await adminService.rejectRequest(requestId, adminNotes);
      console.log('Rejection response:', response);

      // Refresh the request details to show updated status
      await fetchRequestDetails();
      
      // Show success message
      console.log('Request rejected successfully');
      
      // Navigate back after a short delay
      setTimeout(() => {
        navigate("/requests");
      }, 2000);
      
    } catch (err: any) {
      console.error('Error rejecting request:', err);
      setError(err.message || "Failed to reject request");
    } finally {
      setActionLoading(null);
    }
  };

  const getRequestTypeDisplay = (type: string): string => {
    switch (type) {
      case "LEAVE_GROUP":
        return "Leave Group Request";
      case "JOIN_GROUP":
        return "Join Group Request";
      case "TRANSACTION_DISPUTE":
        return "Transaction Dispute";
      case "DELETE_CARD":
        return "Delete Card Request";
      case "CLOSE_ACCOUNT":
        return "Close Account Request";
      default:
        return type.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, l => l.toUpperCase());
    }
  };

  const getStatusDisplay = (status: string) => {
    switch (status) {
      case "PENDING":
        return { text: "Pending Review", color: "text-yellow-600 bg-yellow-100", icon: Clock };
      case "APPROVED":
        return { text: "Approved", color: "text-green-600 bg-green-100", icon: CheckCircle };
      case "COMPLETED":
        return { text: "Completed", color: "text-green-600 bg-green-100", icon: CheckCircle };
      case "REJECTED":
        return { text: "Rejected", color: "text-red-600 bg-red-100", icon: XCircle };
      default:
        return { text: status, color: "text-gray-600 bg-gray-100", icon: Clock };
    }
  };

  const formatDate = (dateString: string): string => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  // Show loading while checking authentication
  if (authLoading) {
    return (
      <div className="bg-white rounded-lg shadow-sm">
        <div className="p-6 border-b border-gray-200">
          <h1 className="text-2xl font-semibold text-gray-900">Request Details</h1>
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
          <h1 className="text-2xl font-semibold text-gray-900">Request Details</h1>
        </div>
        <div className="p-8 text-center">
          <div className="bg-yellow-50 border-l-4 border-yellow-400 p-4">
            <p className="text-sm text-yellow-700">
              Authentication required to view request details. Please log in with admin credentials.
            </p>
          </div>
        </div>
      </div>
    );
  }

  if (isLoading && !requestDetails) {
    return (
      <div className="bg-white rounded-lg shadow-sm">
        <div className="p-6 border-b border-gray-200">
          <h1 className="text-2xl font-semibold text-gray-900">Request Details</h1>
        </div>
        <div className="p-8 text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading request details...</p>
        </div>
      </div>
    );
  }

  if (error && !requestDetails) {
    return (
      <div className="bg-white rounded-lg shadow-sm">
        <div className="p-6 border-b border-gray-200">
          <h1 className="text-2xl font-semibold text-gray-900">Request Details</h1>
        </div>
        <div className="p-8 text-center">
          <div className="bg-red-50 border-l-4 border-red-400 p-4 max-w-md mx-auto">
            <div className="flex">
              <div className="flex-shrink-0">
                <AlertTriangle className="h-5 w-5 text-red-400" />
              </div>
              <div className="ml-3">
                <p className="text-sm text-red-700">{error}</p>
                <div className="mt-4">
                  <button
                    onClick={() => navigate("/requests")}
                    className="text-sm bg-red-100 text-red-700 px-3 py-1 rounded hover:bg-red-200 mr-2"
                  >
                    Back to Requests
                  </button>
                  <button
                    onClick={fetchRequestDetails}
                    className="text-sm bg-blue-100 text-blue-700 px-3 py-1 rounded hover:bg-blue-200"
                  >
                    Retry
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (!requestDetails) {
    return (
      <div className="bg-white rounded-lg shadow-sm">
        <div className="p-6 border-b border-gray-200">
          <h1 className="text-2xl font-semibold text-gray-900">Request Details</h1>
        </div>
        <div className="p-8 text-center">
          <FileText className="h-12 w-12 text-gray-400 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">Request Not Found</h3>
          <p className="text-gray-600">The requested details could not be loaded.</p>
        </div>
      </div>
    );
  }

  const request = requestDetails.request;
  const user = requestDetails.user;
  const group = requestDetails.group;
  const statusInfo = getStatusDisplay(request.status);
  const StatusIcon = statusInfo.icon;

  return (
    <div className="bg-white rounded-lg shadow-sm">
      <div className="p-6 border-b border-gray-200">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-semibold text-gray-900">Request Details</h1>
            <p className="text-gray-600 mt-1">Request ID: {request.requestId}</p>
          </div>
          <div className={`flex items-center px-3 py-1 rounded-full text-sm font-medium ${statusInfo.color}`}>
            <StatusIcon className="h-4 w-4 mr-1" />
            {statusInfo.text}
          </div>
        </div>
      </div>

      <div className="p-6">
        {error && (
          <div className="bg-red-50 border-l-4 border-red-400 p-4 mb-6">
            <div className="flex">
              <div className="flex-shrink-0">
                <AlertTriangle className="h-5 w-5 text-red-400" />
              </div>
              <div className="ml-3">
                <p className="text-sm text-red-700">{error}</p>
              </div>
            </div>
          </div>
        )}

        {/* Request Information */}
        <div className="max-w-4xl mx-auto space-y-8">
          {/* Basic Request Info */}
          <div className="bg-gray-50 rounded-lg p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Request Information</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Request Type</label>
                <div className="text-gray-900">{getRequestTypeDisplay(request.requestType)}</div>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">Submitted On</label>
                <div className="text-gray-900">{formatDate(request.createdAt)}</div>
              </div>
              <div className="md:col-span-2">
                <label className="block text-sm font-medium text-gray-700 mb-2">Reason</label>
                <div className="bg-white p-3 rounded border text-gray-900">
                  {request.reason || "No reason provided"}
                </div>
              </div>
            </div>
          </div>

          {/* User Information */}
          <div className="bg-gray-50 rounded-lg p-6">
            <h2 className="text-lg font-semibold text-gray-900 mb-4">User Information</h2>
            {requestDetails.userError ? (
              <div className="text-red-600 text-sm">
                Failed to load user details: {requestDetails.userError}
              </div>
            ) : (
              <div className="flex items-start space-x-4">
                <div className="flex-shrink-0">
                  <div className="w-12 h-12 bg-blue-500 rounded-full flex items-center justify-center">
                    <User className="h-6 w-6 text-white" />
                  </div>
                </div>
                <div className="flex-1 grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">User ID</label>
                    <div className="text-gray-900 font-mono">{request.userId}</div>
                  </div>
                  {user?.email && (
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
                      <div className="text-gray-900">{user.email}</div>
                    </div>
                  )}
                  {user?.firstName && (
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Name</label>
                      <div className="text-gray-900">
                        {user.firstName} {user.lastName || ''}
                      </div>
                    </div>
                  )}
                  {user?.phoneNumber && (
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Phone</label>
                      <div className="text-gray-900">{user.phoneNumber}</div>
                    </div>
                  )}
                </div>
              </div>
            )}
          </div>

          {/* Group Information (if applicable) */}
          {request.groupId && (
            <div className="bg-gray-50 rounded-lg p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">Group Information</h2>
              {requestDetails.groupError ? (
                <div className="text-red-600 text-sm">
                  Failed to load group details: {requestDetails.groupError}
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Group ID</label>
                    <div className="text-gray-900 font-mono">{request.groupId}</div>
                  </div>
                  {group?.name && (
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Group Name</label>
                      <div className="text-gray-900">{group.name}</div>
                    </div>
                  )}
                </div>
              )}
            </div>
          )}

          {/* Card Information (if applicable) */}
          {request.cardId && (
            <div className="bg-gray-50 rounded-lg p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">Card Information</h2>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Card ID</label>
                <div className="text-gray-900 font-mono">{request.cardId}</div>
              </div>
            </div>
          )}

          {/* Admin Notes Section */}
          {request.status === "PENDING" && (
            <div className="bg-blue-50 rounded-lg p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">Admin Review</h2>
              <div>
                <label htmlFor="adminNotes" className="block text-sm font-medium text-gray-700 mb-2">
                  Admin Notes {request.status === "PENDING" && actionLoading === 'reject' && (
                    <span className="text-red-600">*</span>
                  )}
                </label>
                <textarea
                  id="adminNotes"
                  value={adminNotes}
                  onChange={(e) => setAdminNotes(e.target.value)}
                  placeholder="Add notes about this request (required for rejection)..."
                  rows={4}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                {actionLoading === 'reject' && !adminNotes.trim() && (
                  <p className="text-red-600 text-sm mt-1">Admin notes are required when rejecting a request</p>
                )}
              </div>
            </div>
          )}

          {/* Processing Information (for processed requests) */}
          {request.status !== "PENDING" && (
            <div className="bg-gray-50 rounded-lg p-6">
              <h2 className="text-lg font-semibold text-gray-900 mb-4">Processing Information</h2>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {request.processedAt && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Processed On</label>
                    <div className="text-gray-900">{formatDate(request.processedAt)}</div>
                  </div>
                )}
                {request.adminUserId && (
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">Processed By</label>
                    <div className="text-gray-900 font-mono">{request.adminUserId}</div>
                  </div>
                )}
                {request.adminNotes && (
                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-2">Admin Notes</label>
                    <div className="bg-white p-3 rounded border text-gray-900">
                      {request.adminNotes}
                    </div>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* Action Buttons */}
          <div className="flex justify-center space-x-4 pt-6">
            <button
              onClick={() => navigate("/requests")}
              className="px-6 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300 focus:outline-none focus:ring-2 focus:ring-gray-500"
            >
              Back to Requests
            </button>
            
            {request.status === "PENDING" && (
              <>
                <button
                  onClick={handleApprove}
                  disabled={actionLoading !== null}
                  className="px-6 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-500 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {actionLoading === 'approve' ? (
                    <div className="flex items-center">
                      <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                      </svg>
                      Approving...
                    </div>
                  ) : (
                    "Approve Request"
                  )}
                </button>
                
                <button
                  onClick={handleReject}
                  disabled={actionLoading !== null}
                  className="px-6 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {actionLoading === 'reject' ? (
                    <div className="flex items-center">
                      <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                      </svg>
                      Rejecting...
                    </div>
                  ) : (
                    "Reject Request"
                  )}
                </button>
              </>
            )}
          </div>

          {/* Success Messages */}
          {request.status === "APPROVED" && request.processedAt && (
            <div className="bg-green-50 border-l-4 border-green-400 p-4 mt-6">
              <div className="flex">
                <div className="flex-shrink-0">
                  <CheckCircle className="h-5 w-5 text-green-400" />
                </div>
                <div className="ml-3">
                  <p className="text-sm text-green-700">
                    This request has been approved and processed successfully.
                  </p>
                </div>
              </div>
            </div>
          )}

          {request.status === "REJECTED" && request.processedAt && (
            <div className="bg-red-50 border-l-4 border-red-400 p-4 mt-6">
              <div className="flex">
                <div className="flex-shrink-0">
                  <XCircle className="h-5 w-5 text-red-400" />
                </div>
                <div className="ml-3">
                  <p className="text-sm text-red-700">
                    This request has been rejected.
                  </p>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default UserRequestForm;