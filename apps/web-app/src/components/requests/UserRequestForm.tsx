import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";

interface UserRequestData {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
  username: string;
  phoneNumber: string;
  gender: "Male" | "Female" | "Other";
  requestType: "User" | "Stokvel";
  status: "Pending" | "Approved" | "Declined";
  dateSubmitted: string;
}

interface UserRequestFormProps {
  requestId?: string;
}

const UserRequestForm = ({ requestId }: UserRequestFormProps) => {
  const navigate = useNavigate();
  const [isLoading, setIsLoading] = useState(false);
  
  // Mock data - replace with API call based on requestId
  const [requestData, setRequestData] = useState<UserRequestData>({
    id: requestId || "1",
    firstName: "Kevin",
    lastName: "Fleming",
    email: "jaskolski.brent@yahoo.com",
    username: "jaskolski",
    phoneNumber: "073 345 3388",
    gender: "Female",
    requestType: "User",
    status: "Pending",
    dateSubmitted: "17 August 2025"
  });

  useEffect(() => {
    // Simulate API call to fetch request details
    if (requestId) {
      setIsLoading(true);
      // Replace with actual API call
      setTimeout(() => {
        setIsLoading(false);
      }, 500);
    }
  }, [requestId]);

  const handleApprove = async () => {
    setIsLoading(true);
    try {
      // API call to approve request
      console.log("Approving request:", requestId);
      // Simulate API delay
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      // Update local state
      setRequestData(prev => ({ ...prev, status: "Approved" }));
      
      // Navigate back to requests page
      navigate("/requests");
    } catch (error) {
      console.error("Error approving request:", error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDecline = async () => {
    setIsLoading(true);
    try {
      // API call to decline request
      console.log("Declining request:", requestId);
      // Simulate API delay
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      // Update local state
      setRequestData(prev => ({ ...prev, status: "Declined" }));
      
      // Navigate back to requests page
      navigate("/requests");
    } catch (error) {
      console.error("Error declining request:", error);
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading && !requestData.firstName) {
    return (
      <div className="bg-white rounded-lg shadow-sm p-6">
        <div className="animate-pulse">
          <div className="h-8 bg-gray-200 rounded w-64 mb-8"></div>
          <div className="space-y-6">
            <div className="h-4 bg-gray-200 rounded w-32"></div>
            <div className="h-10 bg-gray-200 rounded"></div>
            <div className="h-4 bg-gray-200 rounded w-32"></div>
            <div className="h-10 bg-gray-200 rounded"></div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-sm">
      <div className="p-6 border-b border-gray-200">
        <h1 className="text-2xl font-semibold text-gray-900">Stokvels Details</h1>
      </div>

      <div className="p-6">
        {/* Profile Avatar Section */}
        <div className="flex justify-center mb-8">
          <div className="w-16 h-16 bg-blue-500 rounded-full flex items-center justify-center">
            <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
            </svg>
          </div>
        </div>

        {/* Form Fields */}
        <div className="max-w-2xl mx-auto space-y-6">
          {/* First Name and Last Name */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label htmlFor="firstName" className="block text-sm font-medium text-gray-700 mb-2">
                First Name
              </label>
              <input
                type="text"
                id="firstName"
                value={requestData.firstName}
                readOnly
                className="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50 text-gray-900 focus:outline-none"
              />
            </div>
            <div>
              <label htmlFor="lastName" className="block text-sm font-medium text-gray-700 mb-2">
                Last Name
              </label>
              <input
                type="text"
                id="lastName"
                value={requestData.lastName}
                readOnly
                className="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50 text-gray-900 focus:outline-none"
              />
            </div>
          </div>

          {/* Email and Username */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-2">
                Your email
              </label>
              <input
                type="email"
                id="email"
                value={requestData.email}
                readOnly
                className="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50 text-gray-900 focus:outline-none"
              />
            </div>
            <div>
              <label htmlFor="username" className="block text-sm font-medium text-gray-700 mb-2">
                Username
              </label>
              <input
                type="text"
                id="username"
                value={requestData.username}
                readOnly
                className="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50 text-gray-900 focus:outline-none"
              />
            </div>
          </div>

          {/* Phone Number and Gender */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label htmlFor="phoneNumber" className="block text-sm font-medium text-gray-700 mb-2">
                Phone Number
              </label>
              <input
                type="tel"
                id="phoneNumber"
                value={requestData.phoneNumber}
                readOnly
                className="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50 text-gray-900 focus:outline-none"
              />
            </div>
            <div>
              <label htmlFor="gender" className="block text-sm font-medium text-gray-700 mb-2">
                Gender
              </label>
              <div className="relative">
                <select
                  id="gender"
                  value={requestData.gender}
                  disabled
                  className="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50 text-gray-900 focus:outline-none appearance-none cursor-not-allowed"
                >
                  <option value="Female">Female</option>
                  <option value="Male">Male</option>
                  <option value="Other">Other</option>
                </select>
                <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-gray-700">
                  <svg className="fill-current h-4 w-4" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20">
                    <path d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" />
                  </svg>
                </div>
              </div>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex justify-center space-x-4 pt-6">
            <button
              onClick={handleApprove}
              disabled={isLoading || requestData.status !== "Pending"}
              className={`px-8 py-3 rounded-lg text-sm font-medium transition-colors duration-200 ${
                requestData.status !== "Pending"
                  ? "bg-gray-300 text-gray-500 cursor-not-allowed"
                  : "bg-blue-600 text-white hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
              }`}
            >
              {isLoading ? (
                <div className="flex items-center">
                  <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  Processing...
                </div>
              ) : (
                "Approve"
              )}
            </button>
            
            <button
              onClick={handleDecline}
              disabled={isLoading || requestData.status !== "Pending"}
              className={`px-8 py-3 rounded-lg text-sm font-medium transition-colors duration-200 ${
                requestData.status !== "Pending"
                  ? "bg-gray-300 text-gray-500 cursor-not-allowed"
                  : "bg-gray-900 text-white hover:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2"
              }`}
            >
              {isLoading ? (
                <div className="flex items-center">
                  <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  Processing...
                </div>
              ) : (
                "Decline"
              )}
            </button>
          </div>

          {/* Status Display */}
          {requestData.status !== "Pending" && (
            <div className="mt-6 p-4 rounded-lg bg-gray-50 text-center">
              <p className="text-sm text-gray-600">
                Request Status: 
                <span className={`ml-2 px-3 py-1 rounded-full text-sm font-medium ${
                  requestData.status === "Approved" 
                    ? "bg-green-100 text-green-800" 
                    : "bg-red-100 text-red-800"
                }`}>
                  {requestData.status}
                </span>
              </p>
              <p className="text-xs text-gray-500 mt-1">
                Submitted on {requestData.dateSubmitted}
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default UserRequestForm;