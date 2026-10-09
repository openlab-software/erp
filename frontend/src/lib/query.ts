import { ApiError } from "./http";

/** Message to show for a failed request: the backend's own text when there is one. */
export const errorMessage = (error: unknown, fallback: string) =>
  error instanceof ApiError ? error.message : fallback;

/** Builds `?a=1&b=2`, skipping empty values. */
export const toSearch = (
  params: Record<string, string | number | boolean | undefined>,
) => {
  const search = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== "") search.set(key, String(value));
  }
  const text = search.toString();
  return text ? `?${text}` : "";
};

export const jsonBody = (body: unknown) => JSON.stringify(body);
