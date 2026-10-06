import { useState } from "react";
import { Outlet } from "@modern-js/runtime/router";
import { OpenLabUIProvider } from "@openlab-ui/react";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import styled from "@xstyled/emotion";
import { Sidebar } from "../components/shell/sidebar";
import { Topbar } from "../components/shell/topbar";
import { ToastProvider } from "../contexts/toast-context";
import "./index.css";
import "@openlab-ui/react/styles.css";

export default function Layout() {
  const [collapsed, setCollapsed] = useState(false);
  // One client per app instance (kept in state so it survives re-renders).
  const [queryClient] = useState(
    () =>
      new QueryClient({
        defaultOptions: {
          queries: { staleTime: 30_000, retry: 1, refetchOnWindowFocus: false },
        },
      }),
  );
  return (
    <QueryClientProvider client={queryClient}>
      <OpenLabUIProvider>
        <ToastProvider>
          <App data-side={collapsed ? "collapsed" : "open"}>
            <Sidebar collapsed={collapsed} />
            <Main>
              <Topbar onToggleSide={() => setCollapsed((c) => !c)} />
              <Content>
                <Outlet />
              </Content>
            </Main>
          </App>
        </ToastProvider>
      </OpenLabUIProvider>
    </QueryClientProvider>
  );
}

const App = styled.div`
  display: grid;
  grid-template-columns: var(--side-w, 232px) 1fr;
  height: 100vh;
  overflow: hidden;
  transition: grid-template-columns 220ms ease;
  &[data-side="collapsed"] {
    --side-w: 56px;
  }
  @media (max-width: 820px) {
    grid-template-columns: 56px 1fr;
  }
`;

const Main = styled.div`
  display: flex;
  flex-direction: column;
  background: var(--bg);
  overflow: hidden;
  min-width: 0;
`;

const Content = styled.div`
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  &::-webkit-scrollbar {
    width: 10px;
  }
  &::-webkit-scrollbar-thumb {
    background: var(--line-strong);
    border-radius: 5px;
    border: 2px solid var(--bg);
    background-clip: padding-box;
  }
  &::-webkit-scrollbar-thumb:hover {
    background: var(--ink-4);
    border: 2px solid var(--bg);
    background-clip: padding-box;
  }
`;
