import Sidebar from "../components/layout/Sidebar";
import Header from "../components/layout/Header";
import StokvelDetailsChart from "../components/charts/StokvelDetailsChart";
import { stats, stokvelsCreatedData, requests, tierStats } from "../mock/data";
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from "recharts";

const Dashboard = () => {
  const colors = ["#2563eb", "#60a5fa", "#1e3a8a", "#93c5fd"];

  return (
    <div className="flex h-screen">
      <Sidebar />
      <div className="flex flex-1 flex-col">
        <Header />
        <main className="p-6 space-y-6 overflow-y-auto">
          {/* Stats */}
          <div className="grid grid-cols-3 gap-4">
            <div className="rounded-lg bg-white p-4 shadow">
              <p>Total payouts</p>
              <h2 className="text-xl font-bold">{stats.totalPayouts}</h2>
            </div>
            <div className="rounded-lg bg-white p-4 shadow">
              <p>Number of Stokvels</p>
              <h2 className="text-xl font-bold">{stats.stokvels}</h2>
            </div>
            <div className="rounded-lg bg-white p-4 shadow">
              <p>Total Users Growth Rate</p>
              <h2 className="text-xl font-bold">{stats.growthRate}</h2>
            </div>
          </div>

          {/* Charts */}
          <div className="rounded-lg bg-white p-4 shadow">
            <h3 className="font-bold mb-2">Stokvels Details</h3>
            <StokvelDetailsChart />
          </div>

          <div className="grid grid-cols-3 gap-4">
            <div className="rounded-lg bg-white p-4 shadow">
              <h3 className="font-bold mb-2">Stokvels Created</h3>
              <ResponsiveContainer width="100%" height={200}>
                <LineChart data={stokvelsCreatedData}>
                  <XAxis dataKey="month" />
                  <YAxis />
                  <Tooltip />
                  <Line type="monotone" dataKey="value" stroke="#2563eb" strokeWidth={2} dot />
                </LineChart>
              </ResponsiveContainer>
            </div>

            <div className="rounded-lg bg-white p-4 shadow">
              <h3 className="font-bold mb-2">Requests</h3>
              <ul className="space-y-2">
                {requests.map((req) => (
                  <li key={req.id} className="flex items-center space-x-2">
                    <div className="h-8 w-8 rounded-full bg-gray-200 flex items-center justify-center">
                      {req.name[0]}
                    </div>
                    <div>
                      <p className="font-medium">{req.name}</p>
                      <p className="text-sm text-gray-500">{req.type}</p>
                    </div>
                  </li>
                ))}
              </ul>
            </div>

            <div className="rounded-lg bg-white p-4 shadow">
              <h3 className="font-bold mb-2">Stokvel Tier Statistics</h3>
              <ResponsiveContainer width="100%" height={200}>
                <PieChart>
                  <Pie data={tierStats} dataKey="value" nameKey="name" outerRadius={80} label>
                    {tierStats.map((_ , index) => (
                      <Cell key={`cell-${index}`} fill={colors[index % colors.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            </div>
          </div>
        </main>
      </div>
    </div>
  );
};

export default Dashboard;
