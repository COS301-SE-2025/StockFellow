import { useState } from "react";
import LogEntry from "./LogEntry";
import Pagination from "../common/Pagination";

interface Log {
  id: string;
  user: string;
  address: string;
  date: string;
  type: "POST" | "GET" | "DELETE";
  status: number;
}

const UserLogsList = () => {
  const [currentPage, setCurrentPage] = useState(1);

  // Mock data - replace with actual data from backend
  const logs: Log[] = [
    {
      id: "00001",
      user: "Obito Uchiha",
      address: "/users/create",
      date: "14 Feb 2025",
      type: "POST",
      status: 200
    },
    {
      id: "00002", 
      user: "Sasuke Uchiha",
      address: "/users/id",
      date: "14 June 2025",
      type: "GET",
      status: 500
    },
    {
      id: "00003",
      user: "Jon Snow",
      address: "/groups/id",
      date: "16 March 2025",
      type: "DELETE",
      status: 401
    },
    {
      id: "00004",
      user: "Reed Richards",
      address: "/transaction/getcycle",
      date: "15 Feb 2025",
      type: "GET",
      status: 200
    },
    {
      id: "00005",
      user: "Son Goku",
      address: "/transaction/pay",
      date: "14 Feb 2025",
      type: "POST",
      status: 500
    },
    {
      id: "00006",
      user: "Monkey D. Luffy",
      address: "/auth/login",
      date: "14 Feb 2025",
      type: "POST",
      status: 201
    }
  ];

  return (
    <div className="bg-white rounded-lg shadow-sm">
      <div className="p-6 border-b border-gray-200">
        <h1 className="text-2xl font-semibold text-gray-900">User Logs</h1>
      </div>
      
      {/* Table Header */}
      <div className="bg-gray-50 px-6 py-3 border-b border-gray-200">
        <div className="grid grid-cols-6 gap-4 text-xs font-medium text-gray-500 uppercase tracking-wider">
          <div>ID</div>
          <div>USER</div>
          <div>ADDRESS</div>
          <div>DATE</div>
          <div>TYPE</div>
          <div>STATUS</div>
        </div>
      </div>

      {/* Table Body */}
      <div className="divide-y divide-gray-200">
        {logs.map((log) => (
          <LogEntry key={log.id} log={log} />
        ))}
      </div>

      <div className="p-6 border-t border-gray-200">
        <Pagination
          currentPage={currentPage}
          totalPages={4}
          onPageChange={setCurrentPage}
        />
      </div>
    </div>
  );
};

export default UserLogsList;