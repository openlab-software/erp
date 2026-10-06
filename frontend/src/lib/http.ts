export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
  ) {
    super(message);
  }
}

/** Calls the BFF (same origin, `/api/...`). Throws ApiError with the backend's message. */
export async function http<T>(path: string, init: RequestInit = {}): Promise<T> {
  let response: Response;
  try {
    response = await fetch(path, {
      ...init,
      headers: {
        accept: "application/json",
        ...(init.body !== undefined && { "content-type": "application/json" }),
        ...init.headers,
      },
    });
  } catch {
    throw new ApiError(0, "Não foi possível conectar ao servidor");
  }

  if (!response.ok) {
    const payload = await response.json().catch(() => ({}));
    throw new ApiError(
      response.status,
      payload.message ?? payload.mensagem ?? "Erro inesperado",
    );
  }
  return (await response.json()) as T;
}
