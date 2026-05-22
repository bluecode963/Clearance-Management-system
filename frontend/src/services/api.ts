const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? '/api';

export type UserRole = 'ADMIN' | 'STUDENT' | 'OFFICE_STAFF' | 'REGISTRAR';

export type UserResponse = {
  id: number;
  fullName: string;
  email: string;
  role: UserRole;
  active: boolean;
};

export type AuthResponse = {
  token: string;
  tokenType: string;
  user: UserResponse;
};

export type LoginPayload = {
  email: string;
  password: string;
};

export type RegisterPayload = {
  fullName: string;
  email: string;
  password: string;
  role: UserRole;
  studentId?: string;
  department?: string;
  program?: string;
  yearOfStudy?: number;
};

export async function getHealth() {
  const response = await fetch(`${API_BASE_URL}/health`);

  if (!response.ok) {
    throw new Error('Backend health check failed');
  }

  return response.json() as Promise<{ status: string; app: string }>;
}

export async function login(payload: LoginPayload) {
  const response = await fetch(`${API_BASE_URL}/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });

  return handleAuthResponse(response);
}

export async function register(payload: RegisterPayload) {
  const response = await fetch(`${API_BASE_URL}/auth/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });

  return handleAuthResponse(response);
}

export async function logout() {
  const token = getToken();
  if (!token) {
    return;
  }

  await fetch(`${API_BASE_URL}/auth/logout`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ token }),
  });

  clearToken();
}

export function getToken() {
  return localStorage.getItem('clearance_auth_token');
}

export function saveToken(token: string) {
  localStorage.setItem('clearance_auth_token', token);
}

export function clearToken() {
  localStorage.removeItem('clearance_auth_token');
}

async function handleAuthResponse(response: Response) {
  if (!response.ok) {
    throw new Error(await resolveErrorMessage(response));
  }

  const data = (await response.json()) as AuthResponse;
  saveToken(data.token);
  return data;
}

async function resolveErrorMessage(response: Response) {
  try {
    const data = (await response.json()) as { message?: string; validationErrors?: string[] };
    if (data.validationErrors?.length) {
      return data.validationErrors.join(', ');
    }
    return data.message ?? 'Authentication request failed';
  } catch {
    return 'Authentication request failed';
  }
}

export { API_BASE_URL };
