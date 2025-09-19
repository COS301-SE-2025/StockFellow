const Header = () => {
  return (
    <div className="flex items-center justify-between bg-white p-4 shadow">
      <input
        type="text"
        placeholder="Search"
        className="rounded-md border px-3 py-2 text-sm focus:outline-none"
      />
      <div className="flex items-center space-x-2">
        <img
          src="https://i.pravatar.cc/40"
          alt="user"
          className="h-8 w-8 rounded-full"
        />
        <span className="text-sm">Naruto Uzumaki</span>
      </div>
    </div>
  );
};

export default Header;
