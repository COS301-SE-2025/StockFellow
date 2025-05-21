module.exports = function (api) {
  api.cache(true);
  return {
    presets: [
      ["babel-preset-expo", { jsxImportSource: "nativewind" }],
      "nativewind/babel",
    ],
    env: {
      production: {
        plugins: ['react-native-paper/babel'], // The plugin automatically rewrites the import 
        // statements so that only the modules you use are imported instead of the whole library
      },
    },
  };
};