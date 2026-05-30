import { FormEvent, useState } from 'react';
import { DashboardLayout } from '../components/DashboardLayout';
import { PageShell } from '../components/PageShell';
import { createAdminUser, type AdminCreateUserPayload, type UserRole } from '../services/api';

const defaultForm: AdminCreateUserPayload = {
  fullName: '',
  email: '',
  password: '',
  role: 'STUDENT',
  studentId: '',
  department: '',
  program: '',
  yearOfStudy: 1,
  officeId: undefined,
};

export function AdminDashboardPage() {
  const [form, setForm] = useState<AdminCreateUserPayload>(defaultForm);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  function updateField<K extends keyof AdminCreateUserPayload>(key: K, value: AdminCreateUserPayload[K]) {
    setForm((current) => ({ ...current, [key]: value }));
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setMessage('');
    setError('');
    setSubmitting(true);

    try {
      const payload = buildPayload(form);
      const createdUser = await createAdminUser(payload);
      setMessage(`${createdUser.fullName} was created as ${formatRole(createdUser.role)}.`);
      setForm(defaultForm);
    } catch (createError) {
      setError(createError instanceof Error ? createError.message : 'Could not create user');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <PageShell title="Admin Dashboard" subtitle="Manage offices, users, and monitor clearance requests.">
      <div className="space-y-8">
        <DashboardLayout
          role="ADMIN"
          heading="System overview"
          description="Administrative users can create accounts for students, office staff, registrars, and other admins."
          metrics={[
            { label: 'Active offices', value: '5' },
            { label: 'User creation', value: 'Admin only' },
            { label: 'Open requests', value: 'Tracked' },
          ]}
          items={[
            { title: 'Office setup', owner: 'Registrar', status: 'Seeded' },
            { title: 'User management', owner: 'Admin', status: 'Enabled' },
            { title: 'Request monitoring', owner: 'Admin', status: 'Prepared' },
          ]}
        />

        <form className="space-y-4 rounded border border-slate-200 bg-white p-6 shadow-sm" onSubmit={handleSubmit}>
          <div>
            <h2 className="text-lg font-semibold text-slate-950">Create user</h2>
            <p className="mt-1 text-sm text-slate-600">Assign the correct role before sharing the temporary password.</p>
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <TextField label="Full name" onChange={(value) => updateField('fullName', value)} value={form.fullName} />
            <TextField label="Email" onChange={(value) => updateField('email', value)} type="email" value={form.email} />
            <TextField label="Temporary password" onChange={(value) => updateField('password', value)} type="password" value={form.password} />
            <label className="block text-sm font-medium text-slate-700">
              Role
              <select
                className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm"
                onChange={(event) => updateField('role', event.target.value as UserRole)}
                value={form.role}
              >
                <option value="STUDENT">Student</option>
                <option value="OFFICE_STAFF">Office Staff</option>
                <option value="REGISTRAR">Registrar</option>
                <option value="ADMIN">Admin</option>
              </select>
            </label>
          </div>

          {form.role === 'STUDENT' && (
            <div className="grid gap-4 rounded border border-slate-100 bg-slate-50 p-4 md:grid-cols-2">
              <TextField label="Student ID" onChange={(value) => updateField('studentId', value)} value={form.studentId ?? ''} />
              <TextField label="Department" onChange={(value) => updateField('department', value)} value={form.department ?? ''} />
              <TextField label="Program" onChange={(value) => updateField('program', value)} value={form.program ?? ''} />
              <label className="block text-sm font-medium text-slate-700">
                Year of study
                <input
                  className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm"
                  min={1}
                  onChange={(event) => updateField('yearOfStudy', Number(event.target.value))}
                  required
                  type="number"
                  value={form.yearOfStudy ?? 1}
                />
              </label>
            </div>
          )}

          {form.role === 'OFFICE_STAFF' && (
            <label className="block max-w-xs text-sm font-medium text-slate-700">
              Office ID
              <input
                className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm"
                min={1}
                onChange={(event) => updateField('officeId', Number(event.target.value))}
                placeholder="Example: 1 for Library"
                required
                type="number"
                value={form.officeId ?? ''}
              />
            </label>
          )}

          {message && <p className="rounded bg-emerald-50 px-3 py-2 text-sm text-emerald-700">{message}</p>}
          {error && <p className="rounded bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>}

          <button
            className="rounded bg-brand-600 px-4 py-2 text-sm font-semibold text-white hover:bg-brand-700 disabled:cursor-not-allowed disabled:bg-slate-400"
            disabled={submitting}
            type="submit"
          >
            {submitting ? 'Creating...' : 'Create user'}
          </button>
        </form>
      </div>
    </PageShell>
  );
}

function TextField({
  label,
  onChange,
  type = 'text',
  value,
}: {
  label: string;
  onChange: (value: string) => void;
  type?: string;
  value: string;
}) {
  return (
    <label className="block text-sm font-medium text-slate-700">
      {label}
      <input
        className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm"
        onChange={(event) => onChange(event.target.value)}
        required
        type={type}
        value={value}
      />
    </label>
  );
}

function buildPayload(form: AdminCreateUserPayload): AdminCreateUserPayload {
  const payload: AdminCreateUserPayload = {
    fullName: form.fullName.trim(),
    email: form.email.trim(),
    password: form.password,
    role: form.role,
  };

  if (form.role === 'STUDENT') {
    payload.studentId = form.studentId?.trim();
    payload.department = form.department?.trim();
    payload.program = form.program?.trim();
    payload.yearOfStudy = Number(form.yearOfStudy);
  }

  if (form.role === 'OFFICE_STAFF') {
    payload.officeId = Number(form.officeId);
  }

  return payload;
}

function formatRole(role: UserRole) {
  return role.replace(/_/g, ' ').toLowerCase();
}
