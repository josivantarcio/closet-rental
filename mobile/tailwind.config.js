/** @type {import('tailwindcss').Config} */
module.exports = {
  content: ["./app/**/*.{ts,tsx}", "./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: "#e91e8c",
          dark: "#c2185b",
          light: "#f48fb1",
        },
        surface: {
          DEFAULT: "#1a1a1a",
          elevated: "#242424",
          card: "#2a2a2a",
        },
        border: "#3a3a3a",
      },
    },
  },
  plugins: [],
};
