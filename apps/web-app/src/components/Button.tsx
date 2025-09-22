import React from "react";

interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  children: React.ReactNode;
}

const Button: React.FC<ButtonProps> = ({ children, ...props }) => {
  return (
    <button
      {...props}
      className="w-full rounded-3xl bg-blue-600 px-6 py-3 mb-4 text-white hover:bg-blue-700 transition"
    >
      {children}
    </button>
  );
};

export default Button;
