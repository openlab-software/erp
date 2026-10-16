import { type Dirent, readFileSync, readdirSync } from 'node:fs';
import { join } from 'node:path';

/** What the frontend needs of a module manifest (modules/<name>/module.json). */
export interface ErpModule {
  name: string;
  display_name: string;
  summary: string;
  category: string;
  version: string;
  depends: string[];
  integrates_with: string[];
  services: { name: string; port: number; bff_prefix: string }[];
  menu: { group: string; label: string; path: string; icon: string }[];
}

/**
 * The modules installed in this deployment, in manifest order.
 *
 * In Kubernetes the chart renders the manifests of the enabled modules (dependencies included) into
 * ERP_MODULES_JSON. Locally there is no chart, so the manifests are read from ../modules and
 * ERP_MODULES (comma-separated names, default: all) picks the ones to show.
 */
export function installedModules(): ErpModule[] {
  const fromChart = process.env.ERP_MODULES_JSON;
  if (fromChart) {
    return JSON.parse(fromChart) as ErpModule[];
  }

  const dir =
    process.env.ERP_MODULES_DIR ?? join(process.cwd(), '..', 'modules');
  const wanted = process.env.ERP_MODULES?.split(',').map(m => m.trim());
  const modules: ErpModule[] = [];
  let entries: Dirent[];
  try {
    entries = readdirSync(dir, { withFileTypes: true });
  } catch {
    console.warn(
      `[bff] no module manifests: set ERP_MODULES_JSON or ERP_MODULES_DIR (looked in ${dir})`,
    );
    return [];
  }
  for (const entry of entries) {
    if (!entry.isDirectory()) continue;
    try {
      const manifest = JSON.parse(
        readFileSync(join(dir, entry.name, 'module.json'), 'utf8'),
      ) as ErpModule;
      if (!wanted || wanted.includes(manifest.name)) modules.push(manifest);
    } catch {
      // a folder without a manifest is not a module
    }
  }
  return modules;
}
