/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./*.{js,ts,tsx,jsx}",
    "./app/**/*.{js,jsx,ts,tsx}",
    "./src/**/*.{js,jsx,ts,tsx}",
    "./index.{js,ts,tsx}"
  ],
  // Remove this line for NativeWind v2:
  // presets: [require("nativewind/preset")],
  theme: {
    extend: {},
  },
  plugins: [],
}