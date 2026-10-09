/** `{ data, page, page_size, total }` — the listing envelope of both services. */
export interface Page<T> {
  data: T[];
  page: number;
  page_size: number;
  total: number;
}

export interface Success {
  success: boolean;
}
