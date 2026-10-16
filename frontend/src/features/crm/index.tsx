import { ModuleGate } from "@/features/modules";
import { PipelinePage } from "./pages/pipeline-page";

// Public API of the CRM feature: the sales pipeline board.
export const CrmPipelinePage = () => (
  <ModuleGate module="crm">
    <PipelinePage />
  </ModuleGate>
);
