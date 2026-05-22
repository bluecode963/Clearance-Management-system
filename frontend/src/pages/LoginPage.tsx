import { FormEvent, useState } from 'react';
import { PageShell } from '../components/PageShell';
import { login, type AuthResponse } from '../services/api';

type LoginPageProps = {
  onAuthenticated: (auth: AuthResponse) => void;
};

export function LoginPage({ onAuthenticated }: LoginPageProps) {
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
      const auth = await login({ email, password });
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
      </form>
    </PageShell>
  );
}
