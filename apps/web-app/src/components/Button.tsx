// src/components/Button.tsx
import React, { ButtonHTMLAttributes } from 'react';

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'danger';
  size?: 'sm' | 'md' | 'lg';
  className?: string;
}

const Button: React.FC<ButtonProps> = ({ 
  variant = 'primary', 
  size = 'md', 
  className = '',
  children,
  ...props 
}) => {
  const baseClasses = `
    inline-flex items-center justify-center font-medium rounded-md
    focus:outline-none focus:ring-2 focus:ring-offset-2
    disabled:opacity-50 disabled:cursor-not-allowed
    transition-colors duration-200
  `;

  const variantClasses = {
    primary: `
      bg-blue-600 text-white border border-transparent
      hover:bg-blue-700 focus:ring-blue-500
    `,
    secondary: `
      bg-white text-gray-700 border border-gray-300
      hover:bg-gray-50 focus:ring-blue-500
    `,
    danger: `
      bg-red-600 text-white border border-transparent
      hover:bg-red-700 focus:ring-red-500
    `
  };

  const sizeClasses = {
    sm: 'px-3 py-2 text-sm',
    md: 'px-4 py-2 text-sm',
    lg: 'px-6 py-3 text-base'
  };

  const classes = `
    ${baseClasses}
    ${variantClasses[variant]}
    ${sizeClasses[size]}
    ${className}
  `.trim();

  return (
    <button className={classes} {...props}>
      {children}
    </button>
  );
};

export default Button;