interface Request {
  id: string;
  name: string;
  role: string;
  status: "Approved" | "Pending" | "Declined";
  date: string;
}

interface RequestCardProps {
  request: Request;
  onView: () => void;
}

const RequestCard = ({ request, onView }: RequestCardProps) => {
  const getStatusColor = (status: string) => {
    switch (status) {
      case "Approved":
        return "text-green-600 bg-green-50";
      case "Pending":
        return "text-blue-600 bg-blue-50";
      case "Declined":
        return "text-red-600 bg-red-50";
      default:
        return "text-gray-600 bg-gray-50";
    }
  };

  return (
    <div className="p-6 hover:bg-gray-50 transition-colors duration-200">
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-4">
          {/* Avatar */}
          <div className="w-10 h-10 bg-blue-500 rounded-full flex items-center justify-center">
            <svg className="w-6 h-6 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
            </svg>
          </div>
          
          {/* User Info */}
          <div>
            <h3 className="text-lg font-medium text-gray-900">{request.name}</h3>
            <p className="text-sm text-gray-500">{request.role}</p>
          </div>
        </div>

        <div className="flex items-center space-x-6">
          {/* Status */}
          <span className={`px-3 py-1 rounded-full text-sm font-medium ${getStatusColor(request.status)}`}>
            {request.status}
          </span>
          
          {/* Date */}
          <span className="text-sm text-gray-500">{request.date}</span>
          
          {/* View Button */}
          <button
            onClick={onView}
            className="bg-gray-900 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-gray-800 transition-colors duration-200"
          >
            View
          </button>
        </div>
      </div>
    </div>
  );
};

export default RequestCard;