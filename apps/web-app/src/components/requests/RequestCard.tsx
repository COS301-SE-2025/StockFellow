import { Clock, CheckCircle, XCircle, User, Calendar } from 'lucide-react';

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
  const getStatusConfig = (status: string) => {
    switch (status) {
      case "Approved":
        return {
          color: "text-green-700 bg-green-100",
          icon: CheckCircle,
          text: "Approved"
        };
      case "Pending":
        return {
          color: "text-blue-700 bg-blue-100",
          icon: Clock,
          text: "Pending Review"
        };
      case "Declined":
        return {
          color: "text-red-700 bg-red-100",
          icon: XCircle,
          text: "Declined"
        };
      default:
        return {
          color: "text-gray-700 bg-gray-100",
          icon: Clock,
          text: status
        };
    }
  };

  const statusConfig = getStatusConfig(request.status);
  const StatusIcon = statusConfig.icon;

  return (
    <div className="p-6 hover:bg-gray-50 transition-colors duration-200 border-l-4 border-transparent hover:border-blue-500">
      <div className="flex items-center justify-between">
        <div className="flex items-center space-x-4">
          {/* Avatar */}
          <div className="w-12 h-12 bg-gradient-to-br from-blue-500 to-blue-600 rounded-full flex items-center justify-center shadow-sm">
            <User className="w-6 h-6 text-white" />
          </div>
          
          {/* Request Info */}
          <div className="flex-1">
            <div className="flex items-center space-x-3">
              <h3 className="text-lg font-semibold text-gray-900">{request.name}</h3>
              <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${statusConfig.color}`}>
                <StatusIcon className="w-3 h-3 mr-1" />
                {statusConfig.text}
              </span>
            </div>
            <div className="flex items-center space-x-4 mt-1">
              <p className="text-sm font-medium text-blue-600 bg-blue-50 px-2 py-1 rounded">
                {request.role}
              </p>
              <div className="flex items-center text-sm text-gray-500">
                <Calendar className="w-4 h-4 mr-1" />
                {request.date}
              </div>
            </div>
          </div>
        </div>

        {/* Actions */}
        <div className="flex items-center space-x-3">
          <button
            onClick={onView}
            className="inline-flex items-center px-4 py-2 bg-gray-900 text-white text-sm font-medium rounded-md hover:bg-gray-800 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500 transition-colors duration-200"
          >
            <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
            </svg>
            View Details
          </button>
        </div>
      </div>

      {/* Additional request info for better context */}
      <div className="mt-4 ml-16">
        <div className="bg-gray-50 rounded-md p-3">
          <p className="text-sm text-gray-600">
            Request ID: <span className="font-mono text-gray-800">{request.id.substring(0, 16)}...</span>
          </p>
        </div>
      </div>
    </div>
  );
};

export default RequestCard;