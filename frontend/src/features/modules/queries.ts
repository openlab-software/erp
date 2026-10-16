import { http } from "@/lib/http";
import { useQuery } from "@tanstack/react-query";
import type { ModuleList } from "./types";

export const moduleKeys = { all: ["modules"] as const };

/** The modules installed in this deployment. They only change with a new release, so never refetch. */
export const useModules = () =>
  useQuery({
    queryKey: moduleKeys.all,
    queryFn: () => http<ModuleList>("/api/modules"),
    select: (list) => list.data,
    staleTime: Number.POSITIVE_INFINITY,
  });

/** `undefined` while loading, otherwise whether the module (by `name`) is installed. */
export const useHasModule = (name: string) => {
  const modules = useModules();
  return modules.data ? modules.data.some((m) => m.name === name) : undefined;
};
