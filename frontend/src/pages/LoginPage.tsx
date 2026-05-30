import { FormEvent, useState } from 'react';
import { PageShell } from '../components/PageShell';
import { login, type AuthResponse, type UserRole } from '../services/api';

type LoginPageProps = {
  onAuthenticated: (auth: AuthResponse) => void;
  onForgotPassword: () => void;
};

export function LoginPage({ onAuthenticated, onForgotPassword }: LoginPageProps) {
  const [role, setRole] = useState<UserRole>('STUDENT');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setMessage('');
    setError('');
    setSubmitting(true);

    try {
      const auth = await login({ email, password, role });
      setMessage(`Welcome, ${auth.user.fullName}.`);
      onAuthenticated(auth);
    } catch (authError) {
      setError(authError instanceof Error ? authError.message : 'Login failed');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <PageShell
      title="Login"
      subtitle="Sign in with your registered account to open the matching role dashboard."
    >
      <form className="max-w-md space-y-4 rounded border border-slate-200 bg-white p-6 shadow-sm" onSubmit={handleSubmit}>
        <label className="block text-sm font-medium text-slate-700">
          Role
          <select
            className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm"
            onChange={(event) => setRole(event.target.value as UserRole)}
            value={role}
          >
            <option value="STUDENT">Student</option>
            <option value="ADMIN">Admin</option>
            <option value="OFFICE_STAFF">Office Staff</option>
            <option value="REGISTRAR">Registrar</option>
          </select>
        </label>
        <label className="block text-sm font-medium text-slate-700">
          Email
          <input
            className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm"
            onChange={(event) => setEmail(event.target.value)}
            placeholder="student@example.edu"
            required
            type="email"
            value={email}
          />
        </label>
        <label className="block text-sm font-medium text-slate-700">
          Password
          <input
            className="mt-1 w-full rounded border border-slate-300 px-3 py-2 text-sm"
            onChange={(event) => setPassword(event.target.value)}
            placeholder="Password"
            required
            type="password"
            value={password}
          />
        </label>
        {message && <p className="rounded bg-emerald-50 px-3 py-2 text-sm text-emerald-700">{message}</p>}
        {error && <p className="rounded bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>}
        <button
          className="w-full rounded bg-brand-600 px-4 py-2 text-sm font-semibold text-white hover:bg-brand-700 disabled:cursor-not-allowed disabled:bg-slate-400"
          disabled={submitting}
          type="submit"
        >
          {submitting ? 'Signing in...' : 'Login'}
        </button>
        <button className="text-sm font-semibold text-brand-700" onClick={onForgotPassword} type="button">
          Forgot password?
        </button>
      </form>
    </PageShell>
  );
}
