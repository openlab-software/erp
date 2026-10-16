import { installedModules } from '../lib/modules.ts';

/** GET /api/modules -> { data: [manifest, ...] } (only the installed modules) */
export const get = async () => ({ data: installedModules() });
