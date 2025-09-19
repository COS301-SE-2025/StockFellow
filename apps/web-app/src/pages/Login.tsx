import { Link, useNavigate } from "react-router-dom";
import Input from "../components/Input";
import Button from "../components/Button";

const Login = () => {
  const navigate = useNavigate();

  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault();
    navigate("/dashboard");
  };

  return (
    <div className="flex h-screen">
      <div className="flex-1 bg-gradient-to-b from-blue-400 to-blue-600 flex items-center justify-center">
        <h1 className="text-4xl font-bold text-white">StockFellow</h1>
      </div>
      <div className="flex-1 flex items-center justify-center">
        <form
          onSubmit={handleLogin}
          className="w-full max-w-sm space-y-4 p-6 bg-white rounded-lg shadow"
        >
          <h2 className="text-2xl font-bold">Hello Again!</h2>
          <p className="text-sm text-gray-500">Welcome Back</p>
          <Input type="email" placeholder="Email Address" required />
          <Input type="password" placeholder="Password" required />
          <Button type="submit">Login</Button>
          <Link to="/register" className="text-sm text-blue-600 hover:underline">
            Don’t have an account? Register
          </Link>
        </form>
      </div>
    </div>
  );
};

export default Login;
