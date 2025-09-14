interface Log {
  id: string;
  user: string;
  address: string;
  date: string;
  type: "POST" | "GET" | "DELETE";
  status: number;
}

interface LogEntryProps {
  log: Log;
}

const LogEntry = ({ log }: LogEntryProps) => {
  const getTypeColor = (type: string) => {
    switch (type) {
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

  return (
    <div className="px-6 py-4 hover:bg-gray-50 transition-colors duration-200">
      <div className="grid grid-cols-6 gap-4 items-center text-sm">
        <div className="text-gray-900 font-medium">{log.id}</div>
        <div className="text-gray-900">{log.user}</div>
        <div className="text-gray-600 font-mono text-xs">{log.address}</div>
        <div className="text-gray-600">{log.date}</div>
        <div>
          <span className={`px-2 py-1 rounded-full text-xs font-medium ${getTypeColor(log.type)}`}>
            {log.type}
          </span>
        </div>
        <div>
          <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(log.status)}`}>
            {log.status}
          </span>
        </div>
      </div>
    </div>
  );
};

export default LogEntry;