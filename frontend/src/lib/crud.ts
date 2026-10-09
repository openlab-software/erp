import {
  keepPreviousData,
  useMutation,
  useQuery,
  useQueryClient,
} from "@tanstack/react-query";
import { http } from "./http";
import { jsonBody, toSearch } from "./query";
import type { Page, Success } from "./types";

export interface ListParams {
  q: string;
  page: number;
  pageSize: number;
}

interface CrudOptions<V> {
  /** Cache namespace, e.g. "categories". */
  key: string;
  /** BFF route, e.g. "/api/catalog/categories". */
  base: string;
  /** Form values → request body (snake_case, `null` for cleared optionals). */
  toPayload: (values: V) => unknown;
}

/**
 * api + TanStack Query hooks for one paginated `{ list, create, update, delete }` resource
 * (everything that follows the `brands` shape). Every mutation refreshes the cached lists.
 */
export function createCrud<T, V>({ key, base, toPayload }: CrudOptions<V>) {
  const keys = {
    all: [key] as const,
    list: (params: ListParams) => [key, "list", params] as const,
  };
  const itemUrl = (id: string) => `${base}/${encodeURIComponent(id)}`;

  const api = {
    list: ({ q, page, pageSize }: ListParams) =>
      http<Page<T>>(`${base}${toSearch({ q, page, page_size: pageSize })}`),
    create: (values: V) =>
      http<T>(base, { method: "POST", body: jsonBody(toPayload(values)) }),
    update: (id: string, values: V) =>
      http<T>(itemUrl(id), {
        method: "PUT",
        body: jsonBody(toPayload(values)),
      }),
    remove: (id: string) => http<Success>(itemUrl(id), { method: "DELETE" }),
  };

  /** Paginated list; keeps the previous page on screen while the next one loads. */
  const useList = (params: ListParams) =>
    useQuery({
      queryKey: keys.list(params),
      queryFn: () => api.list(params),
      placeholderData: keepPreviousData,
    });

  const useRefreshingMutation = <TVariables, TResult>(
    mutationFn: (variables: TVariables) => Promise<TResult>,
  ) => {
    const queryClient = useQueryClient();
    return useMutation({
      mutationFn,
      onSuccess: () => queryClient.invalidateQueries({ queryKey: keys.all }),
    });
  };

  const useCreate = () => useRefreshingMutation(api.create);
  const useUpdate = () =>
    useRefreshingMutation(({ id, values }: { id: string; values: V }) =>
      api.update(id, values),
    );
  const useRemove = () => useRefreshingMutation(api.remove);

  return { keys, api, useList, useCreate, useUpdate, useRemove };
}
