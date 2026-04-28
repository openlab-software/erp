import { Outlet } from "@modern-js/runtime/router";
import { OpenLabUIProvider } from "@openlab-ui/react";
import "@openlab-ui/react/styles.css";

export default function Layout() {
  return (
    <OpenLabUIProvider>
      <div>
        <Outlet />
      </div>
    </OpenLabUIProvider>
  );
}
