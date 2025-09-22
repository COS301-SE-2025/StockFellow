// src/pages/Login.tsx
import React, { useState } from "react";
import { Link, Navigate, useLocation } from "react-router-dom";
import Input from "../components/Input";
import Button from "../components/Button";
import { useAuth } from "../contexts/AuthContext";

const Login: React.FC = () => {
  //const navigate = useNavigate();
  const location = useLocation();
  const { login, isAuthenticated } = useAuth();
  
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState("");

  // Get the page user was trying to access, default to dashboard
  const from = (location.state as any)?.from?.pathname || "/dashboard";

  // If already authenticated, redirect to dashboard
  if (isAuthenticated) {
    return <Navigate to={from} replace />;
  }

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Clear any previous errors
    setError("");
    
    // Validate inputs
    if (!username.trim()) {
      setError("Username is required");
      return;
    }
    
    if (!password.trim()) {
      setError("Password is required");
      return;
    }
    
    setIsLoading(true);

    try {
      // Use the auth context login method which calls the admin service
      await login(username, password);
      
      // Navigation will happen automatically via the redirect above
      // when isAuthenticated becomes true
      
    } catch (err: any) {
      console.error("Login error:", err);
      
      // Handle different error types
      if (err.message.includes("Invalid username or password")) {
        setError("Invalid username or password. Please try again.");
      } else if (err.message.includes("Authentication service temporarily unavailable")) {
        setError("Authentication service is currently unavailable. Please try again later.");
      } else if (err.message.includes("Please check your credentials")) {
        setError("Please check your credentials and try again.");
      } else {
        setError(err.message || "Login failed. Please try again.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex h-screen">
      {/* Left side - Branding */}
      <div className="flex-1 bg-gradient-to-b from-blue-500 to-blue-900 flex flex-col 
        items-start justify-center px-20">
        <h1 className="text-4xl font-bold text-white">StockFellow</h1>
        <h3 className="text-lg text-white mt-2">
          Your digital stokvel companion for seamless group savings
        </h3>
        <div className="mt-8 text-white/80">
          <p className="text-sm">Admin Portal</p>
          <p className="text-xs mt-1">
            Secure access to administrative functions and analytics
          </p>
        </div>
      </div>
      
      {/* Right side - Login Form */}
      <div className="flex-1 flex items-center justify-center bg-gray-50">
        <div className="w-full max-w-sm bg-white rounded-lg shadow-lg p-8">
          <form onSubmit={handleLogin} className="space-y-6">
            <div className="text-center">
              <h2 className="text-2xl font-bold text-gray-900">Hello Again!</h2>
              <p className="text-base text-gray-600 mt-1">Welcome Back</p>
            </div>

            {/* Error Message */}
            {error && (
              <div className="rounded-md bg-red-50 border border-red-200 p-3">
                <div className="text-sm text-red-700">{error}</div>
              </div>
            )}

            {/* Username Field */}
            <div>
              <Input
                type="text"
                placeholder="Username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
                disabled={isLoading}
                className={`w-full ${error ? 'border-red-300' : ''}`}
              />
            </div>

            {/* Password Field with visibility toggle */}
            <div>
              <Input
                type="password"
                placeholder="Password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                disabled={isLoading}
                showPasswordToggle={true}
                className={`w-full ${error ? 'border-red-300' : ''}`}
              />
            </div>

            {/* Login Button */}
            <Button
              type="submit"
              disabled={isLoading}
              className="w-full"
            >
              {isLoading ? (
                <div className="flex items-center justify-center">
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-white mr-2"></div>
                  Signing in...
                </div>
              ) : (
                "Login"
              )}
            </Button>

            {/* Additional Links */}
            <div className="text-center space-y-2">
              <Link 
                to="/register" 
                className="text-sm text-blue-600 hover:underline block"
              >
                Don't have an account? Register
              </Link>
              
              <Link 
                to="/forgot-password" 
                className="text-sm text-gray-500 hover:underline block"
              >
                Forgot your password?
              </Link>
            </div>
          </form>

          {/* Development Info */}
          {process.env.NODE_ENV === 'development' && (
            <div className="mt-6 p-3 bg-gray-100 rounded-md">
              <p className="text-xs text-gray-600 mb-2">Development Info:</p>
              <p className="text-xs text-gray-500">
                Backend: http://localhost:4060
              </p>
              <p className="text-xs text-gray-500">
                Auth Provider: Keycloak
              </p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Login;