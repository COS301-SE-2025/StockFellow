import { useState } from "react";
import RequestCard from "./RequestCard";
import Pagination from "../common/Pagination";

interface Request {
  id: string;
  name: string;
  role: string;
  status: "Approved" | "Pending" | "Declined";
  date: string;
}

const RequestsList = () => {
  const [filter, setFilter] = useState("All");
  const [currentPage, setCurrentPage] = useState(1);

  // Mock data - replace with actual data from backend
  const requests: Request[] = [
    {
      id: "1",
      name: "Obito Uchiha",
      role: "User",
      status: "Approved",
      date: "17 August 2025"
    },
    {
      id: "2", 
      name: "The Akatsuki",
      role: "Stokvel",
      status: "Pending",
      date: "17 August 2025"
    },
    {
      id: "3",
      name: "Son Goku",
      role: "User", 
      status: "Declined",
      date: "17 August 2025"
    }
  ];

  const filteredRequests = filter === "All" 
    ? requests 
    : requests.filter(request => request.status === filter);

  const handleView = (requestId: string) => {
    // Handle view request - this would navigate to a detailed view
    console.log("View request:", requestId);
  };

  return (
    <div className="bg-white rounded-lg shadow-sm">
      <div className="p-6 border-b border-gray-200">
        <div className="flex justify-between items-center">
          <h1 className="text-2xl font-semibold text-gray-900">Edit Requests</h1>
          <div className="relative">
            <select 
              value={filter}
              onChange={(e) => setFilter(e.target.value)}
              className="appearance-none bg-white border border-gray-300 rounded-md py-2 pl-3 pr-10 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
            >
              <option value="All">All</option>
              <option value="Approved">Approved</option>
              <option value="Pending">Pending</option>
              <option value="Declined">Declined</option>
            </select>
            <div className="pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-gray-700">
              <svg className="fill-current h-4 w-4" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20">
                <path d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" />
              </svg>
            </div>
          </div>
        </div>
      </div>
      
      <div className="divide-y divide-gray-200">
        {filteredRequests.map((request) => (
          <RequestCard
            key={request.id}
            request={request}
            onView={() => handleView(request.id)}
          />
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

export default RequestsList;