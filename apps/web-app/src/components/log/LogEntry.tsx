interface Log {
  id: string;
  userId: string;
  endpoint: string;
  timestamp: string;
  method: "POST" | "GET" | "DELETE" | "PUT" | "PATCH";
  statusCode: number;
  flagged?: boolean;
}

interface LogEntryProps {
  log: Log;
}

const LogEntry = ({ log }: LogEntryProps) => {
  const getTypeColor = (method: string) => {
    switch (method) {
      case "POST":
        return "text-green-700 bg-green-100";
      case "GET":
        return "text-blue-700 bg-blue-100";
      case "DELETE":
        return "text-red-700 bg-red-100";
      default:
        return "text-gray-700 bg-gray-100";
    }
  };

  const getStatusColor = (status: number) => {
    if (status >= 200 && status < 300) {
      return "text-green-700 bg-green-100";
    } else if (status >= 400 && status < 500) {
      return "text-red-700 bg-red-100";
    } else if (status >= 500) {
      return "text-purple-700 bg-purple-100";
    }
    return "text-gray-700 bg-gray-100";
  };

  const formatDate = (timestamp: string) => {
    return new Date(timestamp).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    });
  };

  return (
    <div className="px-6 py-4 hover:bg-gray-50 transition-colors duration-200">
      <div className="grid grid-cols-6 gap-4 items-center text-sm">
        <div className="text-gray-900 font-mono text-xs">{log.id.slice(0, 8)}...</div>
        <div className="text-gray-900 font-mono text-xs">{log.userId || 'N/A'}</div>
        <div className="text-gray-600 font-mono text-xs truncate" title={log.endpoint}>
          {log.endpoint}
        </div>
        <div className="text-gray-600 text-xs">{formatDate(log.timestamp)}</div>
        <div>
          <span className={`px-2 py-1 rounded-full text-xs font-medium ${getTypeColor(log.method)}`}>
            {log.method}
          </span>
        </div>
        <div>
          <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(log.statusCode)}`}>
            {log.statusCode}
          </span>
        </div>
      </div>
    </div>
  );
};

export default LogEntry;