import { Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Dashboard from "./pages/Dashboard";
import Requests from "./pages/Requests";
import Logs from "./pages/Logs";
import RequestDetails from "./pages/RequestDetails";
import Settings from "./pages/Settings";
import FraudDetection from "./pages/FraudDetection";

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />
      <Route path="/dashboard" element={<Dashboard />} />
      <Route path="/requests" element={<Requests />} />
      <Route path="/requests/:id" element={<RequestDetails />} />
      <Route path="/logs" element={<Logs />} />
      <Route path="/settings" element={<Settings />} />
      <Route path="/fraud-detection" element={<FraudDetection />} />
    </Routes>
  );
}

export default App;