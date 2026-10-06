import path from "path";
import { appTools, defineConfig } from "@modern-js/app-tools";
import { bffPlugin } from "@modern-js/plugin-bff";

// https://modernjs.dev/en/configure/app/usage
export default defineConfig({
  plugins: [appTools(), bffPlugin()],
  server: {
    // ssr: true,
    port: 3001,
  },
  source: {
    alias: {
      react: path.resolve("./node_modules/react"),
      "react-dom": path.resolve("./node_modules/react-dom"),
      "react/jsx-runtime": path.resolve("./node_modules/react/jsx-runtime"),
    },
  },
});
