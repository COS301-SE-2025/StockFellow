// src/components/Input.tsx
import React, { InputHTMLAttributes, useState } from 'react';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  className?: string;
  showPasswordToggle?: boolean;
}

const Input: React.FC<InputProps> = ({ 
  className = '', 
  showPasswordToggle = false,
  type,
  ...props 
}) => {
  const [isPasswordVisible, setIsPasswordVisible] = useState(false);
  
  const baseClasses = `
    w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm
    placeholder-gray-400 text-gray-900
    focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500
    disabled:bg-gray-100 disabled:cursor-not-allowed
    transition-colors duration-200
  `;

  const inputType = showPasswordToggle && type === 'password' 
    ? (isPasswordVisible ? 'text' : 'password')
    : type;

  const togglePasswordVisibility = () => {
    setIsPasswordVisible(!isPasswordVisible);
  };

  if (showPasswordToggle && type === 'password') {
    return (
      <div className="relative">
        <input
          type={inputType}
          className={`${baseClasses} ${className} pr-10`.trim()}
          {...props}
        />
        <button
          type="button"
          className="absolute inset-y-0 right-0 pr-3 flex items-center"
          onClick={togglePasswordVisibility}
        >
          {isPasswordVisible ? (
            // Eye slash icon (password visible, click to hide)
            <svg 
              className="h-5 w-5 text-gray-400 hover:text-gray-600" 
              fill="none" 
              viewBox="0 0 24 24" 
              stroke="currentColor"
            >
              <path 
                strokeLinecap="round" 
                strokeLinejoin="round" 
                strokeWidth={2} 
                d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L21 21" 
              />
            </svg>
          ) : (
            // Eye icon (password hidden, click to show)
            <svg 
              className="h-5 w-5 text-gray-400 hover:text-gray-600" 
              fill="none" 
              viewBox="0 0 24 24" 
              stroke="currentColor"
            >
              <path 
                strokeLinecap="round" 
                strokeLinejoin="round" 
                strokeWidth={2} 
                d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" 
              />
              <path 
                strokeLinecap="round" 
                strokeLinejoin="round" 
                strokeWidth={2} 
                d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" 
              />
            </svg>
          )}
        </button>
      </div>
    );
  }

  return (
    <input
      type={inputType}
      className={`${baseClasses} ${className}`.trim()}
      {...props}
    />
  );
};

export default Input;