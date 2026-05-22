const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api';

export async function getHealth() {
  const response = await fetch(`${API_BASE_URL}/health`);

  if (!response.ok) {
    throw new Error('Backend health check failed');
  }

  return response.json() as Promise<{ status: string; app: string }>;
}

export { API_BASE_URL };
