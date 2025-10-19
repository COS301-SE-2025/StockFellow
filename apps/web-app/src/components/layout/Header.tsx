const Header = () => {
  return (
    <div className="flex items-center justify-between bg-white p-4 shadow">
      <input
        type="text"
        placeholder="Search"
        className="rounded-md border px-3 py-2 text-sm focus:outline-none"
      />
      <div className="flex items-center space-x-2">
        <svg
          className="h-8 w-8 text-gray-400"
          fill="currentColor"
          viewBox="0 0 24 24"
          xmlns="http://www.w3.org/2000/svg"
        >
          <path
            d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z"
          />
        </svg>
        <span className="text-sm">Admin</span>
      </div>
    </div>
  );
};

export default Header;