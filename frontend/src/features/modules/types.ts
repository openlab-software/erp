// Mirrors modules/<name>/module.json (served by the BFF at /api/modules, only the installed modules).
export interface ModuleMenuItem {
  group: string;
  label: string;
  path: string;
  icon: string;
}

export interface ErpModule {
  name: string;
  display_name: string;
  summary: string;
  category: string;
  version: string;
  /** Needed to work: always installed together. */
  depends: string[];
  /** Optional: the modules talk to each other by events when both are installed. */
  integrates_with: string[];
  services: { name: string; port: number; bff_prefix: string }[];
  menu: ModuleMenuItem[];
}

export interface ModuleList {
  data: ErpModule[];
}
