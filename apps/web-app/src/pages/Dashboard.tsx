import Sidebar from "../components/layout/Sidebar";
import Header from "../components/layout/Header";
import StokvelDetailsChart from "../components/charts/StokvelDetailsChart";
import { stats, stokvelsCreatedData, requests, tierStats } from "../mock/data";
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from "recharts";
import totalPayoutImg from '../assets/total_payout.png';
import stokvelsImg from '../assets/stokvel_large.png';
import usersGrowthImg from '../assets/growth_rate.png';

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
            <div className="rounded-3xl bg-white p-4 shadow">
              <div className="flex items-center justify-center">
                <img
                  src={totalPayoutImg}
                  alt="Total payouts"
                  className="h-14 w-14"
                />
                <div className="flex-1">
                  <h2 className="text-xl font-bold text-gray-900 mb-1">{stats.totalPayouts}</h2>
                  <p className="text-gray-600 text-sm">Total payouts</p>
                </div>
              </div>
            </div>
            <div className="rounded-3xl bg-white p-4 shadow">
              <img
                src={stokvelsImg}
                alt="No. of Stokvels"
                className="h-12 w-12"
              />
              <p>Number of Stokvels</p>
              <h2 className="text-xl font-bold">{stats.stokvels}</h2>
            </div>
            <div className="rounded-3xl bg-white p-4 shadow">
              <img
                src={usersGrowthImg}
                alt="Growth rate"
                className="h-12 w-12"
              />
              <p>Total Users Growth Rate</p>
              <h2 className="text-xl font-bold">{stats.growthRate}</h2>
            </div>
          </div>

          {/* Charts */}
          <div className="rounded-3xl bg-white p-4 shadow">
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
                    {tierStats.map((entry, index) => (
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
