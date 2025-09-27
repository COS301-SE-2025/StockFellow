// src/pages/Dashboard.tsx
import React, { useState, useEffect } from 'react';
import { LineChart, Line, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from "recharts";
import Sidebar from "../components/layout/Sidebar";
import Header from "../components/layout/Header";
import { adminService, DashboardSummary, AnalyticsDashboard } from "../services/adminService";

interface DashboardState {
  summary: DashboardSummary | null;
  analytics: AnalyticsDashboard | null;
  loading: boolean;
  error: string | null;
  lastUpdated: Date | null;
}

const Dashboard: React.FC = () => {
  const [dashboardState, setDashboardState] = useState<DashboardState>({
    summary: null,
    analytics: null,
    loading: true,
    error: null,
    lastUpdated: null
  });

  const [refreshing, setRefreshing] = useState(false);
  const [timeRange, setTimeRange] = useState<'7d' | '30d'>('7d');

  const colors = ["#2563eb", "#60a5fa", "#1e3a8a", "#93c5fd"];

  useEffect(() => {
    loadDashboardData();

    // Set up auto-refresh every 5 minutes
    const interval = setInterval(() => {
      if (!document.hidden) { // Only refresh if page is visible
        refreshDashboardData();
      }
    }, 5 * 60 * 1000);

    return () => clearInterval(interval);
  }, [timeRange]);

  const loadDashboardData = async () => {
    try {
      setDashboardState(prev => ({ ...prev, loading: true, error: null }));

      const [summaryData, analyticsData] = await Promise.all([
        adminService.getDashboardSummary(),
        adminService.getAnalyticsDashboard(timeRange)
      ]);

      setDashboardState({
        summary: summaryData,
        analytics: analyticsData,
        loading: false,
        error: null,
        lastUpdated: new Date()
      });
    } catch (error: any) {
      console.error('Failed to load dashboard data:', error);
      setDashboardState(prev => ({
        ...prev,
        loading: false,
        error: error.message || 'Failed to load dashboard data'
      }));

      // If authentication failed, redirect to login
      if (error.message === 'Authentication required') {
        adminService.logout();
        return;
      }
    }
  };

  const refreshDashboardData = async () => {
    setRefreshing(true);
    try {
      await loadDashboardData();
    } finally {
      setRefreshing(false);
    }
  };

  const handleTimeRangeChange = (newTimeRange: '7d' | '30d') => {
    if (newTimeRange !== timeRange) {
      setTimeRange(newTimeRange);
    }
  };

  const formatCurrency = (amount: number): string => {
    return new Intl.NumberFormat('en-ZA', {
      style: 'currency',
      currency: 'ZAR',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(amount);
  };

  const formatDate = (dateString: string): string => {
    return new Date(dateString).toLocaleDateString('en-ZA', {
      month: 'short',
      day: 'numeric'
    });
  };

  // Transform growth trends data for charts
  const transformGrowthData = () => {
    if (!dashboardState.analytics?.growthTrends) return [];

    return dashboardState.analytics.growthTrends
      .slice(0, 7) // Show last 7 days
      .reverse()
      .map(trend => ({
        date: formatDate(trend.date),
        value: trend.newGroups || 0
      }));
  };

  // Transform tier stats (mock data for now, would come from backend later)
  const getTierStats = () => [
    { name: "Tier 1", value: 30 },
    { name: "Tier 2", value: 15 },
    { name: "Tier 3", value: 35 },
    { name: "Tier 4", value: 20 },
  ];

  // Mock requests data (would integrate with pending requests endpoint)
  const getRecentRequests = () => {
    if (!dashboardState.summary?.pendingRequestsCount) {
      return [
        { id: 1, name: "No pending requests", type: "System" }
      ];
    }

    // This would be replaced with actual request data
    return [
      { id: 1, name: "User Request", type: "Account" },
      { id: 2, name: "Group Request", type: "Stokvel" },
      { id: 3, name: "Transaction Issue", type: "Support" },
    ];
  };

  if (dashboardState.loading && !dashboardState.summary) {
    return (
      <div className="flex h-screen">
        <Sidebar />
        <div className="flex flex-1 flex-col">
          <Header />
          <main className="p-6 flex items-center justify-center">
            <div className="text-center">
              <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600 mx-auto"></div>
              <p className="mt-4 text-gray-600">Loading dashboard data...</p>
            </div>
          </main>
        </div>
      </div>
    );
  }

  if (dashboardState.error && !dashboardState.summary) {
    return (
      <div className="flex h-screen">
        <Sidebar />
        <div className="flex flex-1 flex-col">
          <Header />
          <main className="p-6 flex items-center justify-center">
            <div className="text-center">
              <div className="text-red-500 text-lg mb-4">Error loading dashboard</div>
              <p className="text-gray-600 mb-4">{dashboardState.error}</p>
              <button
                onClick={loadDashboardData}
                className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
              >
                Retry
              </button>
            </div>
          </main>
        </div>
      </div>
    );
  }

  const { summary, analytics } = dashboardState;

  return (
    <div className="flex h-screen">
      <Sidebar />
      <div className="flex flex-1 flex-col">
        <Header />
        <main className="p-6 space-y-6 overflow-y-auto">
          {/* Header with refresh and time range controls */}
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Admin Dashboard</h1>
              {dashboardState.lastUpdated && (
                <p className="text-sm text-gray-500">
                  Last updated: {dashboardState.lastUpdated.toLocaleTimeString()}
                </p>
              )}
            </div>
            <div className="flex space-x-3">
              <select
                value={timeRange}
                onChange={(e) => handleTimeRangeChange(e.target.value as '7d' | '30d')}
                className="px-3 py-2 border border-gray-300 rounded-md text-sm"
              >
                <option value="7d">Last 7 days</option>
                <option value="30d">Last 30 days</option>
              </select>
              <button
                onClick={refreshDashboardData}
                disabled={refreshing}
                className="px-4 py-2 bg-blue-600 text-white rounded-md text-sm hover:bg-blue-700 disabled:opacity-50"
              >
                {refreshing ? 'Refreshing...' : 'Refresh'}
              </button>
            </div>
          </div>

          {/* Error banner */}
          {dashboardState.error && (
            <div className="bg-red-50 border border-red-200 rounded-md p-4">
              <div className="text-red-800 text-sm">{dashboardState.error}</div>
            </div>
          )}

          {/* Stats Cards */}
          <div className="grid grid-cols-3 gap-4">
            <div className="rounded-lg bg-white p-4 shadow">
              <p className="text-gray-600">Total Revenue</p>
              <h2 className="text-xl font-bold text-gray-900">
                {summary?.revenueData ? formatCurrency(summary.revenueData.totalRevenue) : 'Loading...'}
              </h2>
              {summary?.revenueData && (
                <p className="text-sm text-green-600">
                  Projected Monthly: {formatCurrency(summary.revenueData.projectedMonthly)}
                </p>
              )}
            </div>
            <div className="rounded-lg bg-white p-4 shadow">
              <p className="text-gray-600">Total Stokvels</p>
              <h2 className="text-xl font-bold text-gray-900">
                {summary?.groupMetrics.totalGroups ?? 'Loading...'}
              </h2>
              <p className="text-sm text-blue-600">
                Active: {summary?.groupMetrics.activeGroups ?? 0}
              </p>
            </div>
            <div className="rounded-lg bg-white p-4 shadow">
              <p className="text-gray-600">Total Users</p>
              <h2 className="text-xl font-bold text-gray-900">
                {summary?.userMetrics.totalUsers ?? 'Loading...'}
              </h2>
              <p className="text-sm text-green-600">
                Verified: {summary?.userMetrics.verifiedUsers ?? 0}
              </p>
            </div>
          </div>

          {/* Activity Chart */}
          <div className="rounded-lg bg-white p-4 shadow">
            <h3 className="font-bold mb-2">User Activity Trends</h3>
            {analytics?.growthTrends ? (
              <ResponsiveContainer width="100%" height={250}>
                <LineChart data={analytics.growthTrends.slice(-7).reverse()}>
                  <XAxis
                    dataKey="date"
                    tickFormatter={(value) => formatDate(value)}
                  />
                  <YAxis />
                  <Tooltip
                    labelFormatter={(value) => `Date: ${formatDate(value)}`}
                  />
                  <Line
                    type="monotone"
                    dataKey="activeUsers"
                    stroke="#2563eb"
                    strokeWidth={2}
                    name="Active Users"
                  />
                  <Line
                    type="monotone"
                    dataKey="newUsers"
                    stroke="#60a5fa"
                    strokeWidth={2}
                    name="New Users"
                  />
                </LineChart>
              </ResponsiveContainer>
            ) : (
              <div className="h-[250px] flex items-center justify-center text-gray-500">
                Loading chart data...
              </div>
            )}
          </div>

          <div className="grid grid-cols-3 gap-4">
            {/* Stokvels Created Chart */}
            <div className="rounded-lg bg-white p-4 shadow">
              <h3 className="font-bold mb-2">Stokvels Created</h3>
              {analytics?.growthTrends ? (
                <ResponsiveContainer width="100%" height={200}>
                  <LineChart data={transformGrowthData()}>
                    <XAxis dataKey="date" />
                    <YAxis />
                    <Tooltip />
                    <Line type="monotone" dataKey="value" stroke="#2563eb" strokeWidth={2} dot />
                  </LineChart>
                </ResponsiveContainer>
              ) : (
                <div className="h-[200px] flex items-center justify-center text-gray-500">
                  Loading...
                </div>
              )}
            </div>

            {/* Pending Requests */}
            <div className="rounded-lg bg-white p-4 shadow">
              <h3 className="font-bold mb-2">
                Pending Requests ({summary?.pendingRequestsCount ?? 0})
              </h3>
              <ul className="space-y-2">
                {getRecentRequests().map((req) => (
                  <li key={req.id} className="flex items-center space-x-2">
                    <div className="h-8 w-8 rounded-full bg-gray-200 flex items-center justify-center">
                      {req.name[0]}
                    </div>
                    <div>
                      <p className="font-medium text-sm">{req.name}</p>
                      <p className="text-xs text-gray-500">{req.type}</p>
                    </div>
                  </li>
                ))}
              </ul>
            </div>

            {/* Stokvel Tier Statistics */}
            <div className="rounded-lg bg-white p-4 shadow">
              <h3 className="font-bold mb-2">Stokvel Tier Statistics</h3>
              <ResponsiveContainer width="100%" height={200}>
                <PieChart>
                  <Pie data={getTierStats()} dataKey="value" nameKey="name" outerRadius={80} label>
                    {getTierStats().map((_, index) => (
                      <Cell key={`cell-${index}`} fill={colors[index % colors.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Alerts and Notifications */}
          {(summary?.recentSuspiciousActivity?.length && summary.recentSuspiciousActivity.length > 0) ||
            (summary?.staleRequests?.length && summary.staleRequests.length > 0) ? (
            <div className="grid grid-cols-2 gap-4">
              {summary?.recentSuspiciousActivity?.length && summary.recentSuspiciousActivity.length > 0 && (
                <div className="rounded-lg bg-yellow-50 border border-yellow-200 p-4">
                  <h3 className="font-bold text-yellow-800 mb-2">Suspicious Activity Detected</h3>
                  <p className="text-yellow-700 text-sm">
                    {summary.recentSuspiciousActivity.length} suspicious activities require review
                  </p>
                </div>
              )}

              {summary?.staleRequests?.length && summary.staleRequests.length > 0 && (
                <div className="rounded-lg bg-orange-50 border border-orange-200 p-4">
                  <h3 className="font-bold text-orange-800 mb-2">Stale Requests</h3>
                  <p className="text-orange-700 text-sm">
                    {summary.staleRequests.length} requests are overdue for review
                  </p>
                </div>
              )}
            </div>
          ) : null}
        </main>
      </div>
    </div>
  );
};

export default Dashboard;