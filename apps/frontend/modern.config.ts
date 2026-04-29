import path from "path";
import { appTools, defineConfig } from "@modern-js/app-tools";

// https://modernjs.dev/en/configure/app/usage
export default defineConfig({
  plugins: [appTools()],
  server: {
    // ssr: true,
    port: 3000,
  },
  source: {
    alias: {
      react: path.resolve("./node_modules/react"),
      "react-dom": path.resolve("./node_modules/react-dom"),
      "react/jsx-runtime": path.resolve("./node_modules/react/jsx-runtime"),
    },
  },
});
