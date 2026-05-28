const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
const BACKEND_CONNECTION_ERROR =
  'Cannot connect to backend. Please check if backend is running on http://localhost:8080.';

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

export type ClearanceType = 'GRADUATION' | 'WITHDRAWAL' | 'TRANSFER';

export type ClearanceRequestPayload = {
  requestType: ClearanceType;
  reason?: string;
};

export type ClearanceStepResponse = {
  id: number;
  officeId: number;
  officeName: string;
  status: string;
  comment?: string | null;
  reviewedAt?: string | null;
};

export type ClearanceRequestResponse = {
  id: number;
  requestType: ClearanceType;
  status: string;
  reason?: string | null;
  createdAt: string;
  steps: ClearanceStepResponse[];
};

export type OfficeStepReviewResponse = {
  stepId: number;
  clearanceRequestId: number;
  requestType: string;
  requestStatus: string;
  studentName: string;
  studentId: string;
  officeId: number;
  officeName: string;
  status: string;
  comment?: string | null;
  reviewedAt?: string | null;
  reviewedBy?: string | null;
};

export type PageResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
};

export async function getHealth() {
  const response = await fetch(`${API_BASE_URL}/api/health`);

  if (!response.ok) {
    throw new Error('Backend health check failed');
  }

  return response.json() as Promise<{ status: string; app: string }>;
}

export async function login(payload: LoginPayload) {
  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}/api/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
  } catch {
    throw new Error(BACKEND_CONNECTION_ERROR);
  }

  return handleAuthResponse(response);
}

export async function register(payload: RegisterPayload) {
  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}/api/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
  } catch {
    throw new Error(BACKEND_CONNECTION_ERROR);
  }

  return handleAuthResponse(response);
}

export async function logout() {
  const token = getToken();
  if (!token) {
    return;
  }

  await fetch(`${API_BASE_URL}/api/auth/logout`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({ token }),
  });

  clearToken();
}

export async function createClearanceRequest(payload: ClearanceRequestPayload) {
  return authorizedJson<ClearanceRequestResponse>('/api/clearance-requests', {
    method: 'POST',
    body: JSON.stringify(payload),
  });
}

export async function getMyClearanceRequests(page = 0, size = 10) {
  return authorizedJson<PageResponse<ClearanceRequestResponse>>(
    `/api/clearance-requests/my?page=${page}&size=${size}`
  );
}

export async function getClearanceRequestById(id: number) {
  return authorizedJson<ClearanceRequestResponse>(`/api/clearance-requests/${id}`);
}

export async function getAssignedOfficeSteps(status?: string) {
  const query = status ? `?status=${status}` : '';
  return authorizedJson<PageResponse<OfficeStepReviewResponse>>(`/api/office/clearance-steps${query}`);
}

export async function reviewOfficeStep(stepId: number, decision: 'APPROVED' | 'REJECTED', comment: string) {
  return authorizedJson<OfficeStepReviewResponse>(`/api/office/clearance-steps/${stepId}/review`, {
    method: 'PATCH',
    body: JSON.stringify({ decision, comment }),
  });
}

export function getToken() {
  return localStorage.getItem('clearance_auth_token');
}

export function getSavedUser() {
  const user = localStorage.getItem('clearance_auth_user');
  return user ? (JSON.parse(user) as UserResponse) : null;
}

export function saveToken(token: string) {
  localStorage.setItem('clearance_auth_token', token);
}

export function saveUser(user: UserResponse) {
  localStorage.setItem('clearance_auth_user', JSON.stringify(user));
}

export function clearToken() {
  localStorage.removeItem('clearance_auth_token');
  localStorage.removeItem('clearance_auth_user');
}

async function handleAuthResponse(response: Response) {
  if (!response.ok) {
    throw new Error(await resolveErrorMessage(response));
  }

  const data = (await response.json()) as AuthResponse;
  saveToken(data.token);
  saveUser(data.user);
  return data;
}

async function authorizedJson<T>(path: string, options: RequestInit = {}) {
  const token = getToken();
  if (!token) {
    throw new Error('Please login before using clearance requests.');
  }

  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
        ...options.headers,
      },
    });
  } catch {
    throw new Error(BACKEND_CONNECTION_ERROR);
  }

  if (!response.ok) {
    throw new Error(await resolveErrorMessage(response));
  }

  return response.json() as Promise<T>;
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
